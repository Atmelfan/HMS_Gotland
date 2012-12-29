package Renderers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Util.VertexData;

public class ModelObj extends Model
{
	

	
	/*public static int IDs = 0;
	private int id = IDs++;
	
	@Override
	public int hashCode()
	{
		return id;
	}*/
	
	private ArrayList<float[]> vertexsets = new ArrayList<float[]>(); // Vertex Coordinates
	private ArrayList<float[]> vertexsetstexs = new ArrayList<float[]>(); // Vertex Coordinates Textures
	
	private ArrayList<int[]> faces = new ArrayList<int[]>(); // Array of Faces (vertex sets)
	private ArrayList<int[]> facestexs = new ArrayList<int[]>(); // Array of of Faces textures
	
	private int numpolys = 0;
	
	//// Statisitcs for drawing ////
	public float toppoint = 0;		// y+
	public float bottompoint = 0;	// y-
	public float leftpoint = 0;		// x-
	public float rightpoint = 0;	// x+
	public float farpoint = 0;		// z-
	public float nearpoint = 0;		// z+
	
	public ModelObj(File file)
	{
		read(file);
		centerit();
		compileVBO();
		cleanup();
	}

	private void cleanup() 
	{
		vertexsets.clear();
		vertexsetstexs.clear();
		faces.clear();
		facestexs.clear();
	}
	
	private void read(File file) {
		int linecounter = 0;
		
		
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String newline;
			boolean firstpass = true;
			
			while (((newline = br.readLine()) != null)) 
			{
				linecounter++;
				newline = newline.trim();
				if (newline.length() > 0) 
				{
					if (newline.charAt(0) == 'v' && newline.charAt(1) == ' ') 
					{
						float[] coords = new float[4];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) 
						{
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
					    }
						//// check for farpoints ////
						if (firstpass) 
						{
							rightpoint = coords[0];
							leftpoint = coords[0];
							toppoint = coords[1];
							bottompoint = coords[1];
							nearpoint = coords[2];
							farpoint = coords[2];
							firstpass = false;
						}
						if (coords[0] > rightpoint) {
							rightpoint = coords[0];
						}
						if (coords[0] < leftpoint) {
							leftpoint = coords[0];
						}
						if (coords[1] > toppoint) {
							toppoint = coords[1];
						}
						if (coords[1] < bottompoint) {
							bottompoint = coords[1];
						}
						if (coords[2] > nearpoint) {
							nearpoint = coords[2];
						}
						if (coords[2] < farpoint) {
							farpoint = coords[2];
						}
						/////////////////////////////
						vertexsets.add(coords);
					}
					if (newline.charAt(0) == 'v' && newline.charAt(1) == 't') 
					{
						float[] coords = new float[4];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) 
						{
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
						}
						vertexsetstexs.add(coords);
					}
					/*if (newline.charAt(0) == 'v' && newline.charAt(1) == 'n') {
						float[] coords = new float[4];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) {
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
						}
						vertexsetsnorms.add(coords);
					}*/
					if (newline.charAt(0) == 'f' && newline.charAt(1) == ' ') 
					{
						String[] coordstext = newline.split("\\s+");
						int[] v = new int[coordstext.length - 1];
						int[] vt = new int[coordstext.length - 1];
						int[] vn = new int[coordstext.length - 1];
						
						for (int i = 1;i < coordstext.length;i++) 
						{
							String fixstring = coordstext[i].replaceAll("//","/0/");
							String[] tempstring = fixstring.split("/");
							v[i-1] = Integer.valueOf(tempstring[0]).intValue();
							if (tempstring.length > 1) 
							{
								vt[i-1] = Integer.valueOf(tempstring[1]).intValue();
							} else {
								vt[i-1] = 0;
							}
							
							if (tempstring.length > 2) 
							{
								vn[i-1] = Integer.valueOf(tempstring[2]).intValue();
							} else {
								vn[i-1] = 0;
							}
						}
						faces.add(v);
						facestexs.add(vt);
						//facesnorms.add(vn);
					}
				}
			}
			br.close();
			
		}catch(FileNotFoundException e) 
		{
			System.out.println("Failed to find file: " + file.getName());
			e.printStackTrace();
		}catch (IOException e) 
		{
			System.out.println("Failed to read file: " + file.getName());
			e.printStackTrace();	
		}catch (NumberFormatException e) 
		{
			System.out.println("Malformed OBJ (on line " + linecounter + "): " + file.getName() + "\r \r" + e.getMessage());
		}
	}
	
	private void centerit() 
	{
		float xshift = (rightpoint - leftpoint) /2f;
		float yshift = (toppoint - bottompoint) /2f;
		float zshift = (nearpoint - farpoint) /2f;
		
		for (int i=0; i < vertexsets.size(); i++) 
		{
			float[] coords = new float[4];
			
			coords[0] = ((float[])(vertexsets.get(i)))[0] - leftpoint - xshift;
			coords[1] = ((float[])(vertexsets.get(i)))[1] - bottompoint - yshift;
			coords[2] = ((float[])(vertexsets.get(i)))[2] - farpoint - zshift;
			
			vertexsets.set(i, coords); // = coords;
		}
		
	}
	
	public float getXWidth() {
		float returnval = 0;
		returnval = rightpoint - leftpoint;
		return returnval;
	}
	
	public float getYHeight() {
		float returnval = 0;
		returnval = toppoint - bottompoint;
		return returnval;
	}
	
	public float getZDepth() {
		float returnval = 0;
		returnval = nearpoint - farpoint;
		return returnval;
	}
	
	public int numpolygons() {
		return numpolys;
	}
	
	
	
	private int vaoId;
	private int vboId;
	
	public void compileVBO()
	{
		ArrayList<VertexData> data = new ArrayList<VertexData>();
		
		for (int i = 0; i < faces.size(); i++)
		{
			int[] tempfaces = faces.get(i);
			int[] tempfacestexs = facestexs.get(i);

			for (int w = 0; w < tempfaces.length; w++)
			{
				VertexData temp = new VertexData();
				if (tempfacestexs[w] != 0)
				{
					float textempx = ((float[]) vertexsetstexs.get(tempfacestexs[w] - 1))[0];
					float textempy = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[1];
					//float textempz = ((float[]) vertexsetstexs.get(tempfacestexs[w] - 1))[2];
					temp.setST(textempx, textempy);

				}

				float tempx = vertexsets.get(tempfaces[w] - 1)[0];
				float tempy = vertexsets.get(tempfaces[w] - 1)[1];
				float tempz = vertexsets.get(tempfaces[w] - 1)[2];
				temp.setXYZ(tempx, tempy, tempz);
				data.add(temp);
			}
		}
		numpolys = data.size();
		// Put each 'Vertex' in one FloatBuffer
		ByteBuffer verticesByteBuffer = BufferUtils.createByteBuffer(data.size() * VertexData.stride);				
		FloatBuffer verticesFloatBuffer = verticesByteBuffer.asFloatBuffer();
		for (int i = 0; i < data.size(); i++) {
			// Add position, color and texture floats to the buffer
			verticesFloatBuffer.put(data.get(i).getElements());
		}
		verticesFloatBuffer.flip();
		
		// Create a new Vertex Array Object in memory and select it (bind)
		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);
		{
			// Create a new Vertex Buffer Object in memory and select it (bind)
			vboId = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			{
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesFloatBuffer, GL15.GL_STATIC_DRAW);
				// Put the position coordinates in attribute list 0
				GL20.glVertexAttribPointer(0, VertexData.positionElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.positionByteOffset);
				// Put the color components in attribute list 1
				GL20.glVertexAttribPointer(1, VertexData.colorElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.colorByteOffset);
				// Put the texture coordinates in attribute list 2
				GL20.glVertexAttribPointer(2, VertexData.textureElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.textureByteOffset);
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
		GL30.glBindVertexArray(0);
		data.clear();//No longer needed
	}
	
	public void draw()
	{
		GL30.glBindVertexArray(vaoId);
		{
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);
			
			// Draw the vertices
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numpolys);
			
			// Put everything back to default (deselect)
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL20.glDisableVertexAttribArray(2);
			GL30.glBindVertexArray(0);
		}
	}
	
	public void destroy()
	{
		GL30.glBindVertexArray(vaoId);
		
		// Disable the VBO index from the VAO attributes list
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		
		// Delete the vertex VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboId);
		
		// Delete the index VBO
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
	}
}

/**
 * A group of faces with an attached texture
 */
class FaceGroup
{
	private ArrayList<int[]> faces = new ArrayList<int[]>(); // Array of Faces (vertex sets)
	private ArrayList<int[]> facestexs = new ArrayList<int[]>(); // Array of of Faces textures
	private ArrayList<int[]> facesnorms = new ArrayList<int[]>(); // Array of Faces normals
}
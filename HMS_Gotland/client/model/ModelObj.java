package model;

import hms_gotland_client.RenderEngine;

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

import Util.GLUtil;
import Util.ShaderUtils;
import Util.VertexData;

/**
 * @author Jeremy Adams (elias4444)
 *
 * Use these lines if reading from a file
 * FileReader fr = new FileReader(ref);
 * BufferedReader br = new BufferedReader(fr);

 * Use these lines if reading from within a jar
 * InputStreamReader fr = new InputStreamReader(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ref)));
 * BufferedReader br = new BufferedReader(fr);
 */

public class ModelObj extends Model
{
	
	private ArrayList<float[]> vertexsets = new ArrayList<float[]>(); // Vertex Coordinates
	private ArrayList<float[]> vertexsetstexs = new ArrayList<float[]>(); // Vertex Coordinates Textures
	private ArrayList<float[]> vertexsetsnorms = new ArrayList<float[]>(); // Vertex Coordinates
	
	private ArrayList<int[]> faces = new ArrayList<int[]>(); // Array of Faces (vertex sets)
	private ArrayList<int[]> facestexs = new ArrayList<int[]>(); // Array of of Faces textures
	private ArrayList<int[]> facesnorms = new ArrayList<int[]>(); // Array of of Faces textures
	
	private int numpolys = 0;
	
	//// Statisitcs for drawing ////
	public float toppoint = 0;		// y+
	public float bottompoint = 0;	// y-
	public float leftpoint = 0;		// x-
	public float rightpoint = 0;	// x+
	public float farpoint = 0;		// z-
	public float nearpoint = 0;		// z+
	private static int vsId;
	private static int fsId;
	private static int shader_id;
	
	public ModelObj(File file, boolean clearVertexData)
	{
		read(file);
		centerit();
		if(shader_id == 0)
		{
			setupShader();
		}
		compileVBO();
		cleanup();
		if(clearVertexData)
		{
			data.clear();
		}
	}

	private void cleanup() 
	{
		vertexsets.clear();
		vertexsetstexs.clear();
		vertexsetsnorms.clear();
		
		faces.clear();
		facestexs.clear();
		facestexs.clear();
	}
	
	public void setupShader()
	{
		vsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/default.vert"), GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		fsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/default.frag"), GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		shader_id = ShaderUtils.makeProgram(vsId, fsId);
		
		GL20.glBindAttribLocation(shader_id, 0, "in_Position");
		GL20.glBindAttribLocation(shader_id, 1, "in_TextureCoord");
		GL20.glBindAttribLocation(shader_id, 2, "in_Normal");
		
		GL20.glValidateProgram(shader_id);
		GLUtil.cerror(getClass().getName() + " setupShader");
		
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
					if (newline.charAt(0) == 'v' && newline.charAt(1) == 'n') {
						float[] coords = new float[4];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) {
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
						}
						vertexsetsnorms.add(coords);
					}
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
						facesnorms.add(vn);
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
			
			coords[0] = (vertexsets.get(i))[0] - leftpoint - xshift;
			coords[1] = (vertexsets.get(i))[1] - bottompoint - yshift;
			coords[2] = (vertexsets.get(i))[2] - farpoint - zshift;
			
			vertexsets.set(i, coords); // = coords;
		}
		
	}
	
	@Override
	public float getXWidth() {
		float returnval = 0;
		returnval = rightpoint - leftpoint;
		return returnval;
	}
	
	@Override
	public float getYHeight() {
		float returnval = 0;
		returnval = toppoint - bottompoint;
		return returnval;
	}
	
	@Override
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
	public ArrayList<VertexData> data = new ArrayList<>();
	
	public void compileVBO()
	{
		ArrayList<VertexData> data = new ArrayList<>();
		//Assemble face indice
		for (int i = 0; i < faces.size(); i++)
		{
			int[] tempfaces = faces.get(i);
			int[] tempfacestexs = facestexs.get(i);
			int[] tempfacesnorms = facestexs.get(i);
			
			for (int w = 0; w < tempfaces.length; w++)
			{
				VertexData newv = new VertexData();
				////////Vertex////////
				newv.setXYZ(vertexsets.get(tempfaces[w] - 1)[0], vertexsets.get(tempfaces[w] - 1)[1], vertexsets.get(tempfaces[w] - 1)[2]);
				
			    ////////Texture coords////////
				if(tempfacestexs[w] < vertexsetstexs.size())
				{
					newv.setST(vertexsetstexs.get(tempfacestexs[w] - 1)[0], vertexsetstexs.get(tempfacestexs[w] - 1)[1]);
				}
				
			    ////////Normals////////
				if(tempfacesnorms[w] < vertexsetsnorms.size())
				{
					newv.setNormal(vertexsetsnorms.get(tempfacesnorms[w] - 1)[0], vertexsetsnorms.get(tempfacesnorms[w] - 1)[0], vertexsetsnorms.get(tempfacesnorms[w] - 1)[0]);
				}
				data.add(newv);
			}
			numpolys++;
		}
		
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
				GL20.glVertexAttribPointer(1, VertexData.textureElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.textureByteOffset);
				// Put the texture coordinates in attribute list 2
				GL20.glVertexAttribPointer(2, VertexData.normalElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.normalByteOffset);
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
		GL30.glBindVertexArray(0);
		GLUtil.cerror(getClass().getName() + " compileVBO");
	}
	
	@Override
	public void draw(float frame, float[] vpMatrix, float[] matrix, RenderEngine engine)
	{	
		ShaderUtils.useProgram(shader_id);
		{
			ShaderUtils.setUniformMatrix4(shader_id, "viewprojMatrix", vpMatrix);
			ShaderUtils.setUniformMatrix4(shader_id, "modelMatrix", matrix);
			GL30.glBindVertexArray(vaoId);
			{
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				GL20.glEnableVertexAttribArray(2);
				// Draw the vertices
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numpolys * 3);
				// Put everything back to default (deselect)
				
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
				GL20.glDisableVertexAttribArray(2);
			}
			GL30.glBindVertexArray(0);
		}
		ShaderUtils.useProgram(0);
	}
	
	@Override
	public void destroy()
	{
		GL30.glBindVertexArray(vaoId);
		
		// Disable the VBO index from the VAO attributes list
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		
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
	public int texture_id;
	public float Ka;
	public float Kd;
	public float Ks;
	
	public float d;//or Tr
	
	
	private ArrayList<int[]> faces = new ArrayList<int[]>(); // Array of Faces (vertex sets)
	private ArrayList<int[]> facestexs = new ArrayList<int[]>(); // Array of of Faces textures
	private ArrayList<int[]> facesnorms = new ArrayList<int[]>(); // Array of Faces normals
	
	public boolean isEmpty()
	{
		return faces.isEmpty() && facestexs.isEmpty() && facesnorms.isEmpty();
	}
}


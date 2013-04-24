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
import java.util.HashMap;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.ShapeHull;
import com.bulletphysics.util.ObjectArrayList;

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
	
	protected ArrayList<float[]> vertexsets = new ArrayList<float[]>(); // Vertex Coordinates
	private ArrayList<float[]> vertexsetstexs = new ArrayList<float[]>(); // Vertex Coordinates Textures
	private ArrayList<float[]> vertexsetsnorms = new ArrayList<float[]>(); // Vertex Coordinates
	
	int numpolys = 0;
	
	//// Statisitcs for drawing ////
	public float toppoint = 0;		// y+
	public float bottompoint = 0;	// y-
	public float leftpoint = 0;		// x-
	public float rightpoint = 0;	// x+
	public float farpoint = 0;		// z-
	public float nearpoint = 0;		// z+
	private static GLShader shader;
	
	public ModelObj(RenderEngine rend, File file)
	{
		super(rend, file);
		read(file);
		compileFaceGroups();
		if(shader == null)
		{
			setupShader();
		}
		cleanup();
	}
	
	private void cleanup() 
	{
		vertexsets.clear();
		vertexsetstexs.clear();
		vertexsetsnorms.clear();
	}
	
	public void setupShader()
	{
		shader = new GLShader("Resources/shaders/default");
		shader.bindAttribLocation(0, "in_Position");
		shader.bindAttribLocation(1, "in_TextureCoord");
		shader.bindAttribLocation(2, "in_Normal");
	}
	
	@Override
	protected void read(File file) {
		int linecounter = 0;
		FaceGroup currentGroup = null;
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
					if (newline.charAt(0) == 'm' && newline.charAt(1) == 't')//mtllib
					{
						loadMTL(newline.split("\\s+")[1], file.getParentFile());
					}
					if (newline.charAt(0) == 'u' && newline.charAt(1) == 's')//usemtl
					{
						String usemtl = newline.split("\\s+")[1];
						if(mtllibs.containsKey(usemtl))
						{
							currentGroup = mtllibs.get(usemtl);
						}
					}
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
						if(currentGroup == null)
							continue;
						currentGroup.faces.add(v);
						currentGroup.facestexs.add(vt);
						currentGroup.facesnorms.add(vn);
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
		centerit();
	}

	private void compileFaceGroups()
	{
		for(FaceGroup group : mtllibs.values())
		{
			//Compile openGL vbo
			group.compileVBO();
			group.clear();
		}
	}
	
	private HashMap<String, FaceGroup> mtllibs = new HashMap<String, FaceGroup>();
	private void loadMTL(String string, File f)
	{
		File mtllib = new File(f, string);
		if(mtllib.exists())
		{
			try
			{
				BufferedReader mtlreader = new BufferedReader(new FileReader(mtllib));
				String newline = null;
				FaceGroup currentMtl = null;
				while ((newline = mtlreader.readLine()) != null)
				{
					newline = newline.trim();
					String[] mtl = newline.split(" ");
					if(newline.startsWith("newmtl"))
					{
						if(mtl.length > 1)
						{
							currentMtl = new FaceGroup(mtl[1]);
							mtllibs.put(mtl[1], currentMtl);
							//System.out.println("newmtl " + currentMtl.name);
						}
					}
					else if(currentMtl != null)
					{
						if(newline.startsWith("map_Kd"))
						{
							if(mtl.length > 1)
							{
								currentMtl.texture_id = renderer.getTexture(f.getAbsolutePath() + File.separator + mtl[1], GL13.GL_TEXTURE0);
								//System.out.println(currentMtl.name + " map_Kd " + mtl[1]);
							}
						}
						//TODO
					}
					
				}
				mtlreader.close();
			} catch (FileNotFoundException e)
			{
				System.err.println("Error: ModelObj.loadMTL() - " + e.getMessage());
			} catch (IOException e)
			{
				System.err.println("Error: ModelObj.loadMTL() - " + e.getMessage());
			}
			
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
	
	@Override
	public void draw(float frame, float[] vpMatrix, float[] matrix, RenderEngine engine)
	{	
		super.draw(frame, vpMatrix, matrix, engine);
		//TODO fix ugly quick hacked OpenGL code
		shader.bind();
		{
			shader.setUniformVar("time", engine.getPartTick() / 3);
			shader.setUniformMatrix4("viewprojMatrix", vpMatrix);
			shader.setUniformMatrix4("modelMatrix", matrix);
			
			for (FaceGroup g : mtllibs.values())
			{
				g.drawArray();
			}
			
		}
		GLUtil.cerror("draw");
	}
	
	@Override
	public void destroy()
	{
		//TODO
	}
	
	private ObjectArrayList<Vector3f> vertexes = new ObjectArrayList<Vector3f>();
	
	@Override
	public CollisionShape body()
	{
		ConvexHullShape hulls = new ConvexHullShape(vertexes);
		ShapeHull hull = new ShapeHull(hulls);
		hull.buildHull(hulls.getMargin());
		return new ConvexHullShape(hull.getVertexPointer());
		
	}
	
	/**
	 * A group of faces with an attached texture
	 */
	class FaceGroup
	{
		public String name;
		public int texture_id;
		
		public float Ka;
		public float Kd;
		public float Ks;
		
		public float d;//or Tr
		
		private ArrayList<int[]> faces = new ArrayList<int[]>(); // Array of Faces (vertex sets)
		private ArrayList<int[]> facestexs = new ArrayList<int[]>(); // Array of of Faces textures
		private ArrayList<int[]> facesnorms = new ArrayList<int[]>(); // Array of Faces normals
		private int numVerts = 0;
		private GLVao vao;
		private GLVbo vbo;
		
		public FaceGroup(String string)
		{
			name = string;
		}

		public void compileVBO()
		{
			vao = new GLVao();
			vbo = new GLVbo(GL15.GL_ARRAY_BUFFER);
			//Assemble face indice
			for (int i = 0; i < faces.size(); i++)
			{
				int[] tempfaces = faces.get(i);
				int[] tempfacestexs = facestexs.get(i);
				int[] tempfacesnorms = facestexs.get(i);
				
				for (int w = 0; w < tempfaces.length; w++)
				{
					////////Vertex////////
					vbo.addElements(vertexsets.get(tempfaces[w] - 1)[0]);
					vbo.addElements(vertexsets.get(tempfaces[w] - 1)[1]);
					vbo.addElements(vertexsets.get(tempfaces[w] - 1)[2]);
					vertexes.add(new Vector3f(vertexsets.get(tempfaces[w] - 1)));
				    ////////Texture coords////////
					if(tempfacestexs[w] < vertexsetstexs.size())
					{
						vbo.addElements(vertexsetstexs.get(tempfacestexs[w] - 1)[0]);
						vbo.addElements(1f - vertexsetstexs.get(tempfacestexs[w] - 1)[1]);
					}
					
				    ////////Normals////////
					if(tempfacesnorms[w] < vertexsetsnorms.size())
					{
						vbo.addElements(vertexsetsnorms.get(tempfacesnorms[w] - 1)[0]);
						vbo.addElements(vertexsetsnorms.get(tempfacesnorms[w] - 1)[1]);
						vbo.addElements(vertexsetsnorms.get(tempfacesnorms[w] - 1)[2]);
					}
					numVerts++;
				}
				numpolys++;
			}
			
			// Create a new Vertex Array Object in memory and select it (bind)
			vbo.compile(GL15.GL_STATIC_DRAW);
			vao.addBuffer(0, 3, GL11.GL_FLOAT, 32, 0, vbo);
			vao.addBuffer(1, 2, GL11.GL_FLOAT, 32, 12, vbo);
			vao.addBuffer(2, 3, GL11.GL_FLOAT, 32, 20, vbo);
			//Enable the attrib arrays
			vao.enableVertexAttrib(0);
			vao.enableVertexAttrib(1);
			vao.enableVertexAttrib(2);
			
			GLUtil.cerror(getClass().getName() + " compileVBO");
		}
		
		public void drawArray()
		{
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture_id);
			vao.drawArrays(GL11.GL_TRIANGLES, 0, numVerts);
		}
		
		public void clear()
		{
			faces.clear();
			facestexs.clear();
			facesnorms.clear();
		}
		
		public boolean isEmpty()
		{
			return faces.size() == 0;
		}

		@Override
		public String toString()
		{
			return "FaceGroup [name=" + name + ", vaoId=" + vao + ", vboId="
					+ vbo + ", numVerts=" + numVerts + ", textureID=" + texture_id + "]";
		}
		
		
	}
}


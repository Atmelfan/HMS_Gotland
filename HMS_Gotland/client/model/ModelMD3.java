package model;

import hms_gotland_client.RenderEngine;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Util.GLUtil;
import Util.ShaderUtils;
import Util.VertexData;

public class ModelMD3 extends Model
{
	private static final int MAX_QPATH = 64;
	public static final float MD3_XYZ_SCALE = (1f / 64);
	private MD3Header header;
	private MD3Frame[] frames;
	private MD3Tag[] tags;
	private MD3Surface[] surfaces;
	static private int vsId;
	static private int fsId;
	static private int shader_id;
	public int vaoID;
	
	public ModelMD3(RenderEngine rend, File file)
	{
		super(rend, file);
		read(file);
		setupVAO();
		if(shader_id <= 0)
		{
			setupShader();
		}
	}
	
	private void setupVAO()
	{
		vaoID = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoID);
		{
			for (int i = 0; i < surfaces.length; i++)
			{
				surfaces[i].setupVBO();
			}
		}
		GL30.glBindVertexArray(0);
	}
	
	@Override
	public boolean isAnimated()
	{
		return true;
	}
	
	public void draw(float frame, float[] vpMatrix, float[] modelMatrix, RenderEngine engine, MD3TagCallback callback)
	{
		draw(frame, vpMatrix, modelMatrix, engine);
		for (int i = 0; i < tags.length; i++)
		{
			callback.tag(tags[i]);
		}
	}
	
	@Override
	public void draw(float frame, float[] vpMatrix, float[] modelMatrix, RenderEngine engine)
	{
		frame = Math.min(frame, header.num_frames);
		int frame1 = (int)Math.floor(frame);
		int frame2 = (int)Math.ceil(frame);
		float interpolation = frame - frame1;
		ShaderUtils.useProgram(shader_id);
		{
			ShaderUtils.setUniformMatrix4(shader_id, "viewprojMatrix", vpMatrix);
			ShaderUtils.setUniformMatrix4(shader_id, "modelMatrix", modelMatrix);
			ShaderUtils.setUniformVar(shader_id, "frame_interpolated", interpolation);
			GL30.glBindVertexArray(vaoID);
			for (int i = 0; i < surfaces.length; i++)
			{
				surfaces[i].draw(frame1, frame2);
			}
		}
	}

	public void cleanup()
	{
		
	}
	
	@Override
	public void destroy()
	{
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoID);
		for (int i = 0; i < surfaces.length; i++)
		{
			surfaces[i].destroy();
		}
	}
	
	public void setupShader()
	{
		vsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/animation.vert"), GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		fsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/animation.frag"), GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		shader_id = ShaderUtils.makeProgram(vsId, fsId);
		
		GL20.glBindAttribLocation(shader_id, 0, "in_Position_0");
		GL20.glBindAttribLocation(shader_id, 1, "in_TextureCoord_0");
		GL20.glBindAttribLocation(shader_id, 2, "in_Normal_0");
		
		GL20.glBindAttribLocation(shader_id, 3, "in_Position_1");
		GL20.glBindAttribLocation(shader_id, 4, "in_TextureCoord_1");
		GL20.glBindAttribLocation(shader_id, 5, "in_Normal_1");
		
		GL20.glValidateProgram(shader_id);
		GLUtil.cerror(getClass().getName() + " setupShader");
	}
	
	protected void read(File file)
	{
		try
		{
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			
			header = new MD3Header();
			header.read(in);
			System.out.println("ident " + (char)(header.ident & 255) + (char)((header.ident >> 8) & 255) + 
					(char)((header.ident >> 16) & 255) + (char)((header.ident >> 24) & 255));
			System.out.println("versn " + header.version);
			System.out.println("frams " + header.num_frames);
			System.out.println("surfs " + header.num_surfaces);
			/*Read frames*/
			frames = new MD3Frame[header.num_frames];
			for (int i = 0; i < header.num_frames; i++)
			{
				frames[i].read(in);
			}
			/*Read tags*/
			tags = new MD3Tag[header.num_tags];
			for (int i = 0; i < header.num_tags; i++)
			{
				tags[i].read(in);
			}
			/*Read surfaces*/
			surfaces = new MD3Surface[header.num_surfaces];
			for (int i = 0; i < header.num_surfaces; i++)
			{
				surfaces[i].read(in);
			}
			
			in.close();
		} catch (FileNotFoundException e)
		{
			System.err.println("Error: ModelMD3.read() - " + e.getMessage());
		} catch (IOException e)
		{
			System.err.println("Error: ModelMD3.read() - " + e.getMessage());
		}
		
	}

	private class MD3Header extends MD3Component
	{
		public int ident;
		public int version;
		public String name;
		public int flags;//Unused?
		public int num_frames;
		public int num_tags;
		public int num_surfaces;
		public int num_skins;//Unused?
		public int ofs_frames;
		public int ofs_tags;
		public int ofs_surfaces;
		public int ofs_eof;
		@Override
		void read(DataInputStream in) throws IOException
		{
			ident = readInteger(in);
			version = readInteger(in);
			name = readString(in, MAX_QPATH);
			flags = readInteger(in);
			num_frames = readInteger(in);
			num_tags = readInteger(in);
			num_surfaces = readInteger(in);
			num_skins = readInteger(in);
			ofs_frames = readInteger(in);
			ofs_tags = readInteger(in);
			ofs_surfaces = readInteger(in);
			ofs_eof = readInteger(in);
		}
		
	}
	
	private class MD3Frame extends MD3Component
	{
		public MD3Vec bound_min = new MD3Vec();
		public MD3Vec bound_max = new MD3Vec();
		public MD3Vec origin = new MD3Vec();
		public float radius;
		public String name;
		
		@Override
		void read(DataInputStream in) throws IOException
		{
			bound_min.read(in);
			bound_max.read(in);
			checkBounds(bound_max, bound_min);
			origin.read(in);
			radius = readFloat(in);
			name = readString(in, 16);
		}
	}
	
	public class MD3Tag extends MD3Component
	{
		public Model tag_model;
		public String name;
		public MD3Vec origin = new MD3Vec();
		public float[] orientation = new float[9];
		@Override
		void read(DataInputStream in) throws IOException
		{
			name = readString(in, MAX_QPATH);
			origin.read(in);
			for (int i = 0; i < orientation.length; i++)
			{
				orientation[i] = readFloat(in);
			}
			tag_model = renderer.getModel(name);
		}
		
		public boolean draw(float frame, float[] vpMatrix, float[] mdMatrix, RenderEngine engine)
		{
			if(tag_model != null)
			{
				tag_model.draw(frame, vpMatrix, mdMatrix, engine);
				return true;
			}
			return false;
		}
	}
	
	private class MD3Surface extends MD3Component
	{
		public int ident;
		public String name;
		public int flags;
		
		public int num_frames;
		public int num_shaders;
		public int num_verts;
		public int num_triangles;
		
		public int ofs_triangles;
		public int ofs_shaders;
		public int ofs_st;
		public int ofs_verts;
		public int ofs_end;
		private MD3Shader[] shaders;
		private MD3Triangle[] triangles;
		private MD3ST[] sts;
		private MD3Vertex[] verts;
		private int[] vboIDs;
		@Override
		void read(DataInputStream in) throws IOException
		{
			ident = readInteger(in);
			name = readString(in, MAX_QPATH);
			flags = readInteger(in);
			
			num_frames = readInteger(in);
			num_shaders = readInteger(in);
			num_verts = readInteger(in);
			num_triangles = readInteger(in);
			
			ofs_triangles = readInteger(in);
			ofs_shaders = readInteger(in);
			ofs_st = readInteger(in);
			ofs_verts = readInteger(in);
			ofs_end = readInteger(in);
			/*Read shaders*/
			shaders = new MD3Shader[num_shaders];
			for (int i = 0; i < num_shaders; i++)
			{
				shaders[i].read(in);
			}
			/*Read triangles*/
			triangles = new MD3Triangle[num_triangles];
			for (int i = 0; i < num_triangles; i++)
			{
				triangles[i].read(in);
			}
			/*Read ST's (texture coordinates*/
			sts = new MD3ST[num_verts];
			for (int i = 0; i < num_verts; i++)
			{
				sts[i].read(in);
			}
			/*Read vertexes*/
			verts = new MD3Vertex[num_verts * num_frames];
			for (int i = 0; i < num_verts * num_frames; i++)
			{
				verts[i].read(in);
			}
		}
		
		public void destroy()
		{
			for (int i = 0; i < vboIDs.length; i++)
			{
				GL15.glDeleteBuffers(vboIDs[i]);
			}
			GLUtil.cerror("ModelMD3(" + header.name + ").MD3Surface(" + name + ").destroy");
		}
		
		public void setupVBO()
		{
			FloatBuffer data = BufferUtils.createFloatBuffer(num_verts * 3);
			for (int i = 0; i < num_frames; i++)
			{
				//Upload all triangles for every frame
				for (int j = 0; j < triangles.length; j++)
				{
					MD3Triangle triangle = triangles[j];
					//Offset the triangle index by the frame
					data.put(verts[triangle.indexes[0] + num_verts * i].getElements(sts[triangle.indexes[0]].s, sts[triangle.indexes[0]].t));
					data.put(verts[triangle.indexes[1] + num_verts * i].getElements(sts[triangle.indexes[1]].s, sts[triangle.indexes[1]].t));
					data.put(verts[triangle.indexes[2] + num_verts * i].getElements(sts[triangle.indexes[2]].s, sts[triangle.indexes[2]].t));
				}
				vboIDs[i] = GL15.glGenBuffers();
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIDs[i]);
				{
					GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
					// Put the position coordinates in attribute list 0
				}
			}
		}
		
		public void draw(int frame1, int frame2)
		{
			//Bind texture array
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIDs[frame1]);
			{
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 32, 0);
				GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 32, 12);
				GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 32, 20);
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIDs[frame2]);
			{
				GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 32, 0);
				GL20.glVertexAttribPointer(4, 2, GL11.GL_FLOAT, false, 32, 12);
				GL20.glVertexAttribPointer(5, 3, GL11.GL_FLOAT, false, 32, 20);
			}
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, num_verts);
		}
	}
	
	private class MD3Shader extends MD3Component
	{
		public String name;
		public int index;
		@Override
		void read(DataInputStream in) throws IOException
		{
			name = readString(in, MAX_QPATH);
			index = readInteger(in);
		}
	}
	
	private class MD3Triangle extends MD3Component
	{
		public int[] indexes = new int[3];
		@Override
		void read(DataInputStream in) throws IOException
		{
			indexes[0] = readInteger(in);
			indexes[1] = readInteger(in);
			indexes[2] = readInteger(in);
		}
	}
	
	private class MD3ST extends MD3Component
	{
		public float s;
		public float t;
		@Override
		void read(DataInputStream in) throws IOException
		{
			s = readFloat(in);
			t = readFloat(in);
		}
	}
	
	private class MD3Vertex extends MD3Component
	{
		public float x;
		public float y;
		public float z;
		public float[] normal;
		@Override
		void read(DataInputStream in) throws IOException
		{
			x = readShort(in) * MD3_XYZ_SCALE;
			y = readShort(in) * MD3_XYZ_SCALE;
			z = readShort(in) * MD3_XYZ_SCALE;
			normal = decodeNormal(readShort(in));
		}
		
		public float[] getElements(float s, float t)
		{
			return new float[]{x, y, z, s, t, normal[0], normal[1], normal[2]};
		}
	}
	
	//Technical, not part of the MD3 reader/renderer
	private abstract class MD3Component
	{
		abstract void read(DataInputStream in) throws IOException;
	}
	
	private final class MD3Vec extends MD3Component
	{
		public float x;
		public float y;
		public float z;
		@Override
		void read(DataInputStream in) throws IOException
		{
			x = readFloat(in);
			y = readFloat(in);
			z = readFloat(in);
		}
	}
	
	private static float[] decodeNormal(short normal)
	{
		float[] result = new float[3];
		double lat = ((normal >> 8) & 255) * (2 * Math.PI ) / 255;
		double lng = (normal & 255) * (2 * Math.PI) / 255;
		result[0] = (float) (Math.cos(lat) * Math.sin(lng));
		result[1] = (float) (Math.sin(lat) * Math.sin(lng));
		result[2] = (float) (Math.cos(lng));
		return result;
	}
	
	private static int readInteger(DataInputStream in) throws IOException
	{
		return Integer.reverseBytes(in.readInt());
	}
	
	private static short readShort(DataInputStream in) throws IOException
	{
		return Short.reverseBytes(in.readShort());
	}
	
	private static String readString(DataInputStream in, int chars) throws IOException
	{
		byte[] str = new byte[chars];
		in.read(str);
		return new String(str, "ASCII");
	}
	
	private static float readFloat(DataInputStream in) throws IOException
	{
		return Float.intBitsToFloat(Integer.reverseBytes(in.readInt()));
	}
	
	
	private Vector3f max;
	private Vector3f min;
	private void checkBounds(MD3Vec mmax, MD3Vec mmin)
	{
		if(mmax.x > max.x) {
			max.x = mmax.x;
		}
		else if(mmax.y > max.y) {
			max.y = mmax.y;
		}
		else if(mmax.z > max.z) {
			max.z = mmax.z;
		}
		
		if(mmin.x > min.x) {
			min.x = min.x;
		}
		else if(mmin.y > min.y) {
			min.y = min.y;
		}
		else if(mmin.z > min.z) {
			min.z = min.z;
		}
	}

	@Override
	public float getXWidth()
	{
		return max.x - min.x;
	}

	@Override
	public float getYHeight()
	{
		return max.y - min.y;
	}

	@Override
	public float getZDepth()
	{
		return max.y - min.y;
	}
	
	public interface MD3TagCallback
	{
		public void tag(MD3Tag tags);
	}
}

package model;

import hms_gotland_client.RenderEngine;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Util.GLUtil;
import Util.ShaderUtils;
import Util.VertexData;

/**
 * @author Atmelfan
 * Blablabla license blablabla don't give a shit
 * blablablabla blabla bla blabla
 * Provided as is, if you fuck up, it's your own damn fault.
 */
public class ModelMD2 extends Model
{
	/*Bounding box*/
	public Vector3f boundingBoxMin;
	public Vector3f boundingBoxMax;
	
	/*MD2 data*/
	private MD2_Header 		header;
	private MD2_ST[] 		textureCoords;
	private MD2_Triangle[] 	triangles;
	private MD2_Frame[] 	frames;
	private MD2_Skin[] 		skins;
	
	/*OpenGL data*/
	private int tex_id;
	private int vao_id = GL30.glGenVertexArrays();
	private int[] frame_ids;
	private static int shader_id = 0;
	private static int vsId = 0;
	private static int fsId = 0;
	
	public ModelMD2(File file, boolean b)
	{
		//Read MD2 data file
		read(file);
		//Print MD2 info
		//System.out.println(header.toString());
		//Assemble frames into VBOs
		compileVBO();
		//Setup shared interpolation & translation shader if not done yet
		if(shader_id <= 0)
		{
			setupShader();
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
	
	@Override
	public void destroy()
	{
		//Delete VAO
		GL30.glBindVertexArray(vao_id);
		{
			for(int i = 0; i < 6; i++){ GL20.glDisableVertexAttribArray(i);}
			
			//Delete VBOs
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			for (int i = 0; i < frame_ids.length; i++)
			{
				GL15.glDeleteBuffers(frame_ids[i]);
			}
		}
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vao_id);
		GL11.glDeleteTextures(tex_id);
	}

	public void drawCEL(float frame, float[] vpMatrix, float[] matrix, RenderEngine engine)
	{
		GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_LINE);
		GL11.glCullFace(GL11.GL_FRONT); 
		draw(frame, vpMatrix, matrix, engine);
		GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_FILL);
		GL11.glCullFace(GL11.GL_BACK);
		draw(frame, vpMatrix, matrix, engine);
	}
	
	@Override
	public void draw(float frame, float[] vpMatrix, float[] matrix, RenderEngine engine)
	{
		if(frame < 0 || frame > header.num_frames - 1) return;
			
		//Select current frame and next
		int frame_0 = frame_ids[(int) Math.floor(frame)];
		
		int frame_1 = frame_ids[(int) Math.min(Math.ceil(frame), header.num_frames - 1)];
		//Upload frame interpolation
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex_id);
		
		ShaderUtils.useProgram(shader_id);
		{
			//Upload uniform values
			ShaderUtils.setUniformMatrix4(shader_id, "viewprojMatrix", vpMatrix);
			ShaderUtils.setUniformMatrix4(shader_id, "modelMatrix", matrix);
			ShaderUtils.setUniformVar(shader_id, "frame_interpolated", (float)(frame - Math.floor(frame)));
			ShaderUtils.setUniformVar(shader_id, "cameraDir", engine.camera.yaw, engine.camera.pitch, engine.camera.roll);
			//Bind frames to VAO
			GL30.glBindVertexArray(vao_id);
			{
				
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, frame_0);//Bind frame 0
				{
					GL20.glVertexAttribPointer(0, VertexData.positionElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.positionByteOffset);
					GL20.glVertexAttribPointer(1, VertexData.textureElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.textureByteOffset);
					GL20.glVertexAttribPointer(2, VertexData.normalElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.normalByteOffset);
				}
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
				
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, frame_1);//Bind frame 1
				{
					GL20.glVertexAttribPointer(3, VertexData.positionElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.positionByteOffset);
					GL20.glVertexAttribPointer(4, VertexData.textureElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.textureByteOffset);
					GL20.glVertexAttribPointer(5, VertexData.normalElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.normalByteOffset);
				}
				
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
				
				//Enable attribs and render
				for(int i = 0; i < 6; i++){ GL20.glEnableVertexAttribArray(i); }
				{
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, header.num_tris * 3);
				}
				for(int i = 0; i < 6; i++){ GL20.glDisableVertexAttribArray(i); }
				
				
			}
			GL30.glBindVertexArray(0);
		}
		ShaderUtils.useProgram(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	@Override
	public boolean isAnimated()
	{
		return true;
	}

	public void compileVBO()
	{
		frame_ids = new int[header.num_frames];
		ArrayList<VertexData> data = new ArrayList<>();
		for(int currentFrame = 0; currentFrame < header.num_frames; currentFrame++)
		{
			MD2_Frame frame = frames[currentFrame];// Current frame
			for (int currentTriangle = 0; currentTriangle < header.num_tris; currentTriangle++)
			{
				for (int j = 0; j < 3; j++)
				{
					VertexData temp = new VertexData();
					//Extract position from vertex array using triangles vertex index
					MD2_Vertex vertex = frame.vertices[triangles[currentTriangle].vertexIndex[j]];
					//For MD2 format/exporter Z is up, swap Z and Y for OGL
					temp.setXYZ(vertex.v[1], vertex.v[2], vertex.v[0]);
					
					//Extract texture coords from st array using triangle texture index
					float s = (float)(textureCoords[triangles[currentTriangle].textureIndex[j]].s / 256f);
					float t = (float)(textureCoords[triangles[currentTriangle].textureIndex[j]].t / 256f);
					temp.setST(s, t);
					
					//Normals, why hardcoded?
					int index = frame.vertices[triangles[currentTriangle].vertexIndex[j]].lightNormalIndex;
					//Some models seems to use a different normal array and doesn't work with Quake II normals
					if(index < ModelMD2_Normals.normals.length)
					{
						float[] n = ModelMD2_Normals.normals[index];
						temp.setNormal(n[0], n[2], n[1]);
					}
					
					data.add(temp);//Add vertex data
				}
			}
			//Put data into floatbuffer
			ByteBuffer verticesByteBuffer = BufferUtils.createByteBuffer(data.size() * VertexData.stride);				
			FloatBuffer verticesFloatBuffer = verticesByteBuffer.asFloatBuffer();
			for (int i = 0; i < data.size(); i++) 
			{
				// Add position, normal and texture floats to the buffer
				verticesFloatBuffer.put(data.get(i).getElements());
			}
			verticesFloatBuffer.flip();
			
			//Put data into its vbo
			int id = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
			{
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesFloatBuffer, GL15.GL_STATIC_DRAW);
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			//Clear data for next frame
			data.clear();
			
			//Save frame id
			frame_ids[currentFrame] = id;
		}
		GLUtil.cerror(getClass().getName() + " compileVBO");
	}
	
	public void read(File file)
	{
		try
		{
			DataInputStream data = new DataInputStream(new FileInputStream(file));
			////////Read header////////
			header = new MD2_Header();
			header.read(data);
			
			//Allocate frame ids
			//frame_ids = new int[header.num_frames];
			
			if(header.ident != (('2'<<24) + ('P'<<16) + ('D'<<8) + 'I') || header.version != 8)
				System.err.println("Invalid magic or version, file may be invalid or corrupted!");
			
		    ////////Read skins////////
			//data.seek(header.ofs_skins); No seek command in datainputstream
			
			skins = new MD2_Skin[header.num_skins];
			for(int i = 0; i < header.num_skins; i++)
			{
				skins[i] = new MD2_Skin().read(data);
			}
			
			////////Read st////////
			//data.seek(header.ofs_st);
			
			textureCoords = new MD2_ST[header.num_st];
			for(int i = 0; i < header.num_st; i++)
			{
				textureCoords[i] = new MD2_ST().read(data);
			}
			
			//Read triangles////////
			//data.seek(header.ofs_tris);
			
			triangles = new MD2_Triangle[header.num_tris];
			for(int i = 0; i < header.num_tris; i++)
			{
				triangles[i] = new MD2_Triangle().read(data);
			}
			
			////////Read frames////////
			//data.seek(header.ofs_frames);
			
			frames = new MD2_Frame[header.num_frames];
			for(int i = 0; i < header.num_frames; i++)
			{
				frames[i] = new MD2_Frame().read(data);
			}
			
			////////Read OpenGL cmds////////
			//TODO implement replacement using OpenGL 3 maybe?
			
			data.close();
		} catch (FileNotFoundException e)
		{
			System.out.println("Failed to find file - " + file.getPath());
			e.printStackTrace();
		} catch (IOException e)
		{
			System.out.println("Failed to read file - " + file.getPath());
			e.printStackTrace();
		}
	}
	/*Header*/
	class MD2_Header
	{
		//Version
		int ident = 0;
		int version = 0;
		
		//Skin 
		int height = 0;
		int width = 0;
		
		//Frame
		int frameSize = 0;
		
		//Nums
		int num_skins = 0;
		int num_xyz = 0;
		int num_st = 0;
		int num_tris = 0;
		int num_glcmds = 0;
		int num_frames = 0;
		
		//Ofs
		int ofs_skins = 0;
		int ofs_st = 0;
		int ofs_tris = 0;
		int ofs_glcmds = 0;
		int ofs_frames = 0;
		int ofs_end = 0;
		
		void read(DataInputStream data) throws IOException
		{
			//Version
			ident 		= Integer.reverseBytes(data.readInt());
			version 	= Integer.reverseBytes(data.readInt());
			//Texture
			height 		= Integer.reverseBytes(data.readInt());
			width 		= Integer.reverseBytes(data.readInt());
			//Frame
			frameSize 	= Integer.reverseBytes(data.readInt());
			//Nums
			num_skins 	= Integer.reverseBytes(data.readInt());
			num_xyz 	= Integer.reverseBytes(data.readInt());
			num_st 		= Integer.reverseBytes(data.readInt());
			num_tris 	= Integer.reverseBytes(data.readInt());
			num_glcmds 	= Integer.reverseBytes(data.readInt());
			num_frames 	= Integer.reverseBytes(data.readInt());
			//Ofs
			ofs_skins 	= Integer.reverseBytes(data.readInt());
			ofs_st 		= Integer.reverseBytes(data.readInt());
			ofs_tris 	= Integer.reverseBytes(data.readInt());
			ofs_glcmds 	= Integer.reverseBytes(data.readInt());
			ofs_frames 	= Integer.reverseBytes(data.readInt());
			ofs_end 	= Integer.reverseBytes(data.readInt());
		}

		@Override
		public String toString()
		{
			return 	"===MD2 header===\nmagic: " + ident + "\nver: " + version + "\nth: " + height + "\ntw: " + width + "\nnum frames: " + num_frames + 
					"\nnum_triangles: " + num_tris + "\nnum vertices: " + num_xyz + "\nnum texcoords: " + num_st + "\n===MD2 header===";
		}
		
		
	}
	
	class MD2_Skin
	{
		String name = "";
		
		MD2_Skin read(DataInputStream data) throws IOException
		{
			char[] temp = new char[64];
			for (int i = 0; i < temp.length; i++)
			{
				temp[i] = (char) data.readUnsignedByte();
			}
			name = new String(temp);
			
			tex_id = GLUtil.loadPNGTexture(name, GL13.GL_TEXTURE0);
			return this;
		}
	}
	
	/*Texture coords*/
	class MD2_ST
	{
		int s = 0;
		int t = 0;
		
		MD2_ST read(DataInputStream data) throws IOException
		{
			s = Short.reverseBytes((short) data.readUnsignedShort());
			t = Short.reverseBytes((short) data.readUnsignedShort());
			return this;
		}
	}
	
	/*Triangle*/
	class MD2_Triangle
	{
		int  vertexIndex[] = new int[3];
		int textureIndex[] = new int[3];
		
		MD2_Triangle read(DataInputStream data) throws IOException
		{
			for (int i = 0; i < vertexIndex.length; i++)
			{
				vertexIndex[i] = Short.reverseBytes((short) data.readUnsignedShort());
			}
			for (int i = 0; i < textureIndex.length; i++)
			{
				textureIndex[i] = Short.reverseBytes((short) data.readUnsignedShort());
			}
			
			return this;
		}
	}
	
	/*Vertex*/
	class MD2_Vertex
	{
		float[] v = new float[3];
		int lightNormalIndex = 0;
		
		MD2_Vertex read(DataInputStream data, MD2_Frame frame) throws IOException
		{
			for (int i = 0; i < v.length; i++)
			{
				v[i] = data.readUnsignedByte() * frame.scale[i] + frame.translate[i];
			}
			
			lightNormalIndex = data.readUnsignedByte();
			
			return this;
		}
	}
	
	/*Frame*/
	class MD2_Frame
	{
		float scale[] = new float[3];
		float translate[] = new float[3];
		char[] name = new char[16];
		MD2_Vertex[] vertices = new MD2_Vertex[header.num_xyz];
		
		MD2_Frame read(DataInputStream data) throws IOException
		{
			for (int i = 0; i < scale.length; i++)
			{
				scale[i] = Float.intBitsToFloat(Integer.reverseBytes(data.readInt()));
			}
			
			for (int i = 0; i < translate.length; i++)
			{
				translate[i] = Float.intBitsToFloat(Integer.reverseBytes(data.readInt()));
			}
			
			for (int i = 0; i < name.length; i++)
			{
				name[i] = (char) data.readUnsignedByte();
			}
			
			for (int i = 0; i < header.num_xyz; i++)
			{
				vertices[i] = new MD2_Vertex().read(data, this);
			}
			return this;
		}
	}
}

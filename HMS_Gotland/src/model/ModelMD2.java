package model;

import hms_gotland_core.RenderEngine;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Util.ShaderUtils;
import Util.VertexData;


public class ModelMD2 extends Model
{
	public MD2_Header 		header;
	
	public MD2_ST[] 		textureCoords;
	private MD2_Triangle[] 	triangles;
	private MD2_Frame[] 	frames;
	private MD2_Skin[] 		skins;
	
	private int vao_id = GL30.glGenVertexArrays();
	private int[] frame_ids;

	private int shader_id;
	
	public void draw()
	{
		System.err.println("MD2 requires frame data! How many times have I done this now!?");
	}
	
	public void draw(float frame)
	{
		if(frame < 0 || frame > header.num_frames - 1) return;
			
		//Select current frame and next
		int frame_0 = frame_ids[(int) Math.floor(frame)];
		int frame_1 = frame_ids[(int)  Math.ceil(frame)];
		//Upload frame interpolation
		ShaderUtils.setUniformVar(shader_id, "frame_interpolated", (float)(frame - Math.floor(frame)));
		
		GL30.glBindVertexArray(vao_id);
		{
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, frame_0);//Bind frame 0
			{
				GL20.glVertexAttribPointer(0, VertexData.positionElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.positionByteOffset);
				GL20.glVertexAttribPointer(1, VertexData.textureElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.textureByteOffset);
				GL20.glVertexAttribPointer(2, VertexData.normalElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.normalByteOffset);
			}
			
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
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, header.num_tris);
			}
			for(int i = 0; i < 6; i++){ GL20.glDisableVertexAttribArray(i); }
			
		}
		GL30.glBindVertexArray(0);
		
		//TODO
		
	}
	
	public void compileVBO()
	{
		ArrayList<VertexData> data = new ArrayList<>();
		for(int k = 0; k < header.num_frames; k++)
		{
			MD2_Frame frame = frames[k];// Current frame
			for (int i = 0; i < header.num_tris; i++)
			{
				for (int j = 0; j < 3; j++)
				{
					VertexData temp = new VertexData();
					//Extract position from vertex array using triangles vertex index
					MD2_Vertex vertex = frame.vertices[triangles[i].vertexIndex[j]];
					temp.setXYZ(vertex.v[0], vertex.v[1], vertex.v[2]);
					
					//Extract texture coords from st array using triangle texture index
					float s = textureCoords[triangles[i].textureIndex[j]].s / header.width;
					float t = textureCoords[triangles[i].textureIndex[j]].t / header.height;
					temp.setST(s, t);
					
					//TODO normals
					//
					
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
			frame_ids[k] = id;
		}
	}
	
	public void read(File file)
	{
		try
		{
			RandomAccessFile data = new RandomAccessFile(file, null);
			////////Read header////////
			header = new MD2_Header();
			header.read(data);
			
			//Allocate frame ids
			frame_ids = new int[header.num_frames];
			
			if(header.ident != (('2'<<24) + ('P'<<16) + ('D'<<8) + 'I') || header.version != 8)
				throw new IllegalArgumentException("Invalid MD2 version!");
			
		    ////////Read skins////////
			data.seek(header.ofs_skins);
			
			skins = new MD2_Skin[header.num_skins];
			for(int i = 0; i < header.num_skins; i++)
			{
				skins[i] = new MD2_Skin().read(data);
			}
			
			////////Read st////////
			data.seek(header.ofs_st);
			
			textureCoords = new MD2_ST[header.num_st];
			for(int i = 0; i < header.num_st; i++)
			{
				textureCoords[i] = new MD2_ST().read(data);
			}
			
			//Read triangles////////
			data.seek(header.ofs_tris);
			
			triangles = new MD2_Triangle[header.num_tris];
			for(int i = 0; i < header.num_tris; i++)
			{
				triangles[i] = new MD2_Triangle().read(data);
			}
			
			////////Read frames////////
			data.seek(header.ofs_frames);
			
			frames = new MD2_Frame[header.num_frames];
			for(int i = 0; i < header.num_frames; i++)
			{
				frames[i] = new MD2_Frame().read(data);
			}
			
			////////Read OpenGL cmds////////
			//TODO implement replacement using OpenGL 3
			
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
		
		void read(RandomAccessFile data) throws IOException
		{
			//Version
			ident 		= data.readInt();
			version 	= data.readInt();
			//Texture
			height 		= data.readInt();
			width 		= data.readInt();
			//Frame
			frameSize 	= data.readInt();
			//Nums
			num_skins 	= data.readInt();
			num_xyz 	= data.readInt();
			num_st 		= data.readInt();
			num_tris 	= data.readInt();
			num_glcmds 	= data.readInt();
			num_frames 	= data.readInt();
			//Ofs
			ofs_skins 	= data.readInt();
			ofs_st 		= data.readInt();
			ofs_tris 	= data.readInt();
			ofs_glcmds 	= data.readInt();
			ofs_frames 	= data.readInt();
			ofs_end 	= data.readInt();
		}
	}
	
	class MD2_Skin
	{
		String name = "";
		
		MD2_Skin read(RandomAccessFile data) throws IOException
		{
			char[] temp = new char[64];
			for (int i = 0; i < 64; i++)
			{
				temp[i] = data.readChar();
			}
			name = new String(temp);
			return this;
		}
	}
	
	/*Texture coords*/
	class MD2_ST
	{
		float s = 0;
		float t = 0;
		
		MD2_ST read(RandomAccessFile data) throws IOException
		{
			s = data.readFloat();
			t = data.readFloat();
			return this;
		}
	}
	
	/*Triangle*/
	class MD2_Triangle
	{
		short  vertexIndex[] = new short[3];
		short textureIndex[] = new short[3];
		
		MD2_Triangle read(RandomAccessFile data) throws IOException
		{
			for (int i = 0; i < vertexIndex.length; i++)
			{
				vertexIndex[i] = data.readShort();
			}
			for (int i = 0; i < textureIndex.length; i++)
			{
				textureIndex[i] = data.readShort();
			}
			
			return this;
		}
	}
	
	/*Vertex*/
	class MD2_Vertex
	{
		float[] v = new float[3];
		char lightNormalIndex = 0;
		
		MD2_Vertex read(RandomAccessFile data, MD2_Frame frame) throws IOException
		{
			for (int i = 0; i < v.length; i++)
			{
				v[i] = data.readChar() * frame.scale[i] + frame.translate[i];
			}
			
			lightNormalIndex = data.readChar();
			
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
		
		MD2_Frame read(RandomAccessFile data) throws IOException
		{
			for (int i = 0; i < scale.length; i++)
			{
				scale[i] = data.readFloat();
			}
			
			for (int i = 0; i < translate.length; i++)
			{
				translate[i] = data.readFloat();
			}
			
			for (int i = 0; i < name.length; i++)
			{
				name[i] = data.readChar();
			}
			
			for (int i = 0; i < header.num_xyz; i++)
			{
				vertices[i] = new MD2_Vertex().read(data, this);
			}
			return this;
		}
	}
}

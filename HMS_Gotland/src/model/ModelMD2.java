package model;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class ModelMD2 extends Model
{
	public MD2_Header 		header;
	
	public MD2_ST[] 		st;
	private MD2_Triangle[] 	triangles;
	private MD2_Frame[] 	frames;
	private MD2_Skin[] 		skins;
	
	public void draw()
	{
		System.err.println("MD2 requires frame data! How many times have I done this now!?");
	}
	
	public void draw(float frame)
	{
		
	}
	
	public void read(File file)
	{
		try
		{
			RandomAccessFile data = new RandomAccessFile(file, null);
			////////Read header////////
			header = new MD2_Header();
			header.read(data);
			
		    ////////Read skins////////
			data.seek(header.ofs_skins);
			
			skins = new MD2_Skin[header.num_skins];
			for(int i = 0; i < header.num_skins; i++)
			{
				skins[i] = new MD2_Skin().read(data);
			}
			
			////////Read st////////
			data.seek(header.ofs_st);
			
			st = new MD2_ST[header.num_st];
			for(int i = 0; i < header.num_st; i++)
			{
				st[i] = new MD2_ST().read(data);
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

package Util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;



public class GLUtil
{
	/**
	 * Prints the OGL error in terminal.
	 * OGL error(&error string) at &string
	 * @param s
	 */
	public static void cerror(String s)
	{
		int i = GL11.glGetError();
		if(i != GL11.GL_NO_ERROR)
			System.err.println("OpenGL error(" + GLU.gluErrorString(i)+ ") at " + s);
	}
	
	public static int getGLMaxVersion()
	{
		try
		{
			Pbuffer tmp = new Pbuffer(64, 64, new PixelFormat(), null);
			tmp.makeCurrent();
			int i = GL11.glGetInteger(GL11.GL_VERSION);
			tmp.destroy();
			return i;
		} catch (LWJGLException e1)
		{
			e1.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * 
	 */
	public static int getMaxSamplings()
	{
		try
		{
			Pbuffer tmp = new Pbuffer(64, 64, new PixelFormat(), null);
			tmp.makeCurrent();
			int i = GL11.glGetInteger(GL30.GL_MAX_SAMPLES);
			tmp.destroy();
			return i;
		} catch (LWJGLException e1)
		{
			e1.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * @param filename, path to png texture
	 * @param textureUnit, example GL_TEXTURE0
	 * @return texture id
	 */
	public static Texture loadPNGTextureStruct(String filename, int textureUnit)
	{
		ByteBuffer buf = null;
		int tWidth = 0;
		int tHeight = 0;
		
		int texId;
		
		Texture t = new Texture();
		
		try
		{
			// Open the PNG file as an InputStream
			InputStream in = new FileInputStream(filename);
			// Link the PNG decoder to this stream
			PNGDecoder decoder = new PNGDecoder(in);

			// Get the width and height of the texture
			tWidth = decoder.getWidth();
			tHeight = decoder.getHeight();
			
			t.width = tWidth;
			t.height = tHeight;
			
			// Decode the PNG file in a ByteBuffer
			buf = ByteBuffer.allocateDirect(4 * decoder.getWidth()
					* decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();

			in.close();
			texId = GL11.glGenTextures();
		} catch (IOException e)
		{
			System.err.println("Failed to load texture: " + e.getMessage());
			return null;
		}

		// Create a new texture object in memory and bind it
		
		GL13.glActiveTexture(textureUnit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

		// All RGB bytes are aligned to each other and each component is 1 byte
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		// Upload the texture data and generate mip maps (for scaling)
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, tWidth, tHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

		// Setup the ST coordinate system
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		// Setup what to do when the texture has to be scaled
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);

		t.texture_id = texId;
		
		return t;
	}
	
	/**
	 * @param filename, path to png texture
	 * @param textureUnit, example GL_TEXTURE0
	 * @return texture id
	 */
	public static int loadPNGTexture(String filename, int textureUnit)
	{
		ByteBuffer buf = null;
		int tWidth = 0;
		int tHeight = 0;
		
		int texId;
		
		try
		{
			// Open the PNG file as an InputStream
			InputStream in = new FileInputStream(filename);
			// Link the PNG decoder to this stream
			PNGDecoder decoder = new PNGDecoder(in);

			// Get the width and height of the texture
			tWidth = decoder.getWidth();
			tHeight = decoder.getHeight();

			// Decode the PNG file in a ByteBuffer
			buf = ByteBuffer.allocateDirect(4 * decoder.getWidth()
					* decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();

			in.close();
			texId = GL11.glGenTextures();
		} catch (IOException e)
		{
			System.err.println("Failed to load texture: " + e.getMessage());
			return 0;
		}

		// Create a new texture object in memory and bind it
		
		GL13.glActiveTexture(textureUnit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

		// All RGB bytes are aligned to each other and each component is 1 byte
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		// Upload the texture data and generate mip maps (for scaling)
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, tWidth, tHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

		// Setup the ST coordinate system
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		// Setup what to do when the texture has to be scaled
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);

		return texId;
	}
	
	
	
	/**
	 * Creates and flips an buffer with the specified values
	 * @param values
	 * @return A buffer
	 */
	public static FloatBuffer buffer(float... values)
	{
		FloatBuffer temp = BufferUtils.createFloatBuffer(values.length);
		temp.put(values, 0, values.length);
		temp.flip();
		return temp;
	}
	
	/**
	 * Creates and flips an buffer with the specified values
	 * @param values
	 * @return A buffer
	 */
	public static IntBuffer buffer(int... values)
	{
		IntBuffer temp = BufferUtils.createIntBuffer(values.length);
		temp.put(values);
		temp.flip();
		return temp;
	}
	
	/**
	 * Creates and flips an buffer with the specified values
	 * @param values
	 * @return A buffer
	 */
	public static ByteBuffer buffer(byte... values)
	{
		ByteBuffer temp = BufferUtils.createByteBuffer(values.length);
		temp.put(values);
		temp.flip();
		return temp;
	}
	
	/**
	 * Sigh...
	 * @param from
	 * @return Vecmath vector from lwjgl vector
	 */
	public static javax.vecmath.Matrix4f jBulletMatrix(org.lwjgl.util.vector.Matrix4f from)
	{
		return new javax.vecmath.Matrix4f(from.m00, from.m01, from.m02, from.m03, 
										  from.m10, from.m11, from.m12, from.m13, 
										  from.m20, from.m21, from.m22, from.m23, 
										  from.m30, from.m31, from.m32, from.m33);
	}
	
	/**
	 * Sigh...
	 * @param from
	 * @return Lwjgl vector from vecmath vector
	 */
	public static org.lwjgl.util.vector.Matrix4f lwjglMatrix(javax.vecmath.Matrix4f from)
	{
		//return new org.lwjgl.util.vector.Matrix4f(from); //Of course...
		org.lwjgl.util.vector.Matrix4f retur = new org.lwjgl.util.vector.Matrix4f();
		//FUCK!
		retur.m00 = from.m00; retur.m01 = from.m01; retur.m02 = from.m02; retur.m03 = from.m03;
		retur.m10 = from.m10; retur.m11 = from.m11; retur.m12 = from.m12; retur.m13 = from.m13;
		retur.m20 = from.m20; retur.m21 = from.m21; retur.m22 = from.m22; retur.m23 = from.m23;
		retur.m30 = from.m30; retur.m31 = from.m31; retur.m32 = from.m32; retur.m33 = from.m33;
		return retur;
	}
	
	public static org.lwjgl.util.vector.Matrix4f lwjglMatrix(float... from)
	{
		if(from.length < 16) return null;
		//return new org.lwjgl.util.vector.Matrix4f(from); //Of course...
		org.lwjgl.util.vector.Matrix4f retur = new org.lwjgl.util.vector.Matrix4f();
		//FUCK!
		retur.m00 = from[0]; retur.m01 = from[4]; retur.m02 = from[8]; retur.m03 = from[12];
		retur.m10 = from[1]; retur.m11 = from[5]; retur.m12 = from[9]; retur.m13 = from[13];
		retur.m20 = from[2]; retur.m21 = from[6]; retur.m22 = from[10]; retur.m23 = from[14];
		retur.m30 = from[3]; retur.m31 = from[7]; retur.m32 = from[11]; retur.m33 = from[15];
		return retur;
	}

	static class Texture
	{
		public int texture_id = 0;
		
		public int height;
		public int width;
	}
	
}

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
	private static final boolean DEBUG = Boolean.valueOf(System.getProperty("hms_gotland.debug"));
			
	static
	{
		if(DEBUG) System.out.println("###Debug enabled###");
	}
	/**
	 * Prints the OGL error in terminal.
	 * OGL error(&error string) at &string
	 * @param s
	 */
	public static void cerror(String s)
	{
		if(DEBUG)
		{
			int i = GL11.glGetError();
			if(i != GL11.GL_NO_ERROR)
				System.err.println("OpenGL error(" + GLU.gluErrorString(i)+ ") at " + s);
		}
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
		int i = 0;
		try
		{
			Pbuffer tmp = new Pbuffer(64, 64, new PixelFormat(), null);
			tmp.makeCurrent();
			i = GL11.glGetInteger(GL30.GL_MAX_SAMPLES);
			tmp.destroy();
		} catch (LWJGLException e1)
		{
			e1.printStackTrace();
		}
		return i;
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
		retur.m00 = from.m00; retur.m01 = from.m10; retur.m02 = from.m20; retur.m03 = from.m30;
		retur.m10 = from.m01; retur.m11 = from.m11; retur.m12 = from.m21; retur.m13 = from.m31;
		retur.m20 = from.m02; retur.m21 = from.m12; retur.m22 = from.m22; retur.m23 = from.m32;
		retur.m30 = from.m03; retur.m31 = from.m13; retur.m32 = from.m23; retur.m33 = from.m33;
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

}

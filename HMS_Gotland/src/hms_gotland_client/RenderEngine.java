package hms_gotland_client;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import model.Model;
import model.ModelPool;

import Util.GLUtil;

public class RenderEngine
{	
	public Camera camera;
	
	public static int  VERTEX_ATTRIB_POINTER = 0;
	public static int TEXTURE_ATTRIB_POINTER = 1;
	public static int  NORMAL_ATTRIB_POINTER = 2;
	
	public ModelPool modelpool = new ModelPool();
	
	
	public RenderEngine(int width, int height)
	{
		try 
		{
			PixelFormat pixelFormat = new PixelFormat(24, 8, 8, 0, GLUtil.getMaxSamplings());
			ContextAttribs contextAtrributes = new ContextAttribs(3, 2);
			contextAtrributes.withForwardCompatible(true);
			contextAtrributes.withProfileCore(true);
			
			Display.setDisplayMode(new DisplayMode(width, height));
			//Display.setTitle(WINDOW_TITLE);
			Display.create(pixelFormat, contextAtrributes);
			
			GL11.glViewport(0, 0, width, height);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		camera = new Camera(width, height, 0.1f, 1000);
		
		GL11.glViewport(0, 0, width, height);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_BLEND); 
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	public float[] getViewProjectionMatrix()
	{
		return camera.getViewProjectionMatrix();
	}
	
	protected void tick()
	{
		modelpool.destroyUnused();
	}
	
	public Model getModel(String name)
	{
		return modelpool.getModel(name);
	}
	
	
	
}

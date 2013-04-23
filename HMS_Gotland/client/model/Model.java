package model;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Vector3f;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.GLSync;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.SharedDrawable;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;

import hms_gotland_client.RenderEngine;

public class Model
{	
	protected RenderEngine renderer;
	protected GLSync sync;
	protected ReentrantLock lock  = new ReentrantLock();
	protected boolean ready = false;
	
	public Model(RenderEngine rend, File resource)
	{
		//backgroundLoad(true, resource);
		renderer = rend;
	}
	
	public void draw(float frame, float[] vpMatrix, float[] mdMatrix, RenderEngine engine)
	{
		
	}
	
	public void backgroundLoad(final boolean drawable, final File file)
	{
		Thread t = new Thread("Model-backgroundloader(" + file.getName() + ")")
		{
			public void run() {
				System.out.println("Loading " + file.getName() + "...");
				
				try {
					Drawable draw = null;
					if(drawable)
					{
						draw = new SharedDrawable(Display.getDrawable());
					}else
					{
						draw = new Pbuffer(2, 2, new PixelFormat(8, 24, 0), Display.getDrawable());
					}
					draw.makeCurrent();
					lock.lock();
					boolean fenceSync = GLContext.getCapabilities().OpenGL32;
					if(fenceSync)
					{
						sync = GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
					}else
					{
						GL11.glFlush();
					}
					read(file);
					compile();
					if(fenceSync)
					{
						sync = GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
					}else
					{
						GL11.glFlush();
					}
					lock.unlock();
					draw.destroy();
				} catch (LWJGLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Done loading" + file.getName());
				ready = true;
			}
		};
		t.setDaemon(true);
		t.start();
		
		
	}
	
	protected void compile() {
		// TODO Auto-generated method stub
		
	}

	protected void read(File file) {
	}

	public void destroy()
	{
	}
	
	public float getXWidth()
	{
		return 1;
	}
	
	public float getYHeight()
	{
		return 1;
	}
	
	public float getZDepth()
	{
		return 1;
	}

	//Default model isn't animated
	public boolean isAnimated()
	{
		return false;
	}

	public CollisionShape body()
	{	
		float width = (float) Math.sqrt(getXWidth() * getXWidth() + getZDepth() * getZDepth());
		return new CapsuleShape(width / 2, getYHeight());
	}
}

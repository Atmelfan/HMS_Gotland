package hms_gotland_client;

import java.io.File;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import level.Level;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

import entity.EntityList;
import entity.EntityPlayer;

import Util.GLUtil;
import Util.OSUtil;

public class HMS_Gotland
{
	public static void main(String[] args) 
	{
		new HMS_Gotland();
	}
	
	// Setup variables
	private final String WINDOW_TITLE = "HMS Gotland - fps: ";
	private final int WIDTH = 1024;
	private final int HEIGHT = 480;
	// Texture variables
	private int[] texIds = new int[] {0, 0};
	private int textureSelector = 0;
	// Moving variables
	
	long lastFrame = 0;
	private int fps;
	private int currentfps;
	
	private Level level;
	public RenderEngine renderEngine;
	
	private long lastTick;
	private Wardos wardos;
	
	public HMS_Gotland() 
	{
		System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + File.separator + "Resources" + File.separator + "native" + File.separator + OSUtil.getOS());
		run();
	}
	
	public void run()
	{
		renderEngine = new RenderEngine(WIDTH, HEIGHT);
		System.out.println("==========================INFO==========================");
		System.out.println("Operating system: " + System.getProperty("os.name"));
		System.out.println("System architecture: "  + System.getProperty("os.arch"));
		System.out.println("GPU vendor: " + GL11.glGetString(GL11.GL_VENDOR));
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		System.out.println("Shader version: " + GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
		System.out.println("LWJGL version: "  + Sys.getVersion());
		System.out.println("Tesselation: " + GLContext.getCapabilities().GL_ARB_tessellation_shader);
		System.out.println("==========================INFO==========================");
		
		level = new Level("test", this);
		GLUtil.cerror(getClass().getName() + " level load");
		
		setupTextures();
		GLUtil.cerror(getClass().getName() + " texture load");
		wardos = new Wardos();
		
		renderEngine.camera.owner = getPlayer();
		
		lastTick = lastFrame = Sys.getTime();
		
		while (!Display.isCloseRequested()) 
		{
			currentfps++;
			if(Sys.getTime() - lastTick >= 16)
			{
				level.tick();
				lastTick = Sys.getTime();
			}
			if(Sys.getTime() - lastFrame >= 1000)
			{
				lastFrame = Sys.getTime();
				fps = currentfps;
				currentfps = 0;
				Display.setTitle(WINDOW_TITLE + fps);
			}
			
			loopCycle();
			
			int i = GL11.glGetError();
			if(i != GL11.GL_NO_ERROR)
			{
				System.out.println("##### GL error: " + GLU.gluErrorString(i) + " #####");
			}
			
		}
		
		// Destroy OpenGL (Display)
		destroyOpenGL();
	}

	private void setupTextures() {
		texIds[0] = GLUtil.loadPNGTexture("Resources/assets/asset1.png", GL13.GL_TEXTURE0);
		texIds[1] = GLUtil.loadPNGTexture("Resources/assets/asset2.png", GL13.GL_TEXTURE0);
	}
	
	private void logicCycle() 
	{
		float distance = 2f;
		
		while(Mouse.next())
		{
			if(!Mouse.getEventButtonState()) continue;
			
			if(Mouse.getEventButton() == 0 && !Mouse.isGrabbed())
			{
				Mouse.setGrabbed(true);
			}
		}
		
		if(Mouse.isGrabbed())
		{
			renderEngine.camera.yaw -= Mouse.getDX();
			renderEngine.camera.pitch -= Mouse.getDY();
			renderEngine.camera.pitch = Math.max(renderEngine.camera.pitch, 45);
			renderEngine.camera.pitch = Math.min(renderEngine.camera.pitch, 225);
		}
		
		//Movement translation
		float yaw = renderEngine.camera.yaw;
		float xt = (float) (distance * Math.sin(Math.toRadians(yaw)));
		float yt = (float) (distance * Math.sin(Math.toRadians(yaw)));
		
		while(Keyboard.next()) 
		{			
			// Only listen to events where the key was pressed (down event)
			if (!Keyboard.getEventKeyState()) continue;
			
			// Switch textures depending on the key released
			switch (Keyboard.getEventKey()) 
			{
			case Keyboard.KEY_1:
				textureSelector = 0;
				break;
			case Keyboard.KEY_2:
				textureSelector = 1;
				break;
				/*
				 * Game control keys
				 */
			case Keyboard.KEY_ESCAPE:
				Mouse.setGrabbed(false);
				break;
			case Keyboard.KEY_F5:
				level.reloadLevel();
				break;
				/*
				 * Movement keys
				 */
			case Keyboard.KEY_Q:
				level.addEntity(EntityList.getEntity("default", level, getPlayer().getPos()));
				break;
			case Keyboard.KEY_W:
				getPlayer().move(new Vector3f(xt, 0f, -yt));
				break;
			case Keyboard.KEY_S:
				getPlayer().move(new Vector3f(-xt, 0f, yt));
				break;
			case Keyboard.KEY_A:
				getPlayer().move(new Vector3f((float) (distance * Math.sin(Math.toRadians(yaw - 90))), 0f, 
						(float) -(distance * Math.sin(Math.toRadians(yaw - 90)))));
				break;
			case Keyboard.KEY_D:
				getPlayer().move(new Vector3f((float) -(distance * Math.sin(Math.toRadians(yaw + 90))), 0f, 
						(float) (distance * Math.sin(Math.toRadians(yaw + 90)))));
				break;
				
				/*
				 * Character control keys
				 */
			case Keyboard.KEY_SPACE:
				getPlayer().move(new Vector3f(0, distance, 0));
				break;
			}
			
		}
		
		renderEngine.camera.update();
	}
	
	private void renderCycle() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texIds[textureSelector]);
		
		level.draw();
		wardos._render();
	}
	
	private void loopCycle() 
	{
		// Update logic
		logicCycle();
		// Update rendered frame
		renderCycle();
		Display.update();
	}
	
	private void destroyOpenGL() {	
		// Delete the texture
		GL11.glDeleteTextures(texIds[0]);
		GL11.glDeleteTextures(texIds[1]);
		level.destroy();
		
		Display.destroy();
	}

	/**
	 * @return the player
	 */
	public EntityPlayer getPlayer()
	{
		return level.player;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(EntityPlayer player)
	{
		level.player = player;
	}

}

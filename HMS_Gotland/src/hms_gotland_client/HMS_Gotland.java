package hms_gotland_client;

import hms_gotland_server.HMS_Gotland_Server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.vecmath.Vector3f;

import level.EntityList;
import level.EntityPlayer;
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


import Util.GLUtil;
import Util.OSUtil;

public class HMS_Gotland
{
	/* Game client
	 * Everything else is the server(except Utils)
	 * 
	 * 
	 */
	public static void main(String[] args) 
	{
		new HMS_Gotland();
	}
	
	// Setup variables
	private final String WINDOW_TITLE = "HMS Gotland - fps: ";
	private final int WIDTH = 1024;
	private final int HEIGHT = 512;
	// Moving variables
	
	long lastFrame = 0;
	private int fps;
	private int currentfps;
	
	private ClientLevel level;
	public RenderEngine renderEngine;

	private ServerInterface serverInterface;
	private HMS_Gotland_Server server;
	
	private Wardos wardos;
	private int texId;
	
	public HMS_Gotland() 
	{
		System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + File.separator + "Resources" + File.separator + "native" + File.separator + OSUtil.getOS());
		run();
	}
	
	public void run()
	{
		try
		{
			renderEngine = new RenderEngine(this, WIDTH, HEIGHT);
			System.out.println("==========================INFO==========================");
			System.out.println("Operating system: " + System.getProperty("os.name"));
			System.out.println("System architecture: "  + System.getProperty("os.arch"));
			System.out.println("GPU vendor: " + GL11.glGetString(GL11.GL_VENDOR));
			System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
			System.out.println("Shader version: " + GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
			System.out.println("LWJGL version: "  + Sys.getVersion());
			System.out.println("==========================INFO==========================");
			
			server = new HMS_Gotland_Server(4321);
			serverInterface = new ServerInterface(new Socket("127.0.0.1", 4321));
			
			level = new ClientLevel(this);
			GLUtil.cerror(getClass().getName() + " level load");
			
			setupTextures();
			
			wardos = new Wardos(this);
			renderEngine.camera.owner = getPlayer();
			lastFrame = Sys.getTime();
			
			while (!Display.isCloseRequested()) 
			{
				updateTiming();
				
				serverInterface.tick();
				level.tick();
				loopCycle();
				
				int i = GL11.glGetError();
				if(i != GL11.GL_NO_ERROR)
				{
					System.out.println("##### GL error: " + GLU.gluErrorString(i) + " #####");
				}
				
			}
		} catch (Exception e)
		{
			System.err.println("Error: HMS_Gotland.run() - " + e.getMessage());
		}finally
		{
			// Destroy OpenGL (Display)
			System.out.println("Shutting down...");
			destroyOpenGL();
			serverInterface.terminate();
			
			if(server != null)server.terminate();
		}
	}

	private void updateTiming()
	{
		currentfps++;
		if(Sys.getTime() - lastFrame >= 1000)
		{
			lastFrame = Sys.getTime();
			fps = currentfps;
			currentfps = 0;
			Display.setTitle(WINDOW_TITLE + fps);
		}
	}

	private void setupTextures() {
		texId = GLUtil.loadPNGTexture("Resources/assets/asset1.png", GL13.GL_TEXTURE0);
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
				/*
				 * Game control keys
				 */
			case Keyboard.KEY_ESCAPE:
				Mouse.setGrabbed(false);
				break;
				/*
				 * Movement keys
				 */
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
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		
		renderEngine.drawStarField();
		level.draw();
		wardos.draw();
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
		GL11.glDeleteTextures(texId);
		level.destroy();
		
		Display.destroy();
	}

	/**
	 * @return the player
	 */
	public ClientPlayer getPlayer()
	{
		return level.player;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(ClientPlayer player)
	{
		level.player = player;
	}

}

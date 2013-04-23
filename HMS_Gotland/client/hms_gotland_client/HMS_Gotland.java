package hms_gotland_client;

import java.io.File;

import level.BaseLevel;
import level.LevelSingleplayer;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Util.OSUtil;
import Util.ScreenShot;

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
	
	private BaseLevel level;
	public RenderEngine renderEngine;

	private ResourceManager resources;
	
	private Wardos wardos;
	private boolean running = true;
	public boolean playing;
	private long lastTick;
	private SoundEngine soundEngine;
	
	public HMS_Gotland() 
	{
		System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + File.separator + "Resources" + File.separator + "native" + File.separator + OSUtil.getOS());
		run();
	}
	
	public void run()
	{
		
		try
		{
			Thread.currentThread().setName("HMS_Gotland_Client");
			resources = new ResourceManager(this);
			renderEngine = new RenderEngine(this, WIDTH, HEIGHT);
			soundEngine = new SoundEngine();
			wardos = new Wardos(this);
			lastFrame = Sys.getTime();
			printInfo();
			//wardos.playMovie("generic://test.avi");
			SoundSource hydro = soundEngine.getNewSource("2422__andrew-duke__dirt.wav");
			hydro.setLooping(true);
			hydro.setVolume(0.15f);
			//hydro.play();
			
			while (running) 
			{
				running = !Display.isCloseRequested();
				
				updateTiming();
				input();
				renderEngine.render(level);
				Display.update();
			}
		} catch (Exception e)
		{
			System.err.println("Error: HMS_Gotland.run() - " + e.getMessage());
			e.printStackTrace();
			Sys.alert("HMS_Gotland error", "Unexpected error:\n" + e.getLocalizedMessage());
		}finally
		{
			System.out.println("Shutting down...");
			quitGame();
			wardos.stopMovie();
			renderEngine.destroy();
			soundEngine.destroy();
		}
	}

	private void tick()
	{
		renderEngine.tick();
		if(getLevel() != null)
		{
			getLevel().tick();
		}else{
			renderEngine.camera.yaw += 0.05f;
			renderEngine.camera.pitch += 0.03f;
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
		if(Sys.getTime() - lastTick >= 16)
		{
			lastTick = Sys.getTime();
			tick();
		}
	}

	private void input() 
	{
		
		while(Mouse.next())
		{
			if(!Mouse.getEventButtonState()) continue;
			
			if(Mouse.getEventButton() == 0 && !Mouse.isGrabbed())
			{
				Mouse.setGrabbed(true);
			}
		}
		
		if(Mouse.isGrabbed() && playing)
		{
			renderEngine.camera.pitch += Mouse.getDY();
			renderEngine.camera.pitch = clamp(renderEngine.camera.pitch, -35, 35);
			renderEngine.camera.yaw += Mouse.getDX();
			if(level != null && level.getPlayer() != null)
			{
				level.getPlayer().move(-renderEngine.camera.yaw);
			}
		}
		
		if(level != null && level.getPlayer() != null)
		{
			soundEngine.setPosition(level.getPlayer().getPos());
			soundEngine.setVelocity(level.getPlayer().getVel());
		}
		
		while(Keyboard.next()) 
		{			
			// Only listen to events where the key was pressed (down event)
			if (!Keyboard.getEventKeyState()) continue;
			
			// Switch textures depending on the key released
			int key = Keyboard.getEventKey();
			switch(key) 
			{
				/*
				 * Game control keys
				 */
			case Keyboard.KEY_ESCAPE:
				if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
				{
					if(playing){
						quitGame();
					}else{
						running = false;
					}
				}
				Mouse.setGrabbed(false);
				break;
			case Keyboard.KEY_F1:
				level = new LevelSingleplayer();
				level.init(new File("Servers/singleplayer/"));
				playing = true;
				break;
			case Keyboard.KEY_F2:
				renderEngine.setFullScreen(!Display.isFullscreen());
				break;
			case Keyboard.KEY_F3:
				ScreenShot.takeScreenShot();
				break;
				/*
				 * Movement keys
				 */
			default:
				break;
			}
			
		}
		
		renderEngine.camera.update();
	}
	
	public void quitGame()
	{
		playing = false;
		if(level != null)
		{
			level.destroy();
			level = null;
		}
	}
	
	private void printInfo()
	{
		System.out.println("==========================INFO==========================");
		System.out.println("Operating system: " + System.getProperty("os.name"));
		System.out.println("System architecture: "  + System.getProperty("os.arch"));
		System.out.println("GPU vendor: " + GL11.glGetString(GL11.GL_VENDOR));
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		System.out.println("Shader version: " + GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
		System.out.println("LWJGL version: "  + Sys.getVersion());
		System.out.println("MSAA Antialias: " + GL11.glGetInteger(GL30.GL_MAX_SAMPLES) + "x");
		System.out.println("CPU cores: " + Runtime.getRuntime().availableProcessors());
		System.out.println("==========================INFO==========================");
	}

	/**
	 * @return the level
	 */
	public BaseLevel getLevel()
	{
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(BaseLevel level)
	{
		this.level = level;
	}
	
	public static float clamp (float i, float low, float high) 
	{
	    return Math.max(Math.min (i, high), low);
	  }

	public RenderEngine getRenderEngine()
	{
		return renderEngine;
	}

	public ResourceManager getResourceManager()
	{
		return resources;
	}

}

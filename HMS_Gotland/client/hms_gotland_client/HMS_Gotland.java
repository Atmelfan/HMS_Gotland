package hms_gotland_client;

import hms_gotland_server.HMS_Gotland_Server;
import hms_gotland_server.Packet;

import java.io.File;
import java.io.IOException;

import level.ClientLevel;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;


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

	private Client client;
	private HMS_Gotland_Server server;
	private ResourceManager resources;
	
	private Wardos wardos;
	private boolean running = true;
	public boolean playing;
	private long lastTick;
	private Server connectedServer;
	private SoundManager soundEngine;
	
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
			soundEngine = new SoundManager();
			wardos = new Wardos(this);
			lastFrame = Sys.getTime();
			printInfo();
			//wardos.playMovie("generic://test.avi");
			SoundSource hydro = soundEngine.getNewSource("2422__andrew-duke__dirt.wav");
			hydro.setLooping(true);
			hydro.setVolume(0.15f);
			hydro.play();
			
			while (running) 
			{
				running = !Display.isCloseRequested();
				
				updateTiming();
				input();
				
				render();
				//Display.sync(60);
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
		}
		System.out.println("Shutting down...");
	}

	private void tick()
	{
		renderEngine.tick();
		if(!updateGame())
		{
			renderEngine.camera.yaw += 0.05f;
			renderEngine.camera.pitch += 0.03f;
		}
	}
	

	private boolean updateGame()
	{
		if(getLevel() != null)
		{
			getLevel().tick();
			return true;
		}
		return false;
	}
	
	public void startGame(Server server) throws IOException
	{
		if(playing) return;
		
		playing = true;
		//Create new level
		setLevel(new ClientLevel(this));
		//Create server connection
		client = new Client();
		Packet.registerPackets(client.getKryo());
		client.addListener(listener);
		client.start();
		//Connect
		server.connect(client);
		connectedServer = server;
	}
	
	public void quitGame()
	{
		playing = false;
		renderEngine.camera.owner = null;
		if(level != null)
		{
			//Stop and clean up if available
			level.destroy();
			level = null;
		}
		if(connectedServer != null)
		{
			connectedServer.disconnect(client);
		}
		if(client != null)
		{
			//Stop and clean up if available
			client.stop();
			client.close();
			client = null;
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
			if(level != null && level.player != null)
			{
				level.player.move(-renderEngine.camera.yaw);
			}
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
				Mouse.setGrabbed(false);
				break;
			case Keyboard.KEY_F1:
				try
				{
					startGame(resources.getServers()[0]);
				} catch (IOException e)
				{
					System.err.println("Error: HMS_Gotland.input() - " + e.getMessage());
				}
				break;
				/*
				 * Movement keys
				 */
			default:
				if(level != null && level.player != null)
				{
					level.player.input(key, renderEngine.camera.yaw);
				}
			}
			
		}
		
		renderEngine.camera.update();
	}
	
	private void render() 
	{
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		if(!wardos.isPlayingMovie())
		{
			renderEngine.drawStarField();
			if(getLevel() != null)
			{
				getLevel().draw();
			}
		}
		wardos.draw();
		GLUtil.cerror("HMS_Gotland.render loop");
	}

	private Listener listener = new Listener()
	{
		@Override
		public void disconnected(Connection connection)
		{
			//quitGame();
			super.disconnected(connection);
		}

		@Override
		public void connected(Connection connection)
		{
			client.sendTCP(new Packet.Login("tester"));
			System.out.println("Logging in...");
			super.connected(connection);
		}
		
		@Override
		public void received(Connection connection, Object object)
		{
			if(object instanceof Packet.AcceptLogin)
			{
				level.setPlayerAndLevel(((Packet.AcceptLogin)object).playerID, ((Packet.AcceptLogin)object).playerPos, ((Packet.AcceptLogin)object).levelName);
			}
			if(object instanceof Packet.Message)
			{
				System.out.println("Server message: " + ((Packet.Message)object).msg);
			}
			
			super.received(connection, object);
		}
		
	};
	
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
		System.out.println("==========================INFO==========================");
	}

	/**
	 * @return the level
	 */
	public ClientLevel getLevel()
	{
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(ClientLevel level)
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

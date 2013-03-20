package hms_gotland_client;

import hms_gotland_server.HMS_Gotland_Server;
import hms_gotland_server.Packet;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.vecmath.Vector3f;

import level.ClientLevel;
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
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

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
	
	private Wardos wardos;
	private boolean running = true;
	public boolean playing;
	private long lastTick;
	
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
			printInfo();
			wardos = new Wardos(this);
			lastFrame = Sys.getTime();
			
			while (running) 
			{
				running = !Display.isCloseRequested();
				
				updateTiming();
				input();
				render();
				Display.update();
				GLUtil.cerror("HMS_Gotland.run-main loop");
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
			destroyOpenGL();
		}
	}

	private void tick()
	{
		if(!updateGame())
		{
			renderEngine.camera.yaw += 0.05f;
			renderEngine.camera.pitch += 0.03f;
		}
		
	}
	

	private boolean updateGame()
	{
		if(level != null)
		{
			level.tick();
			return true;
		}
		return false;
	}
	
	public void startGame(boolean singleplayer, String ip, int tcp, int udp) throws IOException
	{
		playing = true;
		if(singleplayer)
		{
			//Create new level
			level = new ClientLevel(this);
			renderEngine.camera.owner = level.player;
			//Create server
			server = new HMS_Gotland_Server(true, tcp, udp);
			server.start();
			//Create server connection
			client = new Client();
			client.addListener(listener);
			Packet.registerPackets(client.getKryo());
			client.start();
			//Connect
			client.connect(5000, ip, tcp, udp);
			
		}else
		{
			//Create new level
			level = new ClientLevel(this);
			renderEngine.camera.owner = level.player;
			//Create server connection
			client = new Client();
			client.addListener(listener);
			Packet.registerPackets(client.getKryo());
			client.start();
			//Connect
			client.connect(5000, ip, tcp, udp);
		}
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
		if(server != null)
		{
			//Stop and clean up if available
			server.terminate();
			server = null;
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
		
		if(Mouse.isGrabbed())
		{
			renderEngine.camera.yaw -= Mouse.getDX();
			renderEngine.camera.pitch -= Mouse.getDY();
			renderEngine.camera.pitch = Math.max(renderEngine.camera.pitch, 45);
			renderEngine.camera.pitch = Math.min(renderEngine.camera.pitch, 315);
		}
		
		
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
			case Keyboard.KEY_F1:
				try
				{
					startGame(true, "127.0.0.1", 4321, 4322);
				} catch (IOException e)
				{
					System.err.println("Error: HMS_Gotland.input() - " + e.getMessage());
				}
				break;
				/*
				 * Movement keys
				 */
			
			}
			
		}
		
		renderEngine.camera.update();
	}
	
	private void render() 
	{
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		renderEngine.drawStarField();
		if(level != null)
		{
			level.draw();
		}
		wardos.draw();
	}
	
	private void destroyOpenGL()
	{
		Display.destroy();
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
			connection.sendTCP(new Packet.Login("tester"));
			System.out.println("Logging in...");
			super.connected(connection);
		}
		
		@Override
		public void received(Connection connection, Object object)
		{
			if(object instanceof Packet.Login)
			{
				System.out.println("Loading server level: " + ((Packet.Login)object).name);
				level.setLevel(((Packet.Login)object).name);
				GLUtil.cerror(getClass().getName() + " level load");
			}
			super.received(connection, object);
		}
		
	};
	
	private void printInfo()
	{
		renderEngine = new RenderEngine(this, WIDTH, HEIGHT);
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
}

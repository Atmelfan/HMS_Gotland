package hms_gotland_server;

import org.lwjgl.Sys;

import level.Level;

public class HMS_Gotland_Server extends Thread
{

	public boolean terminate = false;
	private static HMS_Gotland_Server server;
	private ConnectionListener clients;
	private long lastTick;
	private Level level;
	private int port;
	
	public static void main(String[] args)
	{
		if(args.length >= 1)
		{
			server = new HMS_Gotland_Server(Integer.parseInt(args[0]));
		}else
		{
			server = new HMS_Gotland_Server(2944);
		}
	}
	
	public HMS_Gotland_Server(int port)
	{
		this.port = port;
		setup();
		clients.connect(port);
		System.out.println("Connected to port: " + port);
		start();
	}

	public void run()
	{
		try
		{
			while(!terminate)
			{
				if(System.currentTimeMillis() - lastTick >= 16)
				{
					level.tick();
					clients.tick();
					lastTick = System.currentTimeMillis();
				}
			}
		} catch (Exception e)
		{
			System.err.println("Critical server error: " + e.getMessage());
		} finally
		{
			clients.terminate();
			level.destroy();
		}
	}

	public void terminate()
	{
		terminate = true;
	}
	
	private void setup()
	{
		lastTick = System.currentTimeMillis();
		level = new Level("test", this);
		clients = new ConnectionListener();
	}

}

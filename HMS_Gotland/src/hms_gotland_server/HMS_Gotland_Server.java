package hms_gotland_server;

import org.lwjgl.Sys;

import level.Level;

public class HMS_Gotland_Server
{

	private static HMS_Gotland_Server server;
	private ClientInterface clients;
	private long lastTick;
	private Level level;
	
	public static void main(String[] args)
	{
		server = new HMS_Gotland_Server();
		server.run();
	}

	private void run()
	{
		setup();
		while(true)
		{
			if(Sys.getTime() - lastTick >= 16)
			{
				level.tick();
				clients.tick();
				lastTick = Sys.getTime();
			}
		}
	}

	private void setup()
	{
		lastTick = Sys.getTime();
		level = new Level("test", this);
		clients = new ClientInterface();
		
	}

}

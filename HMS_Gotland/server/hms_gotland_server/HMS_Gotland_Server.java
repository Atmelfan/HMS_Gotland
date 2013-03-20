package hms_gotland_server;

import java.io.IOException;
import java.util.HashMap;
import javax.vecmath.Vector3f;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import level.Entity;
import level.EntityPlayer;
import level.Level;

public class HMS_Gotland_Server extends Thread
{

	private static HMS_Gotland_Server server;
	private HashMap<Connection, EntityPlayer> players = new HashMap<>();
	private Server kryoServer;
	private long lastTick;
	private Level level;
	private int port;
	private boolean integrated;
	protected boolean running = true;
	
	public static void main(String[] args)
	{
		if(args.length >= 2)
		{
			server = new HMS_Gotland_Server(false, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		}else
		{
			server = new HMS_Gotland_Server(false, 4321, 4322);
		}
		server.start();
	}
	
	/**
	 * Starts a server on TCP port and UDP port
	 * @param port
	 */
	public HMS_Gotland_Server(boolean integrated, int tcpport, int udpport)
	{
		super("HMS_Gotland_Server");
		this.integrated = integrated;
		if(!integrated)
		{
			//
		}
		lastTick = System.currentTimeMillis();
		level = new Level("test", this);
		kryoServer = new Server();
		Packet.registerPackets(kryoServer.getKryo());
		kryoServer.addListener(listener);
		
		try
		{
			kryoServer.bind(tcpport, udpport);
		} catch (IOException e)
		{
			System.err.println("Error: HMS_Gotland_Server.HMS_Gotland_Server() - " + e.getMessage());
		}
		kryoServer.start();
		System.out.println((integrated ? "Server: " : "") + "Connected to port, UDP: " + udpport + ", TCP: " + tcpport);
	}

	public void run()
	{
		try
		{
			while(running)
			{
				if(System.currentTimeMillis() - lastTick >= 16)
				{
					level.tick();
					//Update 
					for (Entity entity : level.entities)
					{
						//TODO, needs to create entities on connect
						kryoServer.sendToAllUDP(new Packet.PositionEntity(entity));
						kryoServer.sendToAllUDP(new Packet.AngleEntity(entity));
					}
					lastTick = System.currentTimeMillis();
				}
			}
		} catch (Exception e)
		{
			System.err.println("Critical server error: ");
			e.printStackTrace();
		} finally
		{
			kryoServer.stop();
			kryoServer.close();
			level.destroy();
		}
	}

	public void terminate()
	{
		running = false;
	}

	private Listener listener = new Listener()
	{
		@Override
		public void connected(Connection connection)
		{
			System.out.println((integrated ? "Server: " : "") + "###" + connection.getRemoteAddressTCP().getHostName() + " connected###");
			super.connected(connection);
		}

		@Override
		public void disconnected(Connection connection)
		{
			EntityPlayer player = players.remove(connection);
			if(player != null)
			{
				System.out.println((integrated ? "Server: " : "") + player.username + " logged off.");
			}
			super.disconnected(connection);
		}

		@Override
		public void idle(Connection connection)
		{
			EntityPlayer player = players.get(connection);
			if(player != null)
			{
				System.out.println((integrated ? "Server: " : "") + player.username + " is idle.");
			}
			super.idle(connection);
		}
		
		@Override
		public void received(Connection connection, Object object)
		{
			System.out.println("hey");
			if(object instanceof Packet.Login)
			{
				System.out.println("hey");
				if(players.get(connection) == null)
				{
					String name = ((Packet.Login)object).name;
					players.put(connection, new EntityPlayer(level, name));
					connection.setName(name);
					System.out.println((integrated ? "Server: " : "") + name + " logged in.");
				}
			}
			super.received(connection, object);
		}
		
	};
}

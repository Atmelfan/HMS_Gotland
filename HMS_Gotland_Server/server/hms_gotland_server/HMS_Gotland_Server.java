package hms_gotland_server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Vector3f;

import level.Entity;
import level.EntityPlayer;
import level.Level;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class HMS_Gotland_Server extends Thread {

	private static HMS_Gotland_Server server;
	private HashMap<Connection, EntityPlayer> players = new HashMap<Connection, EntityPlayer>();
	private Server kryoServer;
	private long lastTick;
	public Level level;
	private int port;
	private boolean integrated;
	protected volatile boolean running = true;

	public static void main(String[] args) {

		if (args.length >= 3) {
			File level = new File(args[0]);
			server = new HMS_Gotland_Server(level, false,
					Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			server.run();
		}else
		{
			System.err.println("Use arguments <level dir> <tcp port> <udp port>!");
		}
	}

	/**
	 * Starts a server on TCP port and UDP port
	 * 
	 * @param port
	 */
	public HMS_Gotland_Server(File dir, boolean b, int tcpport, int udpport) {
		super("HMS_Gotland_Server");
		this.integrated = b;
		lastTick = System.currentTimeMillis();
		System.out.println((integrated ? "Server: " : "") + "Reading level...");
		level = new Level(dir);
		
		if (!integrated) {
			kryoServer = new Server();
			Packet.registerPackets(kryoServer.getKryo());
			kryoServer.addListener(listener);
			try {
				kryoServer.bind(tcpport, udpport);
			} catch (IOException e) {
				System.err
						.println("Error: HMS_Gotland_Server.HMS_Gotland_Server() - "
								+ e.getMessage());
			}
			kryoServer.start();
			System.out.println((integrated ? "Server: " : "")
					+ "Connected to port, UDP: " + udpport + ", TCP: " + tcpport);
		}
		System.out.println((integrated ? "Server: " : "") + "started...");
	}

	public void run() {
		try {
			while (running) {
				tick();
				sleep(16);
			}
		} catch (Exception e) {
			System.err.println("Critical server error: ");
			e.printStackTrace();
		} finally {
			
		}
	}

	public void tick()
	{
		level.tick();
		List<Entity> entities = level.entities;
		for (int i = 0; i < entities.size(); i++) {
			if (!(entities.get(i) instanceof EntityPlayer)) {
				kryoServer.sendToAllTCP(new Packet.EntityPosition(
						entities.get(i)));
			}
		}
	}
	
	
	private void terminate() {
		running = false;
	}
	
	public void destroy()
	{
		System.out.println((integrated ? "Server: " : "") + "Shutting down...");
		level.destroy();
		if (!integrated) {
			kryoServer.stop();
			kryoServer.close();
		}
	}

	private Listener listener = new Listener() {
		@Override
		public void connected(Connection connection) {
			System.out.println((integrated ? "Server: " : "") + "###"
					+ connection.getRemoteAddressTCP().getHostName()
					+ " connected###");
			super.connected(connection);
		}

		@Override
		public void disconnected(Connection connection) {
			EntityPlayer player = players.remove(connection);
			if (player != null) {
				System.out.println((integrated ? "Server: " : "")
						+ player.username + " logged off.");
			}
			super.disconnected(connection);
		}

		@Override
		public void idle(Connection connection) {
			super.idle(connection);
		}

		@Override
		public void received(Connection connection, Object object) {
			// System.out.println(object);
			if (object instanceof Packet.Login) {
				// System.out.println("hey");
				if(players.get(connection) == null) {
					String name = ((Packet.Login) object).name;
					EntityPlayer player = new EntityPlayer(level, name);
					player.setPos(level.getPlayerPos());
					players.put(connection, player);
					connection.setName(name);
					connection.sendTCP(new Packet.AcceptLogin(player.entityID, level.getPlayerPos(), level.modelName));
					List<Entity> entities = level.entities;
					for (int i = 0; i < entities.size(); i++) 
					{
						kryoServer.sendToTCP(connection.getID(), new Packet.CreateEntity(entities.get(i)));
					}
					kryoServer.sendToAllExceptTCP(connection.getID(), new Packet.CreateEntity(player));
					level.addPlayer(player);
					System.out.println((integrated ? "Server: " : "") + name + " logged in.");
				}
			}
			if (object instanceof Packet.ReqInfo) {
				Packet.Info re = new Packet.Info();
				re.stat = "Online, playing: " + level.name;
				re.desc = "A server";

			}

			super.received(connection, object);
		}

	};

	public void removeEntity(Entity entity) {
		kryoServer.sendToAllTCP(new Packet.KillEntity(entity));
	}

	public void addEntity(Entity entity) {
		kryoServer.sendToAllTCP(new Packet.CreateEntity(entity));
	}
}

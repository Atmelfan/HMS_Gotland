package hms_gotland_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class ClientInterface
{
	public boolean terminate;
	private int port;
	private ServerSocket socket;
	private ClientListener server;
	private ArrayList<Client> clients = new ArrayList<>();
	
	public void tick()
	{
		for (Client client : clients)
		{
			client.tick();
		}
	}
	
	public void connect(int port)
	{
		this.port = port;
		try
		{
			socket = new ServerSocket(port);
			server = new ClientListener(socket);
		} catch (UnknownHostException e)
		{
			System.err.println("ClientInterface.connect()" + e.getMessage());
		} catch (IOException e)
		{
			System.err.println("ClientInterface.connect()" + e.getMessage());
		}
	}
	
	private class ClientListener extends Thread
	{
		private ServerSocket socket;

		public ClientListener(ServerSocket socket)
		{
			this.socket = socket;
			start();
		}

		@Override
		public void run()
		{
			while (!terminate)
			{
				Socket client;
				try
				{
					client = socket.accept();
					if(client != null)
					{
						clients.add(new Client(client));
					}
					System.out.println("###Incoming connection from ip: " + client.getInetAddress() + "###");
				} catch (IOException e)
				{
					System.err.println("ClientListener.run()" + e.getMessage());
				}
			}
		}
	}
}

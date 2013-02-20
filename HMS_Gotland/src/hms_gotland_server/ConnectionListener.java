package hms_gotland_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class ConnectionListener
{
	public boolean terminate;
	private int port;
	private ServerSocket socket;
	private ClientListener server;
	private ArrayList<ClientInterface> clients = new ArrayList<>();
	
	public void tick()
	{
		for (ClientInterface client : clients)
		{
			client.tick();
		}
	}
	
	public void terminate()
	{
		terminate = true;
		server.interrupt();
		for (ClientInterface client : clients)
		{
			client.terminate();
		}
	}
	
	public void connect(int port)
	{
		this.port = port;
		try
		{
			socket = new ServerSocket(port);
			socket.setSoTimeout(10);
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
			super("ClientListener");
			this.socket = socket;
			start();
		}

		@Override
		public void run()
		{
			while (!terminate)
			{
				
				try
				{
					sleep(100);
				}catch(InterruptedException e1)
				{
				}
				
				try
				{
					Socket client = socket.accept();
					if(client != null)
					{
						clients.add(new ClientInterface(client));
					}
					System.out.println("###Connection accepted from: " + client.getInetAddress() + "###");
					
				}catch(SocketTimeoutException e)
				{
					
				}catch(IOException e)
				{
					System.err.println("ClientListener.run()" + e.getMessage());
				} 
			}
		}
	}
}

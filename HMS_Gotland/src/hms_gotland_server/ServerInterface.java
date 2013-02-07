package hms_gotland_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerInterface
{
	private String ip;
	private int port;
	private ServerSocket socket;
	
	public void connect(int port)
	{
		this.port = port;
		try
		{
			socket = new ServerSocket(port);
			
		} catch (UnknownHostException e)
		{
			System.err.println("ServerInterface.connect()" + e.getMessage());
		} catch (IOException e)
		{
			System.err.println("ServerInterface.connect()" + e.getMessage());
		}
	}
	
	private class TCPReadWrite extends Thread
	{
		@Override
		public void run()
		{
			
		}
	}
}

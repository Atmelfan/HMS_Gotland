package hms_gotland_client;

import hms_gotland_server.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerInterface
{
	public List<Packet> inputQueue 	= Collections.synchronizedList(new ArrayList<Packet>());
	public List<Packet> outputQueue = Collections.synchronizedList(new ArrayList<Packet>());
	public TCPRead clientReader;
	public TCPWrite clientWriter;
	public boolean terminate = false;
	public Socket client;
	
	public ServerInterface(Socket client)
	{
		this.client = client;
		try
		{
			clientReader = new TCPRead(new DataInputStream(client.getInputStream()));
			clientWriter = new TCPWrite(new DataOutputStream(client.getOutputStream()));
			clientReader.start();
			clientWriter.start();
			
			Packet.Login loginRequest = new Packet.Login();
			loginRequest.name = "testPlayer";
			outputQueue.add(loginRequest);
		} catch (IOException e)
		{
			System.err.println("Error: Server.Server() - " + e.getMessage());
		}
	}

	public void tick()
	{
		synchronized (inputQueue)
		{
			for (Packet packet : inputQueue)
			{
				if(packet instanceof Packet.Login)
				{
					//TODO
				}
			}
			inputQueue.clear();
		}
	}
	
	public void terminate()
	{
		terminate = true;
		try
		{
			client.close();
		} catch (IOException e)
		{
			System.err.println("Error: ServerInterface.terminate() - " + e.getMessage());
		}
		clientReader.interrupt();
		clientWriter.interrupt();
	}
	
	private class TCPRead extends Thread
	{
		private DataInputStream in;
		
		public TCPRead(DataInputStream input)
		{
			super("Client-TCPReader");
			in = input;
		}
		
		@Override
		public void run()
		{
			while(!terminate )
			{
				try
				{
					int id = in.readInt();
					Packet packet = Packet.getPacket(id);
					packet.read(in);
					inputQueue.add(packet);
					System.out.println(packet);
				} catch (IOException e)
				{
					if(!terminate)
						System.err.println("Error: TCPWrite.run() - " + e.getMessage());
				}
				
			}
		}
	}
	
	private class TCPWrite extends Thread
	{
		private DataOutputStream out;
		
		public TCPWrite(DataOutputStream output)
		{
			super("Client-TCPWriter");
			out = output;
		}
		
		@Override
		public void run()
		{
			while(!terminate )
			{
				try
				{
					for (Packet packet : outputQueue)
					{
						packet.writeID(out);
						packet.write(out);
					}
					outputQueue.clear();
				} catch (IOException e)
				{
					if(!terminate)
						System.err.println("Error: TCPWrite.run() - " + e.getMessage());
				}
				
			}
		}
	}
}

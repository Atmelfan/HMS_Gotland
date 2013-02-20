package hms_gotland_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientInterface
{
	public List<Packet> inputQueue 	= Collections.synchronizedList(new ArrayList<Packet>());
	public List<Packet> outputQueue = Collections.synchronizedList(new ArrayList<Packet>());
	public TCPRead clientReader;
	public TCPWrite clientWriter;
	public boolean terminate = false;
	public Socket client;
	
	public ClientInterface(Socket client)
	{
		this.client = client;
		try
		{
			clientReader = new TCPRead(new DataInputStream(client.getInputStream()));
			clientWriter = new TCPWrite(new DataOutputStream(client.getOutputStream()));
			clientReader.start();
			clientWriter.start();
		} catch (IOException e)
		{
			System.err.println("Error: Client.Client() - " + e.getMessage());
		}
		
		Packet.Login loginRequest = new Packet.Login();
		loginRequest.name = "login";
		outputQueue.add(loginRequest);
	}

	public void tick()
	{
		
	}
	
	public void terminate()
	{
		terminate = true;
		try
		{
			client.close();
		} catch(SocketException e)
		{
			
		} catch (IOException e)
		{
			System.err.println("Error: Client.terminate() - " + e.getMessage());
		}
		clientReader.interrupt();
		clientWriter.interrupt();
	}
	/*TCPReader*/
	private class TCPRead extends Thread
	{
		private DataInputStream in;
		
		public TCPRead(DataInputStream input)
		{
			super("Server-TCPReader");
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
					System.out.println("rs: " + id);
					packet.read(in);
					inputQueue.add(packet);
				} catch (IOException e)
				{
					if(!terminate)
						System.err.println("Error: TCPWrite.run() - " + e.getMessage());
				}
				
			}
		}
	}
	
	/*TCPWriter*/
	private class TCPWrite extends Thread
	{
		private DataOutputStream out;
		
		public TCPWrite(DataOutputStream output)
		{
			super("Server-TCPWriter");
			out = output;
		}
		
		@Override
		public void run()
		{
			while(!terminate )
			{
				try
				{
					synchronized (outputQueue)
					{
						for (Packet packet : outputQueue)
						{
							packet.writeID(out);
							packet.write(out);
							System.out.println("ss: " + packet.getID());
						}
						outputQueue.clear();
					}
					
				}catch (IOException e)
				{
					if(!terminate)
						System.err.println("Error: TCPWrite.run() - " + e.getMessage());
				}
				
			}
		}
	}
}

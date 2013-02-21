package hms_gotland_server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


public class ConnectionListener
{
	public boolean terminate;
	
	private Iterator<SelectionKey> listener;
	private Selector selector;
	private Set<SelectionKey> selectedKeys;
	
	private ByteBuffer buffer = ByteBuffer.allocate(16 * 1024).order(ByteOrder.BIG_ENDIAN);//A 16Kb buffer
	
	public void run()
	{
		tick();
	}
	
	public void tick()
	{
		try
		{
			selector.select();
			selectedKeys = selector.selectedKeys();
		} catch (IOException e)
		{
			System.err.println("ConnectionListener.tick()" + e.getMessage());
		}
		
		try
		{
			while (listener.hasNext()) 
			{
			    SelectionKey key = (SelectionKey)listener.next();
			    if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) 
			    {
			    	//New client connection
			    	ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
			    	SocketChannel sc = ssc.accept();
			    	System.out.println("###Accepted new client from IP: " + sc.socket().getInetAddress() + ", port: " + sc.socket().getPort() + "###");
			    	sc.configureBlocking( false );
			    	SelectionKey newKey = sc.register(selector, SelectionKey.OP_READ);
			    	selectedKeys.add(newKey);
			    }
			    else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) 
			    {
			        //Incoming client data
			        SocketChannel sc = (SocketChannel)key.channel();
			        //Can haz data?
			        if (!key.isReadable()) continue;
			        
			        int bytesRead = sc.read(buffer);
			        
			        if(bytesRead == -1)//EOF
			        {
			        	key.cancel();
			        	sc.close();
			        	buffer.clear();
			        	continue;
			        }
			        
			        
			        
			        //Clean up buffer for next packet
			        buffer.clear();
			        
			   }
			   listener.remove();//We are done with it(processed or not)
			}
		} catch (IOException e)
		{
			System.err.println("ConnectionListener.tick()" + e.getMessage());
		}
	}
	
	public void terminate()
	{
		terminate = true;
		for (SelectionKey key : selectedKeys)
		{
			try
			{
				key.channel().close();
			} catch (IOException e)
			{
				System.err.println("ConnectionListener.terminate()" + e.getMessage());
			}
		}
	}
	
	public void connect(int port)
	{
		try
		{
			selector = Selector.open();
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.socket().bind(new InetSocketAddress(port));
			
			ssc.register(selector, SelectionKey.OP_ACCEPT );
			ssc.configureBlocking(false);
			
			int num = selector.select();
			selectedKeys = selector.selectedKeys();
			listener = selectedKeys.iterator();
		} catch (UnknownHostException e)
		{
			System.err.println("ClientInterface.connect()" + e.getMessage());
		} catch (IOException e)
		{
			System.err.println("ClientInterface.connect()" + e.getMessage());
		}
	}

}

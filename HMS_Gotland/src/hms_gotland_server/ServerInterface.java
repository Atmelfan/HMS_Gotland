package hms_gotland_server;

public class ServerInterface
{
	private String ip;
	private int port;
	
	public void connect(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}
	
	private class TCPReadWrite extends Thread
	{
		@Override
		public void run()
		{
			
		}
	}
}

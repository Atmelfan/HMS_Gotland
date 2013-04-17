package hms_gotland_client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

import Util.OSUtil;

import saveformat.HMG_Format;

public class ResourceManager
{
	private File serverDir;
	private HMS_Gotland game;
	
	public ResourceManager(HMS_Gotland game)
	{
		this.game = game;
		updateServers();
	}
	
	public void createNewServerResource(String name, String ip, int porttcp, int portudp)
	{
		File server = new File("Servers", name);
		File models = new File(server, "models");
		File enviro = new File(server, "enviroments");
		File tempor = new File(server, "temp");
		models.mkdirs();
		enviro.mkdirs();
		tempor.mkdirs();
		HMG_Format serverInfo = new HMG_Format();
		try
		{
			serverInfo.read(new File(server, "server.hmg"));
		}catch(IOException e)
		{
		}
		System.out.println(serverInfo.header.toString());
		serverInfo.header.setString("name", name);
		serverInfo.header.setString("created", OSUtil.getTime());
		serverInfo.header.setString("lastOpen", OSUtil.getTime());
		serverInfo.header.setString("ip", ip);
		serverInfo.header.setInteger("portTCP", porttcp);
		serverInfo.header.setInteger("portUDP", portudp);
		serverInfo.header.setInteger("singleplayer", 1);
		
		try
		{
			serverInfo.write(new File(server, "server.hmg"));
		} catch (IOException e)
		{
			System.err.println("Error: ResourceManager.createNewServerResource() - " + e.getMessage());
		}
	}
	
	public void setServerResource(String name)
	{
		serverDir = new File("Servers", name);
		try
		{
			File server = new File(serverDir, "server.hmg");
			HMG_Format serverInfo = new HMG_Format();
			serverInfo.read(server);
			serverInfo.header.setString("lastOpen", OSUtil.getTime());
			serverInfo.write(server);
		}catch(IOException e)
		{
		}
	}
	
	public File getResource(String s)
	{
		if(s.startsWith("generic://"))
		{
			s = s.replace("generic://", "");
			return new File("Resources/assets/", s);
		}
		else if(s.startsWith("server://"))
		{
			s = s.replace("server://", "");
			return new File(serverDir, s);
		}
		return new File(s);
	}
	
	/**
	 * 
	 * @param url tex gpa-robotics.com/blabla.md3
	 * @param to tex server://models/blabla.md3
	 */
	public void download(final String url, final File dest)
	{
		Thread downloader = new Thread()
		{
			public void run()
			{
				try
				{
					File temp = File.createTempFile("hmg", ".hgt", new File(serverDir, "temp"));
					temp.deleteOnExit();
					//Download file to temp from url
					URL website = new URL(url);
				    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				    FileOutputStream fos = new FileOutputStream(temp);
				    boolean done = false;
				    long ofs = 0;
				    System.out.println("Downloading " + dest.getName() + " from " + url);
				    while(!done)
				    {
				    	long packet = fos.getChannel().transferFrom(rbc, ofs, 1024 * 512);
				    	ofs += packet;
				    	done = !(packet >= 1024*512);
				    	//System.out.println("Downloaded " + packet / 1024 + "kb at " + (packet / (1024)) / ((float)(ms + 1) / 1000) + "kb/s");
				    }
				    System.out.println("Downloaded " + ofs / 1024 + "kb from " + url);
				    fos.close();
				    //Copy to its real position
				    dest.createNewFile();
				    OSUtil.copyFile(temp, dest);
				    dest.setExecutable(false);
					temp.delete();//Force deletion of the temporary file when complete
				} catch (IOException e)
				{
					System.err.println("Error: ResourceManager.download() - " + e.getMessage());
				}
			}
		};
		downloader.setDaemon(true);
		downloader.start();
	}
	
	private Server[] servers;
	public void updateServers()
	{
		File[] serverfiles = new File("Servers").listFiles();
		ArrayList<Server> servers = new ArrayList<>();
		for (int i = 0; i < serverfiles.length; i++)
		{
			if(serverfiles[i].isDirectory())
				servers.add(new Server(game, serverfiles[i]));
		}
		this.servers = servers.toArray(new Server[0]);
	}
	
	public Server[] getServers()
	{
		return servers;
	}
}

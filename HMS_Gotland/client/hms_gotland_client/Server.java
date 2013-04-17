package hms_gotland_client;

import hms_gotland_server.HMS_Gotland_Server;
import hms_gotland_server.Packet;
import hms_gotland_server.Packet.Dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.bind.DatatypeConverter;

import saveformat.HMG_Basic;
import saveformat.HMG_Compound;
import saveformat.HMG_Format;

import Util.OSUtil;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class Server extends Listener
{
	public String name;
	public boolean isSinglePlayer = false;
	private File dir;
	public String ip;
	public int portTcp, portUdp;
	public String desc;
	public String stat;
	
	private HMS_Gotland_Server server;
	
	private HMS_Gotland game;
	private Client client;
	private HashMap<String, String> clientDependencies = new HashMap<>();
	private Dependency[] serverDependencies;
	
	public Server(HMS_Gotland game, File dir)
	{
		this.dir = dir;
		this.game = game;
		readHMG(new File(dir, "server.hmg"));
		client = new Client();
		Packet.registerPackets(client.getKryo());
		client.addListener(this);
		updateChecksums(new File(dir, "enviroments"));
		updateChecksums(new File(dir, "models"));
	}
	
	public boolean connect(Client client)
	{
		if(isSinglePlayer)
		{
			server = new HMS_Gotland_Server(dir, true, portTcp, portUdp);
			server.start();
		}else
		{
			if(serverDependencies == null)
			{
				JOptionPane.showMessageDialog(null, "Waiting for server, try again later.");
				return false;
			}else
			{
				if(!downloadResources())
				{
					JOptionPane.showMessageDialog(null, "Failed to download dependency.");
					return false;
				}
			}
		}
		
		try
		{
			client.connect(10000, ip, portTcp, portUdp);
			return true;
		} catch (IOException e)
		{
			System.err.println("Error: Server.connect() - " + e.getMessage());
		}
		return false;
	}
	
	public void disconnect(Client client)
	{
		server.terminate();
		server = null;
		client.stop();
		client.close();
	}
	
	private void readHMG(File file)
	{
		HMG_Format f = new HMG_Format();
		try
		{
			f.read(file);
			f.header.setString("lastOpen", OSUtil.getTime());
			ip = f.header.getString("ip");
			portTcp = f.header.getInteger("portTCP");
			portUdp = f.header.getInteger("portUDP");
			isSinglePlayer = f.header.getInteger("singleplayer") != 0;
			System.out.println(f.toString());
			
			List<HMG_Basic> list = f.root.getList("dependencies");
			
			if(list != null)
			{
				for (int i = 0; i < list.size(); i++)
				{
					HMG_Basic tag = list.get(i);
					if(tag instanceof HMG_Compound)
					{
						HMG_Compound compound = (HMG_Compound)tag;
						clientDependencies.put(compound.getString("name"), compound.getString("md5"));
					}
				}
			}
		} catch (IOException e)
		{
			System.err.println("Error: Server.readHMG() - " + e.getMessage());
		}
	}
	
	private void writeHMG()
	{
		
	}

	public void update()
	{
		if(isSinglePlayer)
		{
			stat = "Singleplayer";
			return;
		}
		stat = "Waiting for server response...";
		client.start();
		Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					client.connect(1000, ip, portTcp, portUdp);
					sleep(5000);
					if(client.isConnected())
					{
						client.stop();
						stat = "Server failed to respond.";
					}
					
				} catch (IOException e)
				{
					System.err.println("Error: Server.update() - " + e.getMessage());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}
	
	public void updateChecksums(File file)
	{
		if(!file.isDirectory())
			return;
		HMG_Format hmg = new HMG_Format();
		hmg.header.setString("Directory", file.getName());
		File[] files = file.listFiles();
		for(int i = 0; i < files.length; i++) {
			File md5 = files[i];
			try {
				hmg.root.setByteArray(md5.getName(), OSUtil.generateMD5(new FileInputStream(md5)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		try {
			hmg.write(new File(file, "checksums.hmg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean downloadResources()
	{
		if(!(JOptionPane.showConfirmDialog(null, "Download resources required by server?", "Download",
				JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION))
			return false;
		if(serverDependencies != null)
		{
			for (int i = 0; i < serverDependencies.length; i++)
			{
				Packet.Dependency d = serverDependencies[i];
				String checksum = new String(d.checksum);
				//Check if file exists or has equal checksum in declaration
				if(!checksum.equals(clientDependencies.get(d.name)))
				{
					File file = null;
					if(d.type == Packet.Dependency.MODEL && !d.name.isEmpty())
					{
						file = new File(dir, "models/" + d.name);
					}
					
					if(d.type == Packet.Dependency.ENVIROMENT && !d.name.isEmpty())
					{
						file = new File(dir, "enviroments/" + d.name);
					}
					
					//Double check if file exists and if its checksum is correct
					if(file != null)
					{
						if(file.exists())
						{
							try
							{
								byte[] checksum2 = OSUtil.generateMD5(new FileInputStream(file));
								if(checksum2.equals(d.checksum))
									continue;//Checksums matches, continue with next file.
							} catch (FileNotFoundException e)
							{
								System.err.println("Error: Server.downloadResources() - " + e.getMessage());
							}
						}
						game.getResourceManager().download(d.url, file);
					}
				}
			}
		}
		return true;
	}

	@Override
	public void connected(Connection arg0)
	{
		super.connected(arg0);
		arg0.sendTCP(new Packet.ReqInfo());
	}

	@Override
	public void disconnected(Connection connection)
	{
		super.disconnected(connection);
		client.stop();
	}

	@Override
	public void received(Connection arg0, Object arg1)
	{
		super.received(arg0, arg1);
		if(arg1 instanceof Packet.Info)
		{
			Packet.Info info = (Packet.Info)arg1;
			desc = info.desc;
			stat = info.stat;
			if(info.hasDependecies)
			{
				serverDependencies = info.dependencies;
			}
		}
		client.stop();
	}
	
	
}

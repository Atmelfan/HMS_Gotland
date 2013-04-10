package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HMG_Format
{
	public HMG_Compound header;
	public HMG_Compound root;
	
	public HMG_Format()
	{
		header = new HMG_Compound();
		header.name = "header";
		root = new HMG_Compound();
		root.name = "root";
	}
	
	public static void main(String[] args) throws Exception
	{
		if(args.length > 1 && args[0].equalsIgnoreCase("print"))
		{
			for (int i = 0; i < args.length; i++)
			{
				File file = new File(args[i]);
				if(!file.exists())
				{
					System.err.println("File doesn't exist: " + file.getAbsolutePath());
					System.err.println("Here, have this formula instead: 4/3 * Pi * sqrt(X^2 + Y^2 + Z^2 + time^2)^3");
					continue;
				}
				HMG_Format sf1 = new HMG_Format();
				sf1.read(file);
				System.out.println(file.getName() + ":");
				System.out.println(sf1.header);
				System.out.println(sf1.root);
			}
		}else if(args.length > 2 && args[0].equalsIgnoreCase("set"))
		{
			File file = new File(args[0]);
			HMG_Format sf1 = new HMG_Format();
			if(!file.exists())
			{
				file.createNewFile();
			}else
			{
				sf1.read(file);
			}
			
			String[] hmgPath = args[1].split("/");
			if(hmgPath[0].equalsIgnoreCase("header"))
			{
				setPath(sf1.header, hmgPath, args[2]);
			}
			if(hmgPath[0].equalsIgnoreCase("root"))
			{
				setPath(sf1.root, hmgPath, args[2]);
			}
			
		}else
		{
			System.err.println("What? Should I magically know what you want to do!?");
		}
		
		
	}
	
	private static void setPath(HMG_Compound parent, String[] hmgPath, String value)
	{
		for (int i = 1; i < hmgPath.length; i++)
		{
			if(i < hmgPath.length - 1)
			{
				HMG_Compound newc = parent.getCompound(hmgPath[i]);
				if(newc == null)
				{
					newc = new HMG_Compound();
					parent.setCompound(hmgPath[i], newc);
				}
				parent = newc;
			}else
			{
				parent.setString(hmgPath[i], value);
			}
		}
	}
	
	public void read(File file) throws IOException
	{
		
		DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
		int magic = in.readInt();
		
		
		if(magic != (('0'<<24) + ('R'<<16) + ('P'<<8) + 'G')) 
		{
			in.close();
			throw new IOException("Invalid magic");
		}
		
		header.read(in);
		root.read(in);
		
		in.close();
	}
	
	public void write(File file) throws IOException
	{
		DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
		if(!file.exists())
			file.createNewFile();
		out.writeInt((('0'<<24) + ('R'<<16) + ('P'<<8) + 'G'));
		
		header.write(out);
		root.write(out);
		out.close();
	}
	
	static public HashMap<Integer, Class<? extends HMG_Basic>> tags = new HashMap<>();
	
	static
	{
		tags.put(1, HMG_Integer.class);
		tags.put(2, HMG_Float.class);
		tags.put(3, HMG_String.class);
		tags.put(4, HMG_Compound.class);
		tags.put(5, HMG_List.class);
	}
}

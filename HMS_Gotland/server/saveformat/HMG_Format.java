package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HMG_Format
{
	public HMG_Compound header = new HMG_Compound("header");
	public HMG_Compound root = new HMG_Compound("root");
	
	public static void main(String[] args) throws Exception
	{
		HMG_Format sf = new HMG_Format();
		
		sf.header.setInteger("format_version", 1);
		
		HMG_Compound t = new HMG_Compound("info");
		t.setString("vendor", "gpa_robotics");
		t.setInteger("protocol_version", 0);
		sf.header.setList(t.name, t);
		
		
		File f = new File("Resources/levels/test1.hms");
		if(!f.exists())
		{
			f.createNewFile();
		}
		sf.write(f);
		
		HMG_Format sf1 = new HMG_Format();
		sf1.read(f);
		System.out.println(sf1.header);
		System.out.println(sf1.root);
	}
	
	public void read(File file) throws Exception
	{
		
		DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
		int magic = in.readInt();
		
		
		if(magic != (('0'<<24) + ('R'<<16) + ('P'<<8) + 'G')) 
		{
			in.close();
			throw new Exception("Invalid magic");
		}
		
		header.read(in);
		root.read(in);
		
		in.close();
	}
	
	public void write(File file) throws Exception
	{
		DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
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

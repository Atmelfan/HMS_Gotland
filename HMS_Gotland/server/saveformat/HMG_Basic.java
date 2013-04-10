package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class HMG_Basic
{
	public String name;
	
	public HMG_Basic()
	{
	}
	
	public HMG_Basic(String s)
	{
		name = s;
	}
	
	public abstract int getID();
	
	public void read(DataInputStream in) throws IOException
	{
		byte[] buffer = new byte[in.readInt()];
		in.read(buffer);
		name = new String(buffer, "UTF-8");
	}
	
	public void write(DataOutputStream out) throws IOException
	{
		byte[] buffer = name.getBytes("UTF-8");
		out.writeInt(buffer.length);
		out.write(buffer);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "HMG_Basic [name=" + String.valueOf(name) + "]";
	}
}

package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class HMG_Basic
{
	public char[] name;
	
	public HMG_Basic()
	{
	}
	
	public int getID()
	{
		return 0;
	}
	
	public void read(DataInputStream in) throws IOException
	{
		name = new char[in.readInt()];
		for(int i = 0; i < name.length; i++)
		{
			name[i] = in.readChar();
		}
	}
	
	public void write(DataOutputStream out) throws IOException
	{
		out.writeInt(name.length);
		for(int i = 0; i < name.length; i++)
		{
			out.writeChar(name[i]);
		}
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

package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class HMG_String extends HMG_Basic
{
	public char[] value;
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "HMG_String [name=" + String.valueOf(name) + ", value="
				+ String.valueOf(value) + "]";
	}

	public HMG_String()
	{
	}
	
	@Override
	public int getID()
	{
		return 3;
	}
	
	@Override
	public void read(DataInputStream in) throws IOException
	{
		super.read(in);
		value = new char[in.readInt()];
		for(int i = 0; i < value.length; i++)
		{
			value[i] = in.readChar();
		}
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException
	{
		super.write(out);
		out.writeInt(value.length);
		for(int i = 0; i < value.length; i++)
		{
			out.writeChar(value[i]);
		}
	}
}

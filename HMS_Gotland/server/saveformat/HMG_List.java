package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HMG_List extends HMG_Basic
{
	public List<HMG_Basic> stuff = new ArrayList<>();
	
	@Override
	public void read(DataInputStream in) throws IOException
	{
		super.read(in);
		int tags = in.readInt();
		for (int i = 0; i < tags; i++)
		{
			int id = in.readInt();
			try
			{
				HMG_Basic tag = HMG_Format.tags.get(id).newInstance();
				tag.read(in);
				stuff.add(tag);
			} catch (InstantiationException | IllegalAccessException e)
			{
				System.err.println("Skipped invalid tag in save file!");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		super.write(out);
		out.writeInt(stuff.size());
		for (int i = 0; i < stuff.size(); i++)
		{
			HMG_Basic tag = stuff.get(getID());
			out.writeInt(tag.getID());
			tag.write(out);
		}
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	@Override
	public int getID()
	{
		return 5;
	}

}

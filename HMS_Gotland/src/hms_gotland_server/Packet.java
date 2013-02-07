package hms_gotland_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public abstract class Packet
{
	////////Base class////////
	public void writeID(DataOutputStream output) throws IOException
	{
		output.writeByte(getID());
	}
	
	abstract void write(DataOutputStream output) throws IOException;
	abstract void read(DataInputStream input) throws IOException;
	abstract int getID();
	
	////////Packets////////
	
	public class Login extends Packet
	{
		public String name;
		
		@Override
		public void write(DataOutputStream output) throws IOException
		{
			writeString(name, output);
		}

		@Override
		public void read(DataInputStream input) throws IOException
		{
			readString(name, input);
		}

		@Override
		int getID()
		{
			return 0;
		}
		
	}
	
	public class Position extends Packet
	{
		public Vector3f origin;
		
		@Override
		void write(DataOutputStream output) throws IOException
		{
			output.writeFloat(origin.x);
			output.writeFloat(origin.y);
			output.writeFloat(origin.z);
		}

		@Override
		void read(DataInputStream input) throws IOException
		{
			origin.x = input.readFloat();
			origin.y = input.readFloat();
			origin.z = input.readFloat();
		}
		
		@Override
		int getID()
		{
			return 1;
		}
		
	}
	
	public class Angle extends Packet
	{
		public Matrix3f basis;
		@Override
		void write(DataOutputStream output) throws IOException
		{
			for (int i = 0; i < 3; i++)
			{
				float[] t = new float[3];
				basis.getRow(i, t);
				output.writeFloat(t[0]);
				output.writeFloat(t[1]);
				output.writeFloat(t[2]);
			}
		}

		@Override
		void read(DataInputStream input) throws IOException
		{
			for (int i = 0; i < 3; i++)
			{
				float[] t = new float[3];
				t[0] = input.readFloat();
				t[1] = input.readFloat();
				t[2] = input.readFloat();
				basis.setRow(i, t);
			}
		}

		@Override
		int getID()
		{
			return 2;
		}
		
	}
	
	////////Util methods////////
	
	protected void writeString(String s, DataOutputStream output) throws IOException
	{
		byte[] sbytes = s.getBytes("UTF-8");
		output.writeInt(sbytes.length);
		output.write(sbytes);
	}
	
	protected String readString(String s, DataInputStream input) throws IOException
	{
		int length = input.readInt();
		byte[] sbytes = new byte[length];
		input.read(sbytes);
		return new String(sbytes, "UTF-8");
	}
}

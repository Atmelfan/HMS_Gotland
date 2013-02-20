package hms_gotland_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public abstract class Packet
{
	////////Base class////////
	public void writeID(DataOutputStream output) throws IOException
	{
		output.writeByte(getID());
	}
	
	public abstract void write(DataOutputStream output) throws IOException;
	public abstract void read(DataInputStream input) throws IOException;
	public abstract int getID();
	
	////////Packets////////
	
	public static Packet getPacket(int packetID)
	{
		try
		{
			return packets.get(packetID).newInstance();
		} catch (InstantiationException e)
		{
			System.err.println("Error: Packet.getPacket() - " + e.getMessage());
		} catch (IllegalAccessException e)
		{
			System.err.println("Error: Packet.getPacket() - " + e.getMessage());
		}
		return null;
	}
	
	private static HashMap<Integer, Class<? extends Packet>> packets = new HashMap<>();
	
	static
	{
		packets.put(0, Login.class);
		packets.put(1, Position.class);
		packets.put(2, Angle.class);
		packets.put(3, CreateEntity.class);
	}
	
	public static class Login extends Packet
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
			name = readString(input);
		}

		@Override
		public
		int getID()
		{
			return 0;
		}
		
	}
	
	
	private static abstract class Entity extends Packet
	{
		public int entityID;
		
		@Override
		public void write(DataOutputStream output) throws IOException
		{
			output.writeInt(entityID);
		}

		@Override
		public void read(DataInputStream input) throws IOException
		{
			entityID = input.readInt();
		}

		@Override
		public abstract int getID();
		
	}
	
	public static class CreateEntity extends Entity
	{
		public String model;
		
		@Override
		public void write(DataOutputStream output) throws IOException
		{
			writeString(model, output);
		}

		@Override
		public void read(DataInputStream input) throws IOException
		{
			model = readString(input);
		}
		
		@Override
		public int getID()
		{
			return 3;
		}
		
	}
	
	public static class Position extends Entity
	{
		public Vector3f origin;
		
		@Override
		public void write(DataOutputStream output) throws IOException
		{
			super.write(output);
			output.writeFloat(origin.x);
			output.writeFloat(origin.y);
			output.writeFloat(origin.z);
		}

		@Override
		public void read(DataInputStream input) throws IOException
		{
			super.read(input);
			origin.x = input.readFloat();
			origin.y = input.readFloat();
			origin.z = input.readFloat();
		}
		
		@Override
		public int getID()
		{
			return 1;
		}
		
	}
	
	public static class Angle extends Entity
	{
		public Quat4f basis;
		@Override
		public void write(DataOutputStream output) throws IOException
		{
			super.write(output);
			output.writeFloat(basis.x);
			output.writeFloat(basis.y);
			output.writeFloat(basis.z);
			output.writeFloat(basis.w);
		}

		@Override
		public void read(DataInputStream input) throws IOException
		{
			super.read(input);
			basis.x = input.readFloat();
			basis.y = input.readFloat();
			basis.z = input.readFloat();
			basis.w = input.readFloat();
		}

		@Override
		public int getID()
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
	
	protected String readString(DataInputStream input) throws IOException
	{
		int length = input.readInt();
		byte[] sbytes = new byte[length];
		input.read(sbytes);
		return new String(sbytes, "UTF-8");
	}
}

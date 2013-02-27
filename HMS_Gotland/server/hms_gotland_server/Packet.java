package hms_gotland_server;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import level.Entity;

import com.esotericsoftware.kryo.Kryo;

public class Packet
{
	public static void registerPackets(Kryo kryo)
	{
		kryo.register(Login.class);
		kryo.register(CreateEntity.class);
		kryo.register(PositionEntity.class);
		kryo.register(AngleEntity.class);
	}
	
	public static class Login extends Packet
	{
		public Login(String s)
		{
			name = s;
		}
		
		public String name;
	}
	
	public static class Message extends Packet
	{
		public static final int MESSAGE_CHAT = 1;
		public static final int MESSAGE_INFO = 2;
		public static final int MESSAGE_SUB = 3;
		
		public String msg;
		public int type;
		
		public Message(int t, String message)
		{
			type = t;
			msg = message;
		}
	}
	
	private static abstract class EntityPacket extends Packet
	{
		protected int entityID;
	}
	
	public static class CreateEntity extends EntityPacket
	{
		public String model;
		
		public CreateEntity(Entity entity)
		{
			entityID = entity.entityID;
			model = entity.getEntityModelName();
		}
	}
	
	public static class PositionEntity extends EntityPacket
	{
		public Vector3f origin;
		
		public PositionEntity(Entity entity)
		{
			entityID = entity.entityID;
			origin = entity.getPos();
		}
	}
	
	public static class AngleEntity extends EntityPacket
	{
		public Quat4f basis;
		
		public AngleEntity(Entity entity)
		{
			entityID = entity.entityID;
			basis = entity.getAngle();
		}
	}
}

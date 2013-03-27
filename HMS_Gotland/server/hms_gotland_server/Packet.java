package hms_gotland_server;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import level.Entity;
import level.EntityPlayer;

import com.esotericsoftware.kryo.Kryo;

public class Packet
{
	public Packet()
	{
	}
	
	public static void registerPackets(Kryo kryo)
	{
		//#####Main packets#####
		kryo.register(Login.class);
		kryo.register(AcceptLogin.class);
		kryo.register(Message.class);
		kryo.register(CreateEntity.class);
		kryo.register(PositionEntity.class);
		kryo.register(AngleEntity.class);
		//#####Aux classes#####
		kryo.register(Vector3f.class);
		kryo.register(Quat4f.class);
	}
	
	public static class Login extends Packet
	{
		public String name;
		public Login()
		{
		}
		
		public Login(String s)
		{
			name = s;
		}
	}
	
	public static class AcceptLogin extends Packet
	{
		public int playerID;
		public Vector3f playerPos;
		public String levelName;
		
		public AcceptLogin()
		{
		}
		
		public AcceptLogin(int id, Vector3f playerposi, String levelData)
		{
			playerID = id;
			playerPos = playerposi;
			levelName = levelData;
		}
	}
	
	public static class Message extends Packet
	{
		public static final int MESSAGE_CHAT = 1;//Chat
		public static final int MESSAGE_INFO = 2;//Info popups
		public static final int MESSAGE_SUBT = 3;//Subtitles
		public static final int MESSAGE_SYST = 3;//System messages
		
		public String msg;
		public int type;
		
		public Message()
		{
		}
		
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

package hms_gotland_server;

import java.util.ArrayList;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import level.Entity;
import level.EntityPlayer;

import com.esotericsoftware.kryo.Kryo;

public class Packet {
	public Packet() {
	}

	public static void registerPackets(Kryo kryo) {
		// #####Main packets#####
		kryo.register(Login.class);
		kryo.register(AcceptLogin.class);
		kryo.register(Message.class);
		kryo.register(Info.class);
		kryo.register(ReqInfo.class);
		kryo.register(Dependency.class);
		kryo.register(CreateEntity.class);
		kryo.register(PositionEntity.class);
		// #####Aux classes#####
		kryo.register(Vector3f.class);
		kryo.register(Quat4f.class);
	}

	public static class Login extends Packet {
		public String name;

		public Login() {
		}

		public Login(String s) {
			name = s;
		}
	}

	public static class AcceptLogin extends Packet {
		public int playerID;
		public Vector3f playerPos;
		public String levelName;

		public AcceptLogin() {
		}

		public AcceptLogin(int id, Vector3f playerposi, String levelData) {
			playerID = id;
			playerPos = playerposi;
			levelName = levelData;
		}
	}

	public static class Dependency extends Packet {
		public static final int MODEL = 1;
		public static final int ENVIROMENT = 2;
		public String url;// Tex gpa-robotics.com/blablabla.md3
		public String name;// Tex models/blablabla.md3
		public byte[] checksum;
		public int type = 0;
	}

	public static class Info extends Packet {
		public String desc;
		public String stat;
		public boolean hasDependecies;
		public Dependency[] dependencies;
	}

	public static class ReqInfo extends Packet {
	}

	public static class Message extends Packet {
		public static final int MESSAGE_CHAT = 1;// Chat
		public static final int MESSAGE_INFO = 2;// Info popups
		public static final int MESSAGE_SUBT = 3;// Subtitles
		public static final int MESSAGE_SYST = 3;// System messages

		public String msg;
		public int type;

		public Message() {
		}

		public Message(int t, String message) {
			type = t;
			msg = message;
		}
	}

	private static abstract class EntityPacket extends Packet {
		public int entityID;
	}

	public static class CreateEntity extends EntityPacket {
		public String model;
		public float mass = 0;

		public CreateEntity() {
		}

		public CreateEntity(Entity entity) {
			entityID = entity.entityID;
			model = entity.getModelName();
			mass = entity.getMass();
		}
	}

	public static class KillEntity extends EntityPacket {
		public KillEntity() {
		}

		public KillEntity(Entity entity) {
			entityID = entity.entityID;
		}
	}

	public static class PositionEntity extends EntityPacket {
		public Vector3f origin;
		public Quat4f basis;

		public PositionEntity() {
		}

		public PositionEntity(Entity entity) {
			entityID = entity.entityID;
			origin = entity.getPos();
			basis = entity.getAngle();
		}
	}
}

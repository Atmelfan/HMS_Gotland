package level;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;


public class ClientPlayer extends ClientEntity
{

	private PairCachingGhostObject ghostObject;
	private KinematicCharacterController character;
	private Vector3f velocity = new Vector3f();
	private Vector3f lastPos = new Vector3f();
	private Quat4f desiredOrientation = new Quat4f();

	public ClientPlayer(BaseLevel lvl, String model, int id)
	{
		super(lvl, model, id);
		System.out.println("New character(ID: " + id + ", model: " + model + ")");
	}
	
	public void tick()
	{
		temp.setRotation(desiredOrientation);
		ghostObject.setWorldTransform(temp);
		
		velocity = QuaternionUtil.quatRotate(desiredOrientation, velocity, new Vector3f());
		velocity.scale(0.98f);
		character.setWalkDirection(velocity);
		lastPos = getPos();
	}
	
	@Override
	protected void setupBody(Vector3f start)
	{
 	    Vector3f localInertia = new Vector3f(0, 0, 0);
	    // Transform
	    Transform startTransform = new Transform();
	    startTransform.setIdentity();
	    startTransform.origin.set(start);
	    // MotionState & body
	    ghostObject = new PairCachingGhostObject();
		getGhostObject().setWorldTransform(startTransform);
		level.world.getBroadphase().getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		ConvexShape capsule = new BoxShape(new Vector3f(1, 1, 1));
		capsule.calculateLocalInertia(getMass(), localInertia);
		getGhostObject().setCollisionShape(capsule);
		getGhostObject().setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);
		float stepHeight = 0.35f;
		setCharacter(new KinematicCharacterController(getGhostObject(), capsule, stepHeight));
		character.setJumpSpeed(7.5f);
	}

	
	@Override
	public float getFrame()
	{
		Vector3f s = new Vector3f(getPos());
		s.sub(lastPos);
		return (float) Math.sin(s.length() * 0);
	}

	@Override
	protected float getMass()
	{
		return 50f;
	}

	
	@Override
	public Vector3f getVel() {
		return ghostObject.getInterpolationLinearVelocity(new Vector3f());
	}

	public void move(float yaw)
	{
		QuaternionUtil.setEuler(desiredOrientation, (float) Math.toRadians(yaw), 0f, 0f);
		if(!character.onGround()) return;
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			velocity.x = 1f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			velocity.x = -1f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			velocity.z = 1f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
		{
			velocity.z = -1f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
		{
			if(character.onGround())
				character.jump();
		}
		velocity.scale(0.2f);
	}
	
	public void setPos(Vector3f playerPos)
	{
		ghostObject.getWorldTransform(temp);
		temp.origin.set(playerPos);
		ghostObject.setWorldTransform(temp);
	}

	public void addYaw(float f)
	{
	}
	
	/**
	 * @return the character
	 */
	public KinematicCharacterController getCharacter()
	{
		return character;
	}

	/**
	 * @param character the character to set
	 */
	public void setCharacter(KinematicCharacterController character)
	{
		this.character = character;
	}

	/**
	 * @return the ghostObject
	 */
	public PairCachingGhostObject getGhostObject()
	{
		return ghostObject;
	}

	@Override
	public Transform getTempTransform() {
		return ghostObject.getWorldTransform(temp);
	}
	
	@Override
	public void setTempTransform() {
		ghostObject.setWorldTransform(temp);
	}
}

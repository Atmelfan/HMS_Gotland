package level;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.CollisionShape;
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

	public ClientPlayer(ClientLevel lvl, String model, int id)
	{
		super(lvl, model, id);
		System.out.println("New character(ID: " + id + ", model: " + model + ")");
	}
	
	public void tick()
	{
		Quat4f r = new Quat4f(desiredOrientation);
		ghostObject.getWorldTransform(temp);
		Quat4f a = new Quat4f();
		temp.getRotation(a);
		
		r.sub(a);
		r.scale(1f/15);
		a.add(r);
		temp.setRotation(a);
		ghostObject.setWorldTransform(temp);
		
		velocity = QuaternionUtil.quatRotate(a, velocity, new Vector3f());
		velocity.scale(0.98f);
		character.setWalkDirection(velocity);
		lastPos = getPos();
	}
	
	@Override
	protected void setupBody(Vector3f start)
	{
		CollisionShape shape = getCollisionShape();
 	    Vector3f localInertia = new Vector3f(0, 0, 0);
	    shape.calculateLocalInertia(getMass(), localInertia);
	    // Transform
	    Transform startTransform = new Transform();
	    startTransform.setIdentity();
	    startTransform.origin.set(start);
	    // MotionState & body
	    ghostObject = new PairCachingGhostObject();
		getGhostObject().setWorldTransform(startTransform);
		level.getAxisSweep().getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		ConvexShape capsule = (ConvexShape) getCollisionShape();
		getGhostObject().setCollisionShape(capsule);
		getGhostObject().setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);
		float stepHeight = 0.35f;
		setCharacter(new KinematicCharacterController(getGhostObject(), capsule, stepHeight));
		character.setJumpSpeed(7.5f);
	}

	@Override
	protected CollisionShape getCollisionShape()
	{
		return model.body();
	}
	
	@Override
	protected float getFrame()
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

	private float[] modelMatrix = new float[16];
	
	@Override
	public float[] getOpenGLMatrix()
	{
		ghostObject.getWorldTransform(temp).getOpenGLMatrix(modelMatrix);
		return modelMatrix;
	}
	
	private Transform temp = new Transform();
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
		
		velocity.scale(0.2f);
	}

	public Vector3f getPos()
	{
		return ghostObject.getWorldTransform(temp).origin;
	}

	public Matrix4f getModelMatrix()
	{
		return ghostObject.getWorldTransform(temp).getMatrix(new javax.vecmath.Matrix4f());
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

	public void input(int key, float yaw)
	{
		switch(key)
		{
			case Keyboard.KEY_SPACE:
				character.jump();
				break;
		}
	}

}

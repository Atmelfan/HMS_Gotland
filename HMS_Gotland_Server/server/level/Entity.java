package level;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;


public class Entity implements DrawableEntity{
	public static int entityIDs = 0;
	public int entityID = entityIDs++;

	protected CollisionObject body;// Physics body
	protected Motionstate motionstate;

	private Transform tempTransform = new Transform();
	private Level level;
	
	private EntityConstructInfo info;
	private int health = 100;

	public Entity(Level level) {
		this.level = level;
	}
	
	public Entity(Level level, EntityConstructInfo info2) {
		this.level = level;
		health = info2.health;
	}

	public DynamicsWorld getWorld() {
		return level.level;
	}

	public float getMass() {
		return 1f;
	}

	public String getModelName() {
		return "";
	}

	public float getFrame() {
		return 0f;
	}

	public Transform getWorldTransform() {
		body.getWorldTransform(tempTransform);
		return tempTransform;
	}

	public void setWorldTransform(Transform trans) {
		body.setWorldTransform(trans);
	}

	public Vector3f getPos() {
		return motionstate.getWorldTransform().origin;
	}

	public void setPos(Vector3f vector3f) {
		motionstate.getWorldTransform().origin.set(vector3f);
	}

	private float[] tmatrix = new float[16];

	public float[] getModelMatrix() {
		motionstate.getWorldTransform().getOpenGLMatrix(tmatrix);
		return tmatrix;
	}

	/**
	 * @return the body
	 */
	public CollisionObject getBody() {
		return body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(CollisionObject body) {
		this.body = body;
	}

	public void processTag(String tag) {
	}

	public Quat4f getAngle() {
		return getWorldTransform().getRotation(new Quat4f());
	}

	public void tick(float dt) 
	{
	}

	public void tickAI(float dt)
	{
	}
	
	@Override
	public Transform getTransform() {
		return new Transform(getWorldTransform());
	}
	
	public static class Motionstate extends MotionState
	{
		public Transform trans;
		public Motionstate(Transform startTransform) {
			trans = startTransform;
		}

		@Override
		public Transform getWorldTransform(Transform arg0) {
			arg0.set(trans);
			return arg0;
		}

		public Transform getWorldTransform() {
			return trans;
		}

		@Override
		public void setWorldTransform(Transform arg0) {
			trans.set(arg0);
		}
		
	}

	@Override
	public Vector3f getPosition()
	{
		return new Vector3f(motionstate.getWorldTransform().origin);
	}

}

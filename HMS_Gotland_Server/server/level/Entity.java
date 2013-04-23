package level;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;


public class Entity implements DrawableEntity{
	public static int entityIDs = 0;
	public int entityID = entityIDs++;

	protected CollisionObject body;// Physics body
	protected EntityMotionState motionstate;

	private Transform tempTransform = new Transform();
	private Level level;
	private int health = 100;

	public Entity(Level level) {
		this.level = level;
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
		String[] cmds = tag.split(" ");
		if (cmds[0].equalsIgnoreCase("&pos") && cmds.length > 3) {
			setPos(new Vector3f(Float.valueOf(cmds[1]), Float.valueOf(cmds[2]),
					Float.valueOf(cmds[3])));
		}
		if (cmds[0].equalsIgnoreCase("&phys") && cmds.length > 3) {
			BoxShape shape = new BoxShape(new Vector3f(Float.valueOf(cmds[1]),
					Float.valueOf(cmds[2]), Float.valueOf(cmds[3])));
			Vector3f localInertia = new Vector3f(0, 0, 0);
			shape.calculateLocalInertia(getMass(), localInertia);
			// Transform
			Transform startTransform = new Transform();
			startTransform.setIdentity();
			startTransform.origin.set(new Vector3f(0, 0, 0));
			// MotionState & body
			motionstate = new EntityMotionState(startTransform);
			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(
					getMass(), motionstate, shape, localInertia);
			body = new RigidBody(rbInfo);
			body.setRestitution(0.1f);
			body.setFriction(0.50f);

			// Associate this entity with the body and collisionshape
			body.setUserPointer(this);
			shape.setUserPointer(this);
		}
	}

	public Quat4f getAngle() {
		return getWorldTransform().getRotation(new Quat4f());
	}

	public void tick(float dt) {

	}

	@Override
	public Transform getTransform() {
		return new Transform(getWorldTransform());
	}
}

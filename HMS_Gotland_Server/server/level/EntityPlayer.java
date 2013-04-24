package level;

import java.io.File;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

public class EntityPlayer extends Entity {
	public String username = "player";

	public EntityPlayer(Level level, String name) {
		super(level);
		ObjCollisionShape sh = new ObjCollisionShape(new File(
				"Resources/assets/models/", getModelName()), false);
		BoxShape shape = new BoxShape(new Vector3f(sh.getXWidth() / 2,
				sh.getYHeight() / 2, sh.getZDepth() / 2));
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
		username = name;
	}

	@Override
	public void processTag(String tag) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see entity.Entity#getEntityModelName()
	 */
	@Override
	public String getModelName() {
		return "lara/Lara_Croft.obj";
	}

	public void angle(float angle, Vector3f axis) {
		Transform tr = new Transform();

		body.getWorldTransform(tr);
		MatrixUtil.setEulerZYX(tr.basis, angle * axis.x, angle * axis.y, angle
				* axis.z);
		body.setWorldTransform(tr);
	}

}

package level;
import javax.vecmath.Vector3f;


import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;


public class EntityPlayer extends Entity
{
	public String username = "player";

	public EntityPlayer(Level level, String name)
	{
		super(level);
		BoxShape shape = new BoxShape(new Vector3f(1, 1.7f, 1));
 	    Vector3f localInertia = new Vector3f(0, 0, 0);
	    shape.calculateLocalInertia(getMass(), localInertia);
	    // Transform
	    Transform startTransform = new Transform();
	    startTransform.setIdentity();
	    startTransform.origin.set(new Vector3f(0, 0, 0));
	    // MotionState & body
	    motionstate = new EntityMotionState(startTransform);
	    RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(getMass(), motionstate, shape, localInertia);
	    body = new RigidBody(rbInfo);
	    body.setRestitution(0.1f);
	    body.setFriction(0.50f);
	    body.setDamping(0f, 0f);
	    
	    //Associate this entity with the body and collisionshape
	    body.setUserPointer(this);
	    shape.setUserPointer(this);
		username = name;
	}


	/* (non-Javadoc)
	 * @see level.Entity#processTag(java.lang.String)
	 */
	@Override
	public void processTag(String tag)
	{
	}


	/* (non-Javadoc)
	 * @see entity.Entity#getEntityModelName()
	 */
	@Override
	public String getEntityModelName()
	{
		return "war.md2";
	}


	public void move(Vector3f pos)
	{
		body.applyCentralForce(pos);
	}
	
	public void angle(float angle, Vector3f axis)
	{
		Transform tr = new Transform();
		
		body.getWorldTransform(tr);
		MatrixUtil.setEulerZYX(tr.basis, angle * axis.x, angle * axis.y, angle * axis.z);
		body.setWorldTransform(tr);
	}

}

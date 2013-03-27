package level;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;


import model.Model;

import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
import com.bulletphysics.collision.dispatch.CollisionWorld.RayResultCallback;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;

import hms_gotland_client.RenderEngine;

public class Entity
{
	public static int entityIDs = 0;
	public int entityID = entityIDs++;
	
	protected RigidBody body;//Physics body
	protected EntityMotionState motionstate;
	
	//Temp stuff for retrieving OpenGL matrix from body
	private Transform tempTransform = new Transform();
	private Level level;
	private int health = 5;
	private int t;

	public Entity(Level level)
	{
		this.level = level;
	}
	
	public void collisionCallback(Entity entityCollided)
	{
		
	}
	
	public void shoot(Vector3f target)
	{
		CollisionWorld.ClosestRayResultCallback m = new CollisionWorld.ClosestRayResultCallback(getPos(), target);
		
		getWorld().rayTest(getPos(), target, m);
		
		if(m.hasHit())
		{
			if(m.collisionObject != null && m.collisionObject.getUserPointer() instanceof Entity)
			{
				((Entity)m.collisionObject.getUserPointer()).health--;
			}
			else
			{
				Vector3f pos = m.hitPointWorld;
				Vector3f normal = m.hitNormalWorld;
				//TODO create bullet hole & particles
			}
		}
		
	}
	
	public DynamicsWorld getWorld()
	{
		return level.level;
	}
	
	public void tick()
	{
		t++;
		if(t > 60 * 34) t = 0;
	}
	
	protected static float getMass()
	{
		return 1f;
	}
	
	public String getEntityModelName()
	{
		return "gpa_robotics_war.obj";
	}
	
	protected float getFrame()
	{
		return t/60f;
	}

	public Transform getWorldTransform()
	{
		body.getWorldTransform(tempTransform);
		return tempTransform;
	}
	
	public void setWorldTransform(Transform trans)
	{
		body.setWorldTransform(trans);
	}
	
	public Vector3f getPos()
	{
		return motionstate.getWorldTransform().origin;
	}
	
	public void setPos(Vector3f vector3f)
	{
		body.translate(vector3f);
	}

	private float[] tmatrix = new float[16];
	public float[] getModelMatrix()
	{
		motionstate.getWorldTransform().getOpenGLMatrix(tmatrix);
		return tmatrix;
	}
	
	/**
	 * @return the body
	 */
	public RigidBody getBody()
	{
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(RigidBody body)
	{
		this.body = body;
	}

	public void processTag(String tag)
	{
		String[] cmds = tag.split(" ");
		if(cmds[0].equalsIgnoreCase("&pos") && cmds.length > 3)
		{
			setPos(new Vector3f(Float.valueOf(cmds[1]), Float.valueOf(cmds[2]), Float.valueOf(cmds[3])));
		}
		if(cmds[0].equalsIgnoreCase("&phys") && cmds.length > 3)
		{
			BoxShape shape = new BoxShape(new Vector3f(Float.valueOf(cmds[1]), Float.valueOf(cmds[2]), Float.valueOf(cmds[3])));
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
		}
	}

	public Quat4f getAngle()
	{
		return getWorldTransform().getRotation(new Quat4f());
	}	
}

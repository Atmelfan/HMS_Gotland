package entity;

import javax.vecmath.Vector3f;

import level.Level;

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
	private Model model;//Model to draw
	protected RigidBody body;//Physics body
	protected EntityMotionState motionstate;
	
	//Temp stuff for retrieving OpenGL matrix from body
	private Transform tempTransform = new Transform();
	private Level level;
	private int health = 5;
	private int t;

	public Entity(Level level, Vector3f pos, float mass)
	{
		this.level = level;
		setModel(level.renderEngine.getModel(getEntityModelName()));
		// Collision form
		BoxShape shape = new BoxShape(new Vector3f(getModel().getXWidth(), getModel().getYHeight(), getModel().getZDepth()));
 	    Vector3f localInertia = new Vector3f(0, 0, 0);
	    shape.calculateLocalInertia(getMass(), localInertia);
	    // Transform
	    Transform startTransform = new Transform();
	    startTransform.setIdentity();
	    startTransform.origin.set(pos);
	    // MotionState & body
	    motionstate = new EntityMotionState(startTransform);
	    RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, motionstate, shape, localInertia);
	    body = new RigidBody(rbInfo);
	    body.setRestitution(0.1f);
	    body.setFriction(0.50f);
	    body.setDamping(0f, 0f);
	    
	    //Associate this entity with the body and collisionshape
	    body.setUserPointer(this);
	    shape.setUserPointer(this);
	}
	
	public Entity(Level level, Vector3f pos)
	{
		this(level, pos, getMass());
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
	
	protected String getEntityModelName()
	{
		return "gpa_robotics_war.obj";
	}

	public void draw(RenderEngine renderEngine)
	{
		float[] temp = new float[16];
		body.getWorldTransform(new Transform()).getOpenGLMatrix(temp);
		model.draw(0, renderEngine.getViewProjectionMatrix(), temp);
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

	/**
	 * @return the model
	 */
	public Model getModel()
	{
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(Model model)
	{
		this.model = model;
	}

	public void processTag(String tag)
	{
		String[] cmds = tag.split(" ");
		if(cmds[0].equalsIgnoreCase("pos") && cmds.length > 3)
		{
			setPos(new Vector3f(Float.valueOf(cmds[1]), Float.valueOf(cmds[2]), Float.valueOf(cmds[3])));
		}
	}	
}

package entity;

import javax.vecmath.Vector3f;

import level.Level;


import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;

import hms_gotland_core.HMS_Gotland;
import Renderers.Model;

public class Entity
{
	private Model model;//Model to draw
	protected RigidBody body;//Physics body
	protected EntityMotionState motionstate;
	
	//Temp stuff for retrieving OpenGL matrix from body
	private Transform tempTransform = new Transform();

	public Entity(Level level, Vector3f pos)
	{
		setModel(level.renderEngine.getModel(getEntityModelName()));
		
		BoxShape shape = new BoxShape(new Vector3f(getModel().getXWidth(), getModel().getYHeight(), getModel().getZDepth()));
 	    Vector3f localInertia = new Vector3f(0,0,0);
	    shape.calculateLocalInertia(getMass(), localInertia);

	    Transform startTransform = new Transform();
	    startTransform.setIdentity();
	    startTransform.origin.set(pos);
	    
	    motionstate = new EntityMotionState(startTransform);
	    
	    RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(getMass(), motionstate, shape, localInertia);
	    body = new RigidBody(rbInfo);
	    body.setRestitution(0.1f);
	    body.setFriction(0.50f);
	    body.setDamping(0f, 0f);
	}
	
	public void tick()
	{
		
	}
	
	protected float getMass()
	{
		return 1f;
	}
	
	protected String getEntityModelName()
	{
		return "default.obj";
	}

	public void draw()
	{
		motionstate.uploadOpenGLMatrix(HMS_Gotland.modelMatrixLocation);
		getModel().draw();
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

	public float[] getModelMatrix()
	{
		float[] matrix = new float[16];
		motionstate.getWorldTransform(new Transform()).getOpenGLMatrix(matrix);
		return matrix;
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
}

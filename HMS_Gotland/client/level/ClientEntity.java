package level;


import hms_gotland_client.RenderEngine;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;


import level.EntityMotionState;
import level.Level;
import model.Model;

public class ClientEntity
{
	public int id;
	public Model model;
	public int frame;
	
	public RigidBody body;
	protected EntityMotionState motionstate;
	
	public ClientEntity(ClientLevel lvl, String model, int id)
	{
		this.model = lvl.renderEngine.getModel(model);
		this.id = id;
		body = setupBody();
	}
	
	protected RigidBody setupBody()
	{
		BoxShape shape = new BoxShape(new Vector3f(model.getXWidth() / 2, model.getYHeight() / 2, model.getZDepth() / 2));
 	    Vector3f localInertia = new Vector3f(0, 0, 0);
	    shape.calculateLocalInertia(getMass(), localInertia);
	    // Transform
	    Transform startTransform = new Transform();
	    startTransform.setIdentity();
	    startTransform.origin.set(new Vector3f());
	    // MotionState & body
	    motionstate = new EntityMotionState(startTransform);
	    RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(getMass(), motionstate, shape, localInertia);
	    RigidBody tbody = new RigidBody(rbInfo);
	    tbody.setRestitution(0.1f);
	    tbody.setFriction(getFriction());
	    tbody.setDamping(0f, 0f);
	    
	    //Associate this entity with the body and collisionshape
	    tbody.setUserPointer(this);
	    shape.setUserPointer(this);
	    return tbody;
	}
	
	protected  float getFriction()
	{
		return 0.5f;
	}

	protected  float getMass()
	{
		return 1;
	}
	
	private float[] modelMatrix = new float[16];
	public void draw(RenderEngine engine)
	{
		motionstate.getWorldTransform().getOpenGLMatrix(modelMatrix);
		model.draw(frame, engine.getViewProjectionMatrix(), modelMatrix, engine);
	}
	
	public float[] getOpenGLMatrix()
	{
		motionstate.getWorldTransform().getOpenGLMatrix(modelMatrix);
		return modelMatrix;
	}
}

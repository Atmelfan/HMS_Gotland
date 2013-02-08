package hms_gotland_client;


import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;

import entity.EntityMotionState;

import level.Level;
import model.Model;

public class ClientEntity
{
	public int id;
	public Model model;
	public int frame;
	
	public RigidBody body;
	private EntityMotionState motionstate;
	
	public ClientEntity(ClientLevel lvl, String model, int id)
	{
		this.model = lvl.renderEngine.getModel(model);
		this.id = id;
		BoxShape shape = new BoxShape(new Vector3f(this.model.getXWidth(), this.model.getYHeight(), this.model.getZDepth()));
 	    Vector3f localInertia = new Vector3f(0, 0, 0);
	    shape.calculateLocalInertia(getMass(), localInertia);
	    // Transform
	    Transform startTransform = new Transform();
	    startTransform.setIdentity();
	    startTransform.origin.set(new Vector3f());
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

	private float getMass()
	{
		return 1;
	}
	
	private float[] modelMatrix = new float[16];
	public void draw(RenderEngine engine)
	{
		motionstate.getWorldTransform().getOpenGLMatrix(modelMatrix);
		model.draw(frame, engine.getViewProjectionMatrix(), modelMatrix);
	}
}

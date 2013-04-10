package level;


import hms_gotland_client.RenderEngine;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.linearmath.Transform;


import level.EntityMotionState;
import level.Level;
import model.Model;

public class ClientEntity
{
	public int id;
	protected ClientLevel level;
	public Model model;
	public float frame;
	
	public RigidBody body = null;
	protected EntityMotionState motionstate;
	
	public ClientEntity(ClientLevel lvl, String model, int id)
	{
		this.model = lvl.renderEngine.getModel(model);
		this.id = id;
		level = lvl;
		setupBody(new Vector3f());
	}
	
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
	    motionstate = new EntityMotionState(startTransform);
	    RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(getMass(), motionstate, shape, localInertia);
	    RigidBody tbody = new RigidBody(rbInfo);
	    tbody.setRestitution(0.1f);
	    tbody.setFriction(getFriction());
	    tbody.setDamping(0f, 0f);
	    
	    //Associate this entity with the body and collisionshape
	    tbody.setUserPointer(this);
	    shape.setUserPointer(this);
	    body = tbody;
	}

	protected CollisionShape getCollisionShape()
	{
		return model.body();
	}
	
	protected  float getFriction()
	{
		return 0.5f;
	}

	protected  float getMass()
	{
		return 1;
	}
	
	public Vector3f getPos()
	{
		return motionstate.getWorldTransform().origin;
	}

	public Matrix4f getModelMatrix()
	{
		return motionstate.getWorldTransform().getMatrix(new Matrix4f());
	}
	
	private float[] modelMatrix = new float[16];
	public void draw(RenderEngine engine)
	{
		model.draw(getFrame(), engine.getViewProjectionMatrix(), getOpenGLMatrix(), engine);
	}
	
	protected float getFrame()
	{
		return frame;
	}

	public float[] getOpenGLMatrix()
	{
		motionstate.getWorldTransform().getOpenGLMatrix(modelMatrix);
		return modelMatrix;
	}

	public void tick()
	{
	}

	public float rayTrace(float thirdPersonRadius, float yaw, float pitch)
	{
		return thirdPersonRadius;
	}

	public void addYaw(float f)
	{
	}
}

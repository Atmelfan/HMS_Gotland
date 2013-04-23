package level;


import hms_gotland_client.RenderEngine;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;


import level.EntityMotionState;
import level.BaseLevel;
import model.Model;

public class ClientEntity implements DrawableEntity
{
	public int id;
	protected BaseLevel level;
	public String model;
	public float frame;
	
	public RigidBody body = null;
	protected EntityMotionState motionstate;
	
	public ClientEntity(BaseLevel lvl, String model, int id)
	{
		this.id = id;
		level = lvl;
		this.model = model;
		setupBody(new Vector3f());
	}
	
	protected void setupBody(Vector3f start)
	{
		CollisionShape shape = new BoxShape(new Vector3f());
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
	    body.setCollisionFlags(body.getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);
	    body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
	    tbody.setRestitution(0.1f);
	    tbody.setFriction(getFriction());
	    tbody.setDamping(0f, 0f);
	    
	    //Associate this entity with the body and collisionshape
	    tbody.setUserPointer(this);
	    shape.setUserPointer(this);
	    body = tbody;
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
		return new Vector3f(getTempTransform().origin);
	}
	
	public Vector3f getVel()
	{
		return body.getInterpolationLinearVelocity(new Vector3f());
	}
	
	public Vector3f getOri()
	{
		return new Vector3f();
	}

	public Matrix4f getModelMatrix()
	{
		return getTempTransform().getMatrix(new Matrix4f());
	}
	
	public float getFrame()
	{
		return frame;
	}

	private float[] modelMatrix = new float[16];
	public float[] getOpenGLMatrix()
	{
		getTempTransform().getOpenGLMatrix(modelMatrix);
		return modelMatrix;
	}

	public void tick()
	{
	}

	public float rayTrace(float thirdPersonRadius, float yaw, float pitch)
	{
		return thirdPersonRadius;
	}
	
	//Getters/setters for the entity's transform
	protected Transform temp = new Transform();
	public Transform getTempTransform()
	{
		body.getWorldTransform(temp);
		return temp;
	}
	
	public void setTempTransform() {
		body.setWorldTransform(temp);
	}

	public void position(Vector3f origin, Quat4f basis) 
	{
		motionstate.position(origin, basis);
	}

	@Override
	public String getModelName() {
		return model;
	}

	@Override
	public Transform getTransform() {
		return motionstate.getWorldTransform();
	}
}

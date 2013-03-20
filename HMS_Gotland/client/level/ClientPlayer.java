package level;


import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;



public class ClientPlayer extends ClientEntity
{

	public ClientPlayer(ClientLevel lvl, String model, int id)
	{
		super(lvl, model, id);
	}

	public void move(Vector3f vector3f)
	{
		body.applyCentralForce(vector3f);
	}

	public Vector3f getPos()
	{
		return motionstate.getWorldTransform().origin;
	}

	public Matrix4f getModelMatrix()
	{
		return motionstate.getWorldTransform().getMatrix(new javax.vecmath.Matrix4f());
	}
	

}

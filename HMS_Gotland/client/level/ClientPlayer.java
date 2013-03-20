package level;


import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.character.KinematicCharacterController;


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
	

}

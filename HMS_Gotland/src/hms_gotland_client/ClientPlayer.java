package hms_gotland_client;

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
		return null;
	}
	

}

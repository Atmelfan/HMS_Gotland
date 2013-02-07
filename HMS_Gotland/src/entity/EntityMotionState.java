package entity;

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

public class EntityMotionState extends MotionState
{
	private Transform transform = new Transform();

	public EntityMotionState(Transform startTransform)
	{
		transform = startTransform;
	}

	@Override
	public Transform getWorldTransform(Transform tr)
	{
		tr.set(transform);
		return tr;
	}
	
	public Transform getWorldTransform()
	{
		return transform;
	}

	@Override
	public void setWorldTransform(Transform arg0)
	{
		transform.set(arg0);
	}
	
}

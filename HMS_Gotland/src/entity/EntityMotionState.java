package entity;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

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
	public Transform getWorldTransform(Transform arg0)
	{
		arg0.set(transform);
		return arg0;
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

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
	
	public void uploadOpenGLMatrix(int location)
	{
		//Allocate an float array to keep the matrix
		float[] matrix = new float[16];
		transform.getOpenGLMatrix(matrix);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(matrix.length);
		buffer.put(matrix); buffer.flip();
		//Upload matrix to shader
		GL20.glUniformMatrix4(location, false, buffer);
	}
	
}

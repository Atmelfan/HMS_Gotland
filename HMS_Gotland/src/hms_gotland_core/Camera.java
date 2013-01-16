/**
 * HMS_Gotland/Camera.java - 27 dec 2012:23:40:51
 * (c) Gustav 'Atmelfan' Palmqvist 2012
 */
package hms_gotland_core;

import java.nio.FloatBuffer;

import javax.vecmath.Quat4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Util.GLUtil;
import Util.VectorUtil;

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

import entity.Entity;

/**
 * @author Atmelfan
 * OpenGL 3.x compatible camera
 */
public class Camera
{
	public boolean thirdPerson = false;
	public float thirdPersonRadius = 10F;
	
	public Entity owner;
	
	public Vector3f pos = new Vector3f();
	public Vector3f angle = new Vector3f();
	
	private Matrix4f projectionMatrix;// view matrix
	private Matrix4f viewMatrix;// projection matrix
	private FloatBuffer tempBuffer;

	static final int VIEW_MATRIX          = 1;// view matrix id
	static final int PROJECTION_MATRIX    = 2;// projection matrix id
	static final int THE_MATRIX           = 3;// ???
	
	public Camera(float width, float height, float near, float far)
	{
		projectionMatrix = new Matrix4f();
		viewMatrix = new Matrix4f();
		tempBuffer = BufferUtils.createFloatBuffer(16);
		setupMatrices(width, height, near, far);
	}
	
	public void update()
	{
		viewMatrix = new Matrix4f();
		
		if(thirdPerson)
		{
			viewMatrix.translate(new Vector3f(0F, 0F, -thirdPersonRadius));
		}
		
		viewMatrix.rotate((float) Math.toRadians(angle.z), new Vector3f(0, 0, 1));
		viewMatrix.rotate((float) Math.toRadians(angle.y), new Vector3f(0, 1, 0));
		viewMatrix.rotate((float) Math.toRadians(angle.x), new Vector3f(1, 0, 0));
		
		if(owner != null)
		{
			viewMatrix.translate(VectorUtil.toLWJGLVector(owner.getPos()));
		}else
		{
			viewMatrix.translate(pos);
		}
		
	}

	private void setupMatrices(float width, float height, float near, float far)
	{
		float fieldOfView = 60f;
		float aspectRatio = width / height;
		
		float y_scale = coTangent(Math.toRadians(fieldOfView / 2f));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = far - near;
		
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((far + near) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * near * far) / frustum_length);
		
	}

	private float coTangent(double radians)
	{
		return (float)(1/Math.tan(radians));
	}
	
	public void uploadBuffer(int matrixPos, int targetMatrix)
	{
		switch(targetMatrix)
		{
		case VIEW_MATRIX:
			viewMatrix.store(tempBuffer); tempBuffer.flip();
			GL20.glUniformMatrix4(matrixPos, false, tempBuffer);
			break;
		case PROJECTION_MATRIX:
			projectionMatrix.store(tempBuffer); tempBuffer.flip();
			GL20.glUniformMatrix4(matrixPos, false, tempBuffer);
			break;
		case THE_MATRIX:
			System.err.println("Error, insufficent power.");
			break;
		default:
			break;
		}
	}

	/**
	 * @return the thirdPerson
	 */
	public boolean isThirdPerson()
	{
		return thirdPerson;
	}

	/**
	 * @param thirdPerson the thirdPerson to set
	 */
	public void setThirdPerson(boolean thirdPerson)
	{
		this.thirdPerson = thirdPerson;
	}

	/**
	 * @return the thirdPersonRadius
	 */
	public float getThirdPersonRadius()
	{
		return thirdPersonRadius;
	}

	/**
	 * @param thirdPersonRadius the thirdPersonRadius to set
	 */
	public void setThirdPersonRadius(float thirdPersonRadius)
	{
		this.thirdPersonRadius = thirdPersonRadius;
	}

	/**
	 * @return the pos
	 */
	public Vector3f getPos()
	{
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(Vector3f pos)
	{
		this.pos = pos;
	}

	/**
	 * @return the angle
	 */
	public Vector3f getAngle()
	{
		return angle;
	}

	/**
	 * @param angle the angle to set
	 */
	public void setAngle(Vector3f angle)
	{
		this.angle = angle;
	}
}
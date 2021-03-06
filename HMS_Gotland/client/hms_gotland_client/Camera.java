/**
 * HMS_Gotland/Camera.java - 27 dec 2012:23:40:51
 * (c) Gustav 'Atmelfan' Palmqvist 2012
 */
package hms_gotland_client;

import java.nio.FloatBuffer;
import level.DrawableEntity;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Util.GLUtil;
import Util.VectorUtil;



/**
 * @author Atmelfan
 * OpenGL 3.x compatible camera
 */
public class Camera
{
	public boolean thirdPerson = true;
	public float thirdPersonRadius = 3F;
	
	public DrawableEntity owner;
	
	public Vector3f pos = new Vector3f(0, 0, 0);
	
	private Matrix4f projectionMatrix;// view matrix
	private Matrix4f viewMatrix;// projection matrix
	public float yaw = 0;
	public float pitch = 0;
	public float roll = 0;
	
	private float[] matrix = new float[16];
	
	public Camera(float width, float height, float near, float far)
	{
		projectionMatrix = new Matrix4f();
		viewMatrix = new Matrix4f();
		setupMatrices(width, height, near, far);
	}
	
	public void update()
	{
		viewMatrix = new Matrix4f();
		if(thirdPerson && owner != null)
		{
			viewMatrix.translate(new Vector3f(0f, 0f, 6f));
		}
		viewMatrix.rotate((float) Math.toRadians(pitch), new Vector3f(1, 0, 0));
		viewMatrix.rotate((float) Math.toRadians(180 + yaw), new Vector3f(0, 1, 0));
		//viewMatrix.rotate((float) Math.toRadians(180), new Vector3f(0, 0, 1));
		
		if(owner != null)
		{
			//Matrix4f v = GLUtil.lwjglMatrix(owner.getModelMatrix());
			//v.invert();
			//Matrix4f.mul(viewMatrix, v, viewMatrix);
			Vector3f o = VectorUtil.toLWJGLVector(owner.getPosition());
			o.negate();
			viewMatrix.translate(o, viewMatrix);
		}else
		{
			viewMatrix.translate(pos);
		}
		
		Matrix4f v = new Matrix4f();
		Matrix4f.mul(projectionMatrix, viewMatrix, v);
		FloatBuffer f = BufferUtils.createFloatBuffer(16);
		v.store(f);
		
		f.rewind();
		f.get(matrix, 0, matrix.length);
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

	public float[] getViewProjectionMatrix()
	{
		return matrix;
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
	 * @return the owner
	 */
	public DrawableEntity getOwner()
	{
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(DrawableEntity owner)
	{
		this.owner = owner;
	}

	public void addYaw(float yaw)
	{
		this.yaw += yaw;
	}
	
	public void adPitch(float pitch)
	{
		this.pitch += pitch;
	}
}

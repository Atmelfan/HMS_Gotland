package model;

import java.nio.FloatBuffer;

import javax.vecmath.Tuple3f;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class GLLightSource
{
	public Matrix4f lightMatrix;
	public Tuple3f color;
	
	public GLLightSource(Vector3f pos, float x, float y, float z)
	{
		lightMatrix = new Matrix4f();
		lightMatrix.rotate(x, new Vector3f(1, 0, 0));
		lightMatrix.rotate(y, new Vector3f(0, 1, 0));
		lightMatrix.rotate(z, new Vector3f(0, 0, 1));
		lightMatrix.translate(pos);
	}
	
	public void set(Vector3f pos, float x, float y, float z)
	{
		lightMatrix = new Matrix4f();
		lightMatrix.rotate(x, new Vector3f(1, 0, 0));
		lightMatrix.rotate(y, new Vector3f(0, 1, 0));
		lightMatrix.rotate(z, new Vector3f(0, 0, 1));
		lightMatrix.translate(pos);
	}
	
	public FloatBuffer getElements(FloatBuffer floatbuffer)
	{
		lightMatrix.store(floatbuffer);
		float[] col = new float[3];
		color.get(col);
		floatbuffer.put(col);
		return floatbuffer;
	}
}

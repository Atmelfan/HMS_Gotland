package hms_gotland_client;

import javax.vecmath.Tuple3f;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class LightSource
{
	public Matrix4f lightMatrix;
	public Tuple3f color;
	public Tuple3f cone;
	
	public LightSource(Vector3f pos, float x, float y, float z)
	{
		lightMatrix = new Matrix4f();
		lightMatrix.rotate(x, new Vector3f(1, 0, 0));
		lightMatrix.rotate(y, new Vector3f(0, 1, 0));
		lightMatrix.rotate(z, new Vector3f(0, 0, 1));
		lightMatrix.translate(pos);
	}
	
	public void set()
	{
		
	}
}

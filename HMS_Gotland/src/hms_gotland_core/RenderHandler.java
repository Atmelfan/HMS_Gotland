package hms_gotland_core;


import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;

public class RenderHandler
{
	public RenderHandler(HMS_Gotland hms_Gotland)
	{
		// Setup projection matrix
		float fieldOfView = 60f;
		float aspectRatio = (float)Display.getWidth() / (float)Display.getHeight();
		float near_plane = 0.1f;
		float far_plane = 100f;

		float y_scale = coTangent(Math.toRadians(fieldOfView / 2f));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = far_plane - near_plane;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((far_plane + near_plane) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * near_plane * far_plane) / frustum_length);
	}
	
	public static Matrix4f projectionMatrix = new Matrix4f();//Global projection Matrix
	public static Matrix4f viewMatrix       = new Matrix4f();//Global view Matrix
	
	private float coTangent(double d)
	{
		return (float)(1/Math.tan(d));
	}
}

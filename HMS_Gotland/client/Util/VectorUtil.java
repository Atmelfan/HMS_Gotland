package Util;

import javax.vecmath.Vector3f;



public class VectorUtil
{
	public static org.lwjgl.util.vector.Vector3f toLWJGLVector(javax.vecmath.Vector3f in)
	{
		return new org.lwjgl.util.vector.Vector3f(in.x, in.y, in.z);
	}

	public static javax.vecmath.Vector3f toBulletVector(org.lwjgl.util.vector.Vector3f in)
	{
		return new javax.vecmath.Vector3f(in.x, in.y, in.z);
	}
	
	public static float distance(Vector3f a, Vector3f b)
	{
		float x = a.x - b.x;
		float y = a.y - b.y;
		float z = a.z - b.z;
		
		return (x * x) + (y * y) + (z * z);
	}
	
	public static float distance2(Vector3f a, Vector3f b)
	{
		return (float) Math.sqrt(distance(a, b));
	}
}


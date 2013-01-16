package Util;

import javax.vecmath.Vector3f;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;



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
}


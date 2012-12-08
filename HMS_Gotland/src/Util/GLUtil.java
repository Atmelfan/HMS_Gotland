package Util;



public class GLUtil
{
	/**
	 * Sight...
	 * @param from
	 * @return Vecmath vector from lwjgl vector
	 */
	public static javax.vecmath.Matrix4f jBulletVector(org.lwjgl.util.vector.Matrix4f from)
	{
		return new javax.vecmath.Matrix4f(from.m00, from.m01, from.m02, from.m03, 
										  from.m10, from.m11, from.m12, from.m13, 
										  from.m20, from.m21, from.m22, from.m23, 
										  from.m30, from.m31, from.m32, from.m33);
	}
	
	/**
	 * Sight...
	 * @param from
	 * @return Lwjgl vector from vecmath vector
	 */
	public static org.lwjgl.util.vector.Matrix4f lwjglVector(javax.vecmath.Matrix4f from)
	{
		//return new org.lwjgl.util.vector.Matrix4f(from); //Of course...
		org.lwjgl.util.vector.Matrix4f retur = new org.lwjgl.util.vector.Matrix4f();
		//FUCK!
		retur.m00 = from.m00; retur.m01 = from.m01; retur.m02 = from.m02; retur.m03 = from.m03;
		retur.m10 = from.m10; retur.m11 = from.m11; retur.m12 = from.m12; retur.m13 = from.m13;
		retur.m20 = from.m20; retur.m21 = from.m21; retur.m22 = from.m22; retur.m23 = from.m23;
		retur.m30 = from.m30; retur.m31 = from.m31; retur.m32 = from.m32; retur.m33 = from.m33;
		return retur;
	}
}

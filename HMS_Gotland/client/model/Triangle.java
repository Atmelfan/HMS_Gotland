package model;

public class Triangle
{
	private float[] elements = new float[3 * 8];
	
	public void setVertexPosition(int i, float x, float y, float z)
	{
		elements[i * 8 + 0] = x;
		elements[i * 8 + 1] = y;
		elements[i * 8 + 2] = z;
	}
	
	public void setVertexTextureCoord(int i, float s, float t)
	{
		elements[i * 8 + 3] = s;
		elements[i * 8 + 4] = t;
	}
	
	public void setVertexNormal(int i, float x, float y, float z)
	{
		elements[i * 8 + 5] = x;
		elements[i * 8 + 6] = y;
		elements[i * 8 + 7] = z;
	}
	
	public float[] getElements()
	{
		return elements;
	}
}

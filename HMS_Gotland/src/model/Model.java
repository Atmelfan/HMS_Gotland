package model;

public class Model
{
	public void draw(float frame, float[] vpMatrix, float[] modelMatrix)
	{
	}
	
	public void destroy()
	{
	}
	
	public float getXWidth()
	{
		return 1;
	}
	
	public float getYHeight()
	{
		return 1;
	}
	
	public float getZDepth() 
	{
		return 1;
	}

	//Default model isn't animated
	public boolean isAnimated()
	{
		return false;
	}
}

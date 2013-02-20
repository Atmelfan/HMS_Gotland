package model;

import hms_gotland_client.RenderEngine;

public class Model
{
	public void draw(float frame, float[] vpMatrix, float[] modelMatrix, RenderEngine engine)
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

	public void drawCEL(int frame, float[] viewProjectionMatrix, float[] modelMatrix, RenderEngine engine)
	{
	}
}

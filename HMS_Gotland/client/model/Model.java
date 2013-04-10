package model;

import java.io.File;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;

import hms_gotland_client.RenderEngine;

public class Model
{	
	protected RenderEngine renderer;
	public Model(RenderEngine rend, File resource)
	{
		renderer = rend;
	}
	
	public void draw(float frame, float[] vpMatrix, float[] mdMatrix, RenderEngine engine)
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

	public CollisionShape body()
	{	
		float width = (float) Math.sqrt(getXWidth() * getXWidth() + getZDepth() * getZDepth());
		return new CapsuleShape(width / 2, getYHeight());
	}
}

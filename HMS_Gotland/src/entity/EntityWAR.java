package entity;

import javax.vecmath.Vector3f;
import level.Level;

public class EntityWAR extends Entity
{	
	public EntityWAR(Level level, Vector3f pos)
	{
		super(level, pos);
	}

	@Override
	protected String getEntityModelName()
	{
		return "gpa_robotics_war.md2";
	}
}

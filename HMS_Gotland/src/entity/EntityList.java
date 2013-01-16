package entity;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.vecmath.Vector3f;

import level.Level;

public class EntityList
{
	private static HashMap<String, Class<? extends Entity>> entities = new HashMap<>();

	public static Entity getEntity(String name, Level level, Vector3f pos)
	{
		Class<? extends Entity> clazz = entities.get(name);
		
		try
		{
			return clazz.getConstructor(Level.class, Vector3f.class).newInstance(new Object[]{level, pos});
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	static
	{
		entities.put("default", Entity.class);
	}
}

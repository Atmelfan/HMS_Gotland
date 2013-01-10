package entity;

import hms_gotland_core.HMS_Gotland;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.vecmath.Vector3f;

public class EntityList
{
	private static HashMap<String, Class<? extends Entity>> entities = new HashMap<>();

	public static Entity getEntity(String name, HMS_Gotland game, Vector3f pos)
	{
		Class<? extends Entity> clazz = entities.get(name);
		
		try
		{
			return clazz.getConstructor(HMS_Gotland.class, Vector3f.class).newInstance(new Object[]{game, pos});
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

package level;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.vecmath.Vector3f;

public class EntityList {
	private static HashMap<String, Class<? extends Entity>> entities = new HashMap<String, Class<? extends Entity>>();

	public static Entity getEntity(String name, Level level) {
		Class<? extends Entity> clazz = entities.get(name);

		try {
			return clazz.getConstructor(Level.class).newInstance(
					new Object[] { level });
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	static {
		entities.put("entity", Entity.class);
	}
}

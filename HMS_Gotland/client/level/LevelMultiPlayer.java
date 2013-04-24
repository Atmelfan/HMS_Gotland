package level;

import java.io.File;
import java.util.List;

import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.linearmath.Transform;

public class LevelMultiPlayer implements DrawableLevel{

	@Override
	public boolean init(String ip) {
		return false;
	}

	@Override
	public boolean init(File level) {
		return false;
	}

	@Override
	public String getLevelModel() {
		return null;
	}

	@Override
	public List<DrawableEntity> getDrawableEntities()
	{
		return null;
	}

	@Override
	public void destroy()
	{
		
	}

	@Override
	public void tick()
	{
		// TODO Auto-generated method stub
		
	}
}

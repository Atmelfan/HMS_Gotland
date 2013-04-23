package level;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.bulletphysics.dynamics.DynamicsWorld;

public abstract class BaseLevel implements DrawableLevel{

	protected ClientPlayer player;
	protected DynamicsWorld world;
	public BaseLevel()
	{
		setupPhysics();
		setWorldValues(world);
	}

	public void tick()
	{
	}
	
	public void destroy()
	{
		
	}
	
	public void setupPhysics()
	{
	}
	
	public abstract void setWorldValues(DynamicsWorld world);
	
	@Override
	public abstract void init(String ip);

	@Override
	public abstract void init(File level);
	
	@Override
	public List<DrawableEntity> getDrawableEntities() {
		return new ArrayList<DrawableEntity>();
	}

	@Override
	public abstract String getLevelModel();

	public ClientPlayer getPlayer() {
		return player;
	}

	
}

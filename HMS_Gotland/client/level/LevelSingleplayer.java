package level;

import hms_gotland_server.HMS_Gotland_Server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bulletphysics.dynamics.DynamicsWorld;

public class LevelSingleplayer extends BaseLevel {

	private HMS_Gotland_Server server;
	private Level level;
	@Override
	public void setWorldValues(DynamicsWorld world) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void tick() {
		server.tick();
	}
	
	@Override
	public void destroy() {
		server.destroy();
	}

	@Override
	public List<DrawableEntity> getDrawableEntities() {
		return new ArrayList<DrawableEntity>(level.entities);
	}

	@Override
	public void init(String ip) {
	}

	@Override
	public void init(File level) {
		server = new HMS_Gotland_Server(level, true, 4321, 4322);
		this.level = server.level;
	}

	@Override
	public String getLevelModel() {
		return level.modelName;
	}

}

package level;


import java.io.File;
import java.util.List;

public interface DrawableLevel {
	public boolean init(String ip);
	
	public boolean init(File level);
	
	public List<DrawableEntity> getDrawableEntities();
	
	public String getLevelModel();

	public void destroy();

	public void tick();
}
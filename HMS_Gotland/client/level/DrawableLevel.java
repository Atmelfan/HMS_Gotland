package level;


import java.io.File;
import java.util.List;

public interface DrawableLevel {
	public void init(String ip);
	
	public void init(File level);
	
	public List<DrawableEntity> getDrawableEntities();
	
	public String getLevelModel();
}
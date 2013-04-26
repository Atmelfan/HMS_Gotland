package level;

import javax.vecmath.Vector3f;

public interface ControllableEntity extends DrawableEntity {
	public void playAnimation(String s);
	
	public void AIgoto(Vector3f to);
}

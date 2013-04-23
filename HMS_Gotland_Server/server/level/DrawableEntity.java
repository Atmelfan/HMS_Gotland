package level;

import com.bulletphysics.linearmath.Transform;

public interface DrawableEntity {
	public String getModelName();
	public Transform getTransform();
	public float getFrame();
}

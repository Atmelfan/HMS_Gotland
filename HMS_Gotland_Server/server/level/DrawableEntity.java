package level;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

public interface DrawableEntity {
	public String getModelName();
	public Transform getTransform();
	public float getFrame();
	public Vector3f getPosition();
}

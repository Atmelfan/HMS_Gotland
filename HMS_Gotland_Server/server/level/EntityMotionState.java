package level;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

public class EntityMotionState extends MotionState {
	private Transform transform = new Transform();

	public EntityMotionState(Transform startTransform) {
		transform = startTransform;
	}

	@Override
	public Transform getWorldTransform(Transform tr) {
		tr.set(transform);
		return tr;
	}

	public Transform getWorldTransform() {
		return transform;
	}

	@Override
	public void setWorldTransform(Transform arg0) {
		transform.set(arg0);
	}

	public void position(Vector3f origin, Quat4f basis) {
		transform.basis.set(basis);
		transform.origin.set(origin);
	}

}

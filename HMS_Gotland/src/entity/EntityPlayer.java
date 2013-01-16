package entity;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import level.Level;

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;

public class EntityPlayer extends Entity
{

	public EntityPlayer(Level level, Vector3f pos)
	{
		super(level, pos);
		body.setGravity(new Vector3f(0, 0, 0));
	}
	
	
	/* (non-Javadoc)
	 * @see entity.Entity#getMass()
	 */
	@Override
	protected float getMass()
	{
		return 1f;
	}


	public void move(Vector3f pos)
	{
		Vector3f res = QuaternionUtil.quatRotate(getWorldTransform().getRotation(new Quat4f()), pos, new Vector3f(1, 1, 1));
		body.applyCentralForce(res);
		System.out.println(res);
	}
	
	public void angle(float angle, Vector3f axis)
	{
		Transform tr = new Transform();
		
		body.getWorldTransform(tr);
		MatrixUtil.setEulerZYX(tr.basis, angle * axis.x, angle * axis.y, angle * axis.z);
		body.setWorldTransform(tr);
	}

	/* (non-Javadoc)
	 * @see entity.Entity#draw()
	 */
	@Override
	public void draw()
	{
		// Don't draw at all
		//super.draw();
	}

	/* (non-Javadoc)
	 * @see entity.Entity#getEntityModelName()
	 */
	@Override
	protected String getEntityModelName()
	{
		return "character.obj";
	}

}

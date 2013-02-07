package entity;
import javax.vecmath.Vector3f;

import level.Level;

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

public class EntityPlayer extends Entity
{

	public EntityPlayer(Level level, Vector3f pos)
	{
		super(level, pos);
	}


	/* (non-Javadoc)
	 * @see entity.Entity#getEntityModelName()
	 */
	@Override
	protected String getEntityModelName()
	{
		return "war.md2";
	}


	public void move(Vector3f pos)
	{
		body.applyCentralForce(pos);
	}
	
	public void angle(float angle, Vector3f axis)
	{
		Transform tr = new Transform();
		
		body.getWorldTransform(tr);
		MatrixUtil.setEulerZYX(tr.basis, angle * axis.x, angle * axis.y, angle * axis.z);
		body.setWorldTransform(tr);
	}

}

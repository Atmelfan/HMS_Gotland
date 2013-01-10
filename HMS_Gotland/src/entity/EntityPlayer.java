package entity;

import hms_gotland_core.HMS_Gotland;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

public class EntityPlayer extends Entity
{

	public EntityPlayer(HMS_Gotland gotland, Vector3f pos)
	{
		super(gotland, pos);
	}
	
	
	public void move(Vector3f pos, Vector3f angle)
	{
		body.applyCentralForce(pos);
		angle(angle.x, new Vector3f(1, 0, 0));
		
	}
	
	public void angle(float angle, Vector3f axis)
	{
		Quat4f quat = new Quat4f();
		
		quat.w = (float) Math.cos(Math.toRadians(angle));
	    quat.x = (float) (axis.x*Math.sin(Math.toRadians(angle)));
	    quat.y = (float) (axis.y*Math.sin(Math.toRadians(angle)));
	    quat.z = (float) (axis.z*Math.sin(Math.toRadians(angle)));
	    
	    Transform tr = getBodyTransform();
	    tr.setRotation(quat);
	    body.setWorldTransform(tr);
	}
	
	/* (non-Javadoc)
	 * @see entity.Entity#tick()
	 */
	@Override
	public void tick()
	{
		super.tick();
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

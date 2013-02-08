package hms_gotland_client;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import level.Level.BulletHole;
import model.Model;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;

import entity.Entity;
import entity.EntityPlayer;


public class ClientLevel
{
	public ArrayList<ClientEntity> entities = new ArrayList<>();
	public ArrayList<BulletHole> bulletHoles = new ArrayList<>();
	
	private DiscreteDynamicsWorld level;
	private Model model;
	private RigidBody levelbody;
	RenderEngine renderEngine;
	public ClientPlayer player;
	
	public ClientLevel(HMS_Gotland gotland)
	{
		renderEngine = gotland.renderEngine;
		player = new ClientPlayer(this, "gpa_robotics_war.md2", 0);
		setupWorld();
	}
	
	private void setupWorld()
	{
		DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
		Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
		Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
		AxisSweep3 overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax);
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		 
		level = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration);
		level.setGravity(new Vector3f(0F, -9.82F, 0F));
		level.getDispatchInfo().allowedCcdPenetration = 0.1f;
	}
	
	public void addEntity(ClientEntity entity)
	{
		entities.add(entity);
		level.addRigidBody(entity.body);
	}
	
	public void removeEntity(ClientEntity entity)
	{
		entities.remove(entity);
		level.removeRigidBody(entity.body);
	}
	
	public void draw()
	{
		if(model != null)
		{
			//Draw level data
			float[] temp = new float[16];
			levelbody.getWorldTransform(new Transform()).getOpenGLMatrix(temp);
			model.draw(0, renderEngine.getViewProjectionMatrix(), temp);
		}
		for (int i = 0; i < entities.size(); i++)
		{
			entities.get(i).draw(renderEngine);
		}
	}

	public void setLevel(String name)
	{
		model = renderEngine.getModel(name);
	}
	
	public void tick()
	{
		// TODO Auto-generated method stub
		
	}

	public void destroy()
	{
		// TODO Auto-generated method stub
		
	}

}

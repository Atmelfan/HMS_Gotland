package level;

import hms_gotland_client.HMS_Gotland;
import hms_gotland_client.RenderEngine;

import java.io.File;
import java.util.ArrayList;

import javax.vecmath.Vector3f;

import org.lwjgl.Sys;

import level.Entity;
import level.EntityPlayer;
import level.Level.BulletHole;
import model.Model;
import model.ModelObj;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;



public class ClientLevel
{
	public ArrayList<ClientEntity> entities = new ArrayList<>();
	public ArrayList<BulletHole> bulletHoles = new ArrayList<>();
	
	private DiscreteDynamicsWorld level;
	private Model model;
	private RigidBody levelbody;
	public RenderEngine renderEngine;
	public ClientPlayer player;
	private long lastTick;
	private String name;
	private int playerID;
	private Vector3f playerOriginPos;
	private String mesh;
	
	public ClientLevel(HMS_Gotland gotland)
	{
		renderEngine = gotland.renderEngine;
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
			model.draw(0, renderEngine.getViewProjectionMatrix(), temp, renderEngine);
		}
		for (int i = 0; i < entities.size(); i++)
		{
			entities.get(i).draw(renderEngine);
		}
	}

	public void setLevel(String name)
	{
		this.name = name;
	}
	
	public void tick()
	{	
		if(mesh != null && levelbody == null)
		{
			model = new ModelObj(new File(mesh), true);
			levelbody = new LevelCollisionShape(new File(mesh), true).body();
			level.addRigidBody(levelbody);
			createPlayer(playerID, playerOriginPos);
			System.out.println("Created level at " + levelbody.getWorldTransform(new Transform()).origin + 
					", player at " + playerOriginPos);
		}
		level.stepSimulation(1/60F);
	}

	public void destroy()
	{
		level.destroy();
	}

	public void createPlayer(int playerID, Vector3f playerPos)
	{
		player = new ClientPlayer(this, "lara/Lara_Croft.obj", playerID);
		player.setPos(playerPos);
		renderEngine.camera.setOwner(player);
		addEntity(player);
	}

	public void setPlayerAndLevel(int playerid, Vector3f playerPos, String levelName)
	{
		playerID = playerid;
		playerOriginPos = playerPos;
		mesh = levelName;
	}

}

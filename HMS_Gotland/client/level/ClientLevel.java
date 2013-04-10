package level;

import hms_gotland_client.HMS_Gotland;
import hms_gotland_client.RenderEngine;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import model.Model;
import model.ModelObj;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;



public class ClientLevel
{
	public ArrayList<ClientEntity> entities = new ArrayList<>();
	
	private DiscreteDynamicsWorld world;
	private Model model;
	private RigidBody levelbody;
	public RenderEngine renderEngine;
	public ClientPlayer player;
	private long lastTick;
	private String name;
	private int playerID;
	private Vector3f playerOriginPos;
	private String mesh;
	private HMS_Gotland game;
	
	public ClientLevel(HMS_Gotland gotland)
	{
		game = gotland;
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
		 
		setWorld(new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration));
		getWorld().setGravity(new Vector3f(0F, -9.82F, 0F));
		getWorld().getDispatchInfo().allowedCcdPenetration = 0.1f;
		
	}
	
	public void addEntity(ClientEntity entity)
	{
		entities.add(entity);
		getWorld().addRigidBody(entity.body);
	}
	
	public void removeEntity(ClientEntity entity)
	{
		entities.remove(entity);
		getWorld().removeRigidBody(entity.body);
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
			model = new ModelObj(renderEngine, renderEngine.resources.getResource(mesh));
			levelbody = new LevelCollisionShape(renderEngine.resources.getResource(mesh), true).body();
			getWorld().addRigidBody(levelbody);
			createPlayer(playerID, playerOriginPos);
			System.out.println("Created level at " + levelbody.getWorldTransform(new Transform()).origin + 
					", player at " + playerOriginPos);
		}
		for (int i = 0; i < entities.size(); i++)
		{
			ClientEntity entity = entities.get(i);
			entity.tick();
		}
		getWorld().stepSimulation(1/60F);
	}

	public void destroy()
	{
		getWorld().destroy();
	}

	public void createPlayer(int playerID, Vector3f playerPos)
	{
		player = new ClientPlayer(this, "generic://models/lara/Lara_Croft.obj", playerID);
		player.setPos(playerPos);
		renderEngine.camera.setOwner(player);
		entities.add(player);
		getWorld().addCollisionObject(player.getGhostObject(), CollisionFilterGroups.CHARACTER_FILTER, (short)(CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));

		getWorld().addAction(player.getCharacter());
	}

	public void setPlayerAndLevel(int playerid, Vector3f playerPos, String levelName)
	{
		playerID = playerid;
		playerOriginPos = playerPos;
		mesh = levelName;
		System.out.println(mesh);
	}

	public AxisSweep3 getAxisSweep()
	{
		// TODO Auto-generated method stub
		return (AxisSweep3)getWorld().getBroadphase();
	}

	/**
	 * @return the world
	 */
	public DiscreteDynamicsWorld getWorld()
	{
		return world;
	}

	/**
	 * @param world the world to set
	 */
	public void setWorld(DiscreteDynamicsWorld world)
	{
		this.world = world;
	}

}

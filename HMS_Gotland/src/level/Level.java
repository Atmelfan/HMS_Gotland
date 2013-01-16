package level;


import hms_gotland_core.HMS_Gotland;
import hms_gotland_core.RenderEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.StridingMeshInterface;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.collision.shapes.TriangleMeshShape;
import com.bulletphysics.collision.shapes.TriangleShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

import entity.Entity;
import entity.EntityList;
import entity.EntityPlayer;

import model.ModelObj;
import Util.VertexData;

public class Level
{
	private static String DEFAULT_LEVEL_PATH = "Resources/levels/";
	
	private File levelFile;
	
	public ArrayList<Entity> entities = new ArrayList<>();
	
	public EntityPlayer player;
	
	public String name;
	
	public ModelObj model;
	public RigidBody levelbody;
	
	public DynamicsWorld level;
	private HMS_Gotland game;

	public RenderEngine renderEngine = new RenderEngine();
	
	public Level(String name, HMS_Gotland gotland)
	{
		game = gotland;
		setupWorld();
		
		player = new EntityPlayer(this, new Vector3f());
		addEntity(player);
		
		levelFile = new File(DEFAULT_LEVEL_PATH + name);
		parseLevelFile(levelFile);
	}
	
	public void tick()
	{
		level.stepSimulation(1/60F);
		for(int i = 0; i < entities.size(); i++)
		{
			entities.get(i).tick();
		}
	}
	
	public void addEntity(Entity entity)
	{
		entities.add(entity);
		level.addRigidBody(entity.getBody());
	}
	
	public void removeEntity(Entity entity)
	{
		entities.remove(entity);
		level.removeRigidBody(entity.getBody());
	}
	
	public void draw()
	{
		if(model != null)
		{
			//Draw level data
			//Empty model matrix already exist
			model.draw();
		}
		
		for (int i = 0; i < entities.size(); i++)
		{
			entities.get(i).draw();
		}
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
		level.getDispatchInfo().allowedCcdPenetration = 0f;
	}
	
	public void explosion(Vector3f pos, float power)
	{
		for (int i = 0; i < entities.size(); i++)
		{
			Entity entity = entities.get(i);
			
			Vector3f v = new Vector3f(entity.getPos());
			v.sub(pos);
			
			Vector3f v1 = new Vector3f(entity.getPos());
			v1.sub(pos);
			v1.normalize();
			//Field force lowers by distance squared
			v1.scale(power - v1.lengthSquared());
			
			entity.getBody().applyForce(v1, v);
		}
	}
	
	private void parseLevelFile(File file)
	{
		System.out.println("Loading level...");
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(new File(file, "level.lvl")));
			
			String line = "";
			int lineCount = 0;
			
			while((line = reader.readLine()) != null)
			{
				lineCount++;
				line = line.toLowerCase().trim();
				
				//&model command points to models used in this level
				if(line.startsWith("&models"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 1)
					{
						renderEngine.modelpool.loadFolder(new File(file, lines[1]));
					}
				}
				if(line.startsWith("&entity"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 4)
					{
						
						Entity e = EntityList.getEntity(lines[1], this, new Vector3f(Float.valueOf(lines[2]), Float.valueOf(lines[3]), Float.valueOf(lines[4])));
						if(e != null)
						{
							addEntity(e);
						}else
						{
							throw new InvalidLevelException(file.getName(), "Invalid entity in level file", lineCount);
						}
					}else
					{
						throw new InvalidLevelException(file.getName(), "Invalid &entity command", lineCount);
					}
				}
				if(line.startsWith("&player"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 1)
					{
						try
						{
							if("pos".equals(lines[1]) && lines.length > 4)
							{
								player.setPos(new Vector3f(Float.valueOf(lines[2]), Float.valueOf(lines[3]), Float.valueOf(lines[4])));
							}
							
							if("angle".equals(lines[1]) && lines.length > 4)
							{
								Matrix3f t = player.getWorldTransform().basis;
								t.rotX(Float.valueOf(lines[2]));
								t.rotY(Float.valueOf(lines[3]));
								t.rotZ(Float.valueOf(lines[4]));
								player.getWorldTransform().set(t);
							}
							
							if("damage".equals(lines[1]) && lines.length > 2)
							{
								
							}
						} catch (NumberFormatException e)
						{
							e.printStackTrace();
							throw new InvalidLevelException(file.getName(), "Malformed &player command", lineCount);
						}
					}else
					{
						throw new InvalidLevelException(file.getName(), "Invalid &player command", lineCount);
					}
				}
				if(line.startsWith("&name"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 1)
					{
						name = lines[1];
					}else
					{
						throw new InvalidLevelException(file.getName(), "Invalid &name command", lineCount);
					}
				}
				if(line.startsWith("&obj"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 1)
					{
						model = new ModelObj(new File(file, lines[1]), false);
						
						//Get vertex data
						VertexData[] data = model.data.toArray(new VertexData[0]);
						
						//Assemble vertex data into hull vectors
						ObjectArrayList<Vector3f> vertes = new ObjectArrayList<Vector3f>();
						for (int i = 0; i < data.length; i++)
						{
							vertes.add(new Vector3f(data[i].getXYZ()));
						}
						//Create body
						CollisionShape groundShape = new ConvexHullShape(vertes);
						Transform groundTransform = new Transform();
						groundTransform.setIdentity();
						groundTransform.origin.set(new Vector3f(0F, 0F, 0F));
						float mass = 0F;
						Vector3f localInertia = new Vector3f(0F, 0F, 0F);
					    DefaultMotionState myMotionState = new DefaultMotionState(groundTransform);
						RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, groundShape, localInertia);
						levelbody = new RigidBody(rbInfo);
						levelbody.setRestitution(0.1f);
						levelbody.setFriction(0.50f);
						levelbody.setDamping(0f, 0f);
						//Add level body to level
						
						
						
						level.addRigidBody(levelbody);
						model.data.clear();
					}else
					{
						throw new InvalidLevelException(file.getName(), "Invalid &obj command", lineCount);
					}
				}
			}
			reader.close();
			
		}catch (FileNotFoundException e)
		{
			System.out.println("Could not find level file!");
			System.out.println(file.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e)
		{
			System.out.println("Could not read level file!");
			e.printStackTrace();
		} catch (InvalidLevelException e)
		{
			System.out.println("Invalid level file!");
			e.printStackTrace();
		}
	}

	public void destroy()
	{
		model.destroy();
		level.destroy();
		
	}

	public void reloadLevel()
	{
		parseLevelFile(levelFile);
	}
}

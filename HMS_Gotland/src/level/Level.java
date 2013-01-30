package level;


import hms_gotland_client.HMS_Gotland;
import hms_gotland_client.RenderEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.RayResultCallback;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.NearCallback;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StridingMeshInterface;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.InternalTickCallback;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
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
	
	public RenderEngine renderEngine;
	private HMS_Gotland game;

	
	public Level(String name, HMS_Gotland gotland)
	{
		game = gotland;
		renderEngine = gotland.renderEngine;
		//Setup bullet world
		setupWorld();
		//Create player
		player = new EntityPlayer(this, new Vector3f(0, 0, 0));
		addEntity(player);
		//Read level save file
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
			float[] temp = new float[16];
			levelbody.getWorldTransform(new Transform()).getOpenGLMatrix(temp);
			model.draw(0, renderEngine.getViewProjectionMatrix(), temp);
		}
		for (int i = 0; i < entities.size(); i++)
		{
			entities.get(i).draw(renderEngine);
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
						Entity e = EntityList.getEntity(lines[1], this, new Vector3f());
						if(e != null)
						{
							if(line.endsWith("{"))
							{
								
								String tag;
								while((tag = reader.readLine()) != null)
								{
									tag = tag.trim();
									
									if(tag.startsWith("}")) break;
									e.processTag(tag);
								}
							}
							e.setPos(new Vector3f(Float.valueOf(lines[2]), Float.valueOf(lines[3]), Float.valueOf(lines[4])));
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
						//TriangleIndexVertexArray array = new TriangleIndexVertexArray(model.numpolygons(), );
						
						CollisionShape groundShape = new BoxShape(new Vector3f(model.getXWidth(), model.getYHeight(), model.getZDepth()));
						Transform groundTransform = new Transform();
						groundTransform.setIdentity();
						groundTransform.origin.set(new Vector3f(0F, 0F, 0F));
						float mass = 0F;
						Vector3f localInertia = new Vector3f(0F, 0F, 0F);
					    DefaultMotionState myMotionState = new DefaultMotionState(groundTransform);
						RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, groundShape, localInertia);
						levelbody = new RigidBody(rbInfo);
						levelbody.setRestitution(0.1f);
						levelbody.setFriction(0.10f);
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

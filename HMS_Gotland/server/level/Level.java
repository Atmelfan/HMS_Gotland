package level;


import hms_gotland_server.HMS_Gotland_Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.AxisSweep3_32;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;


import model.ModelObj;
import model.ModelPool;
import Util.VertexData;

public class Level
{
	private static String DEFAULT_LEVEL_PATH = "Resources/levels/";
	
	private File levelFile;
	
	public ArrayList<Entity> entities = new ArrayList<>();
	
	public String name;
	
	//Physics
	public LevelCollisionShape model;
	public RigidBody levelbody;
	public DynamicsWorld level;
	public ModelPool modelpool;
	
	private HMS_Gotland_Server game;
	
	public Level(String name, HMS_Gotland_Server hms_Gotland_Server)
	{
		game = hms_Gotland_Server;
		modelpool = new ModelPool();
		//Setup bullet world
		setupWorld();
		//Read level save file
		levelFile = new File(DEFAULT_LEVEL_PATH + name);
		parseLevelFile(levelFile);
	}
	
	public void saveToDisk()
	{
		
	}
	
	public void tick()
	{
		for(int i = 0; i < entities.size(); i++)
		{
			entities.get(i).tick();
		}
		level.stepSimulation(1/60F);
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
	
	private void setupWorld()
	{
		DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
		Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
		Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
		AxisSweep3_32 overlappingPairCache = new AxisSweep3_32(worldAabbMin, worldAabbMax);
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		
		level = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration);
		level.setGravity(new Vector3f(0F, -9.82F, 0F));
		level.getDispatchInfo().allowedCcdPenetration = 0.1f;
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
				
				if(line.startsWith("&entity"))
				{
					String[] lines = line.split(" ");
					if(lines.length >= 3)
					{
						Entity e = EntityList.getEntity(lines[1], this);
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
							if(e.getBody() != null)
							{
								addEntity(e);
							}else
							{
								System.err.println("No collision size in &entity command @line " + lineCount);
							}
							
						}else
						{
							System.err.println("Invalid entity in &entity command @line " + lineCount);
						}
					}else
					{
						System.err.println("Invalid &entity command @line " + lineCount);
					}
				}
				if(line.startsWith("&player"))
				{
					//TODO
				}
				if(line.startsWith("&name"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 1)
					{
						name = lines[1];
					}else
					{
						throw new LevelException(file.getName(), "Invalid &name command", lineCount);
					}
				}
				if(line.startsWith("&obj"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 1)
					{
						model = new LevelCollisionShape(new File(file, lines[1]), false);
						
						//Create body
						ArrayList<Vector3f> mesh = model.mesh;
						ByteBuffer index = BufferUtils.createByteBuffer(mesh.size() * 3 * 4);
						for (int i = 0; i < mesh.size() * 3; i++)
						{
							index.putInt(i);
						}
						
						ByteBuffer geom = BufferUtils.createByteBuffer(mesh.size() * 3 * 4);
						for (int i = 0; i < mesh.size(); i++)
						{
							geom.putFloat(mesh.get(i).x);
							geom.putFloat(mesh.get(i).y);
							geom.putFloat(mesh.get(i).z);
						}
						index.rewind();
						geom.rewind();
						TriangleIndexVertexArray trimesh = new TriangleIndexVertexArray(model.numpolygons(), index, 3 * 4, model.numpolygons(), geom, 3 * 4);
						BvhTriangleMeshShape trimeshshape = new BvhTriangleMeshShape(trimesh,true);
						
						Transform groundTransform = new Transform();
						groundTransform.setIdentity();
						groundTransform.origin.set(new Vector3f(0F, 0F, 0F));
						float mass = 0F;
						Vector3f localInertia = new Vector3f(0F, 0F, 0F);
					    DefaultMotionState myMotionState = new DefaultMotionState(groundTransform);
						RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, trimeshshape, localInertia);
						levelbody = new RigidBody(rbInfo);
						levelbody.setRestitution(0.1f);
						levelbody.setFriction(0.50f);
						levelbody.setDamping(0f, 0f);
						//Add level body to level
						level.addRigidBody(levelbody);
						model.mesh.clear();
					}else
					{
						throw new LevelException(file.getName(), "Invalid &obj command", lineCount);
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
		} catch (LevelException e)
		{
			System.out.println("Invalid level file!");
			e.printStackTrace();
		}
	}

	public void destroy()
	{
		level.destroy();
	}

	public void reloadLevel()
	{
		parseLevelFile(levelFile);
	}
	
	//BulletHole struct
	public class BulletHole
	{
		Vector3f position;
		Vector3f normal;
	}
}

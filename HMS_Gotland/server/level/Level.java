package level;


import hms_gotland_server.HMS_Gotland_Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

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

import model.ModelPool;

public class Level
{
	
	private File levelFile;
	public ArrayList<Entity> entities = new ArrayList<>();
	public HashMap<String, String> dependencies = new HashMap<>();
	public String name;
	
	//Physics
	public LevelCollisionShape model;
	public RigidBody levelbody;
	public DynamicsWorld level;
	public ModelPool modelpool;
	
	private HMS_Gotland_Server game;

	private Vector3f playerPos = new Vector3f();

	public String modelName;
	
	public Level(HMS_Gotland_Server hms_Gotland_Server, File level)
	{
		game = hms_Gotland_Server;
		//Setup bullet world
		setupWorld();
		//Read level save file
		levelFile = level;
		try
		{
			parseLevelFile(levelFile);
		} catch (IOException e)
		{
			System.err.println("Error loading level:  - " + e.getMessage());
		}
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
			
			Vector3f v1 = new Vector3f(v);
			v1.normalize();
			//Field force lowers by distance squared
			v1.scale(power - v1.lengthSquared());
			
			entity.getBody().applyForce(v1, v);
		}
	}
	
	private void parseLevelFile(File file) throws IOException
	{
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(new File(file, "level.lvl")));
		try
		{
			String line = "";
			int lineCount = 0;
			boolean comment = false;
			while((line = reader.readLine()) != null)
			{
				lineCount++;
				line = line.toLowerCase().trim();
				if(line.startsWith("*/"))
				{
					comment = false;
				}
				if(comment) continue;
				
				if(line.startsWith("/*"))
				{
					comment = true;
				}
				else if(line.startsWith("&entity"))
				{
					String[] lines = line.split(" ");
					//TODO		
				}
				else if(line.startsWith("&dependencies") && line.endsWith("{"))
				{
					line = reader.readLine().toLowerCase().trim();
					while(line != null && !line.equals("}"))
					{
						String[] lines = line.split("\\s+");
						if(lines.length > 1)
						{
							dependencies.put(lines[0], lines[1]);
						}
					}
				}
				else if(line.startsWith("&player"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 4 && lines[1].equalsIgnoreCase("pos"))
					{
						playerPos = new Vector3f(Float.parseFloat(lines[2]), Float.parseFloat(lines[3]), Float.parseFloat(lines[4]));
					}else
					{
						throw new LevelException(file.getName(), "Invalid &player command", lineCount);
					}
				}
				else if(line.startsWith("&name"))
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
				else if(line.startsWith("&obj"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 1)
					{
						modelName = lines[1];
						String n = lines[1].replace("server://", "Level/").replace("generic://", "Resources/assets/");
						model = new LevelCollisionShape(new File(n), false);
						levelbody = model.body();
					}else
					{
						throw new LevelException(file.getName(), "Invalid &obj command", lineCount);
					}
				}
			}
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
		}finally
		{
			reader.close();
		}
	}

	public void destroy()
	{
		level.destroy();
	}

	public Vector3f getPlayerPos()
	{
		return playerPos;
	}
}

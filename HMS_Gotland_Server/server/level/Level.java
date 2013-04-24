package level;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;
import com.bulletphysics.collision.broadphase.AxisSweep3_32;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.InternalTickCallback;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.util.ObjectArrayList;

public class Level{

	private File levelFile;
	public List<Entity> entities = new ArrayList<Entity>();
	public ArrayList<String[]> dependencies = new ArrayList<String[]>();
	public String name;

	// Physics
	public ObjCollisionShape model;
	public RigidBody levelbody;
	public DynamicsWorld level;


	private Vector3f playerPos = new Vector3f();

	public String modelName;
	private boolean hasDependencies;
	private Object lock = new Object();

	public Level(File level) {
		// Setup bullet world
			setupWorld();
			// Read level save file
			levelFile = level;
			try {
				parseLevelFile(levelFile);
			} catch (IOException e) {
				System.err.println("Error loading level:  - " + e.getMessage());
			}
	}

	public void tick() {
		synchronized (lock) {
			level.stepSimulation(1 / 60F);
		}
	}

	public void addEntity(Entity entity) {
		synchronized (lock) {
			entities.add(entity);
			level.addCollisionObject(entity.getBody());
		}
	}

	public void removeEntity(Entity entity) {
		synchronized (lock) {
			entities.remove(entity);
			level.removeCollisionObject(entity.getBody());
		}
	}

	private void setupWorld() {
		DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(
				collisionConfiguration);
		Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
		Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
		AxisSweep3_32 overlappingPairCache = new AxisSweep3_32(worldAabbMin, worldAabbMax);
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

		level = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache,
				solver, collisionConfiguration);
		level.setGravity(new Vector3f(0F, -9.82F, 0F));
		level.getDispatchInfo().allowedCcdPenetration = 0.1f;
		level.setInternalTickCallback(new InternalTickCallback() {

			@Override
			public void internalTick(DynamicsWorld arg0, float dt) {
				ObjectArrayList<CollisionObject> entities = arg0
						.getCollisionObjectArray();
				for (int i = 0; i < entities.size(); i++) {
					CollisionObject entity = entities.get(i);
					Object obj = entity.getUserPointer();
					if (obj instanceof Entity) {
						((Entity) obj).tick(dt);
					}

				}

			}
		}, (Object) this);
	}

	private void parseLevelFile(File file) throws IOException {
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(new File(file, "level.lvl")));
		try {
			String line = "";
			int lineCount = 0;
			boolean comment = false;
			while ((line = reader.readLine()) != null) {
				lineCount++;
				line = line.toLowerCase().trim();
				if (line.startsWith("*/")) {
					comment = false;
				}
				if (comment)
					continue;

				if (line.startsWith("/*")) {
					comment = true;
				} else if (line.startsWith("&entity")) {
					String[] lines = line.split(" ");
					// TODO
				} else if (line.startsWith("&dependencies")
						&& line.endsWith("{")) {
					line = reader.readLine().toLowerCase().trim();
					while (line != null && !line.equals("}")) {
						String[] lines = line.split("\\s+");
						if (lines.length == 2) {
							dependencies.add(lines);
						}
					}
					if (dependencies.size() > 0) {
						hasDependencies = true;
					}
				} else if (line.startsWith("&player")) {
					String[] lines = line.split(" ");
					if (lines.length > 4 && lines[1].equalsIgnoreCase("pos")) {
						playerPos = new Vector3f(Float.parseFloat(lines[2]),
								Float.parseFloat(lines[3]),
								Float.parseFloat(lines[4]));
					} else {
						System.err.println(file.getName() +
								" Invalid &player command " + lineCount);
					}
				} else if (line.startsWith("&name")) {
					String[] lines = line.split(" ");
					if (lines.length > 1) {
						name = lines[1];
					} else {
						System.err.println(file.getName() +
								" Invalid &name command " + lineCount);
					}
				} else if (line.startsWith("&obj")) {
					String[] lines = line.split(" ");
					if (lines.length > 1) {
						modelName = lines[1];
						String n = lines[1].replace("server://", file.getPath()).replace(
										"generic://", "Resources/assets/");
						System.out.println(n);
						model = new ObjCollisionShape(new File(n), false);
						levelbody = model.body();
						level.addRigidBody(levelbody);
					} else {
						System.err.println(file.getName() +
								"  Invalid &obj command " + lineCount);
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not find level file!");
			System.out.println(file.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not read level file!");
			e.printStackTrace();
		} finally {
			reader.close();
		}
	}

	public void destroy() {
		level.destroy();
	}

	public Vector3f getPlayerPos() {
		return playerPos;
	}

	public void addPlayer(EntityPlayer player) {
		entities.add(player);
		level.addCollisionObject(player.getBody());
	}
}

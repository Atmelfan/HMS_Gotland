package model;

import hms_gotland_client.RenderEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import Util.GLUtil;
import Util.ShaderUtils;

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.QuaternionUtil;

/* Name: ModelSMD.java
 * Description:
 * Apparently MD2 is useless for nice looking models(big surprise for an 16 year old format isn't it?),
 * so I made an MD3 reader, problem solved! Nope! MD3 is even more useless than MD2 because Maya can't
 * natively export it and no scripts on the Internet worked. Export to blender and then export to MD3 maybe? 
 * Maya: NO! FUCK YOU! I'M JUST GONNA GIVE YOU 500 ERRORS WITH RETARDED DESCRIPTIONS AND CRASH!
 * Sight... And it looks shit in game without bone animation anyways...
 * Note(s) for future me:
 * Stop being a retarded twelve year old and look up support for <insert stupid format here> before coding shit.
 * Drink more Coka Cola, it makes you a better programmer.
 * Stop spending ten minutes making a stupid description for future retard me. Edit *fifteen minutes
 * (c) GPA Robotics 2013
 */

/* I hate dealing with multiple model files and shit so
 * materials must be embedded before(!) triangles section.
 * -Materials are embedded in between header and nodes
 * -No reference models or seperate files everything is defined in one smd file
 * -Reference model is simply frame 0
 * -Physics isn't implemented with smd files and must be defined in level.lvl
 *  Entity, player and object constructs using primitives
 * 
 * GPA Robotics material file spec:
 * 	texture blablabla.png (default "null")
 *  ambient 0.123456789	(default 0.0f)
 *  tbc
 *  
 * SMD structure
 * 
 * version <v>
 * name <name>
 * materials
 *  material <name(t.ex blablabla, no whitespace)>
 *   <property> <value>
 *   [...] Every property
 *   <property> <value>
 *  [...] Every material
 *  material <name>
 *   <property> <value>
 *   [...] Every property
 *   <property> <value>
 * end
 * nodes
 *  <id> <name> <parent>
 *   [...] Every node
 *  <id> <name> <parent>
 * end
 * skeleton
 * 	time <t>
 *   <id> <x><y><z> <yaw><pitch><roll>
 *    [...] Every bone
 *   <id> <x><y><z> <yaw><pitch><roll>
 *  [...] Every frame
 *  time <t>
 *   <id> <x><y><z> <yaw><pitch><roll>
 *    [...] Every bone
 *   <id> <x><y><z> <yaw><pitch><roll>
 * end
 * triangles
 *  <material> Every triangle
 *   <parentBone> <x><y><z> <nx><ny><nz> <s><t> <links> <bone id> <weight>
 *   <parentBone> <x><y><z> <nx><ny><nz> <s><t> <links> <bone id> <weight>
 *   <parentBone> <x><y><z> <nx><ny><nz> <s><t> <links> <bone id> <weight>
 *  [...]
 *  <material>
 *   [...]
 *   <parentBone> <x><y><z> <nx><ny><nz> <s><t> <links> <bone id> <weight>
 * end
 * vertexanimation
 *  time <t>
 *   <vertex pos> <x><y><z> <nx><ny><nz>
 *   [...] Every vertex
 *   <vertex pos> <x><y><z> <nx><ny><nz>
 *  [...] Every frame
 *  time <t>
 *   <vertex pos> <x><y><z> <nx><ny><nz>
 *   [...] Every vertex
 *   <vertex pos> <x><y><z> <nx><ny><nz>
 * end
 * inversekinematic
 *  TODO
 * end
 */

public class ModelSMD extends Model
{
	public int numPolygons = 0;
	public int version;
	private HashMap<String, SMDMaterial> materials = new HashMap<>();
	private ArrayList<SMDNode> nodes = new ArrayList<>();
	private ArrayList<ArrayList<SMDBone>> skeleton = new ArrayList<>();
	private HashMap<String, ArrayList<SMDVertex>> trias = new HashMap<>();
	private int num_skeleton_frames;
	
	private HashMap<Integer, Integer> nodeParents;
	private ArrayList<HashMap<Integer, Integer>> boneindices = new ArrayList<>();
	private ArrayList<SMDBone> bones = new ArrayList<>();
	
	private static int vsId;
	private static int fsId;
	private static int shader_id;
	

	public ModelSMD(RenderEngine rend, File resource)
	{
		super(rend, resource);
		read(resource);
	}
	
	private void read(File resource)
	{
		String line = "";
		BufferedReader input = null;
		
		boolean isMaterials = false;
		boolean isNodes = false;
		boolean isSkeleton = false;
		boolean isTriangles = false;
		int lastTime = 0;
		String lastMaterial = "";
		SMDVertex currentTriangle = null;
		int indices = 0;
		
		try
		{
			input = new BufferedReader(new FileReader(resource));
			
			while((line = input.readLine().trim()) != null)
			{
				if(line.isEmpty()) continue;
				String[] args = line.split("\\s+");
				int length = args.length;
				
				//Header
				if(args[0].equals("version") && length > 1)
				{
					version = Integer.parseInt(args[1]);
				}
				//Commands
				else if(args[0].equals("materials"))
				{
					isMaterials = true;
				}
				else if(args[0].equals("nodes"))
				{
					isNodes = true;
				}
				else if(args[0].equals("skeleton"))
				{
					isSkeleton = true;
				}
				else if(args[0].equals("triangles"))
				{
					isTriangles = true;
				}
				else if(args[0].equals("end"))
				{
					isMaterials = false;
					isNodes = false;
					isSkeleton = false;
					isTriangles = false;
				}
				//Data
				else if(isMaterials)
				{
					if(args[0].equals("material"))
					{
						lastMaterial = args[1];
						materials.put(args[1], new SMDMaterial());
					}
					
					if(args[0].equals("texture"))
					{
						materials.get(lastMaterial).texture = renderer.getTexture(args[1], GL13.GL_TEXTURE0);
					}
					//TODO
				}
				else if(isNodes)
				{
					SMDNode node = new SMDNode(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
					nodes.add(node);
				}
				else if(isSkeleton)
				{
					if(args[0].equals("time"))
					{
						Map<Integer, Integer> map = boneindices.get(lastTime);
						lastTime = Integer.parseInt(args[1]);
						num_skeleton_frames = lastTime;
						if(map != null)
						{
							//Reuse indices from last frame(if any).
							boneindices.add(new HashMap<Integer, Integer>(map));
						}else
						{
							boneindices.add(new HashMap<Integer, Integer>());
						}
						
						
					}else
					{
						int id = Integer.parseInt(args[0]);
						float[] pos = new float[]{Float.parseFloat(args[1]), Float.parseFloat(args[2]), Float.parseFloat(args[3])};
						float[] ori = new float[]{Float.parseFloat(args[4]), Float.parseFloat(args[5]), Float.parseFloat(args[6])};
						SMDBone bone = new SMDBone();
						bone.id = id;
						bone.setPosition(pos[0], pos[1], pos[2]);
						bone.setEulerOrientation(ori[0], ori[1], ori[2]);
						int i = findBone(bone);
						//Check if
						if(i == -1)
						{
							bones.add(bone);
							i = indices++;
						}
						//Map the bone indice to it's id.
						boneindices.get(lastTime).put(id, i);
					}
				}
				else if(isTriangles)
				{
					if(args[0].equals("material"))
					{
						lastMaterial = args[1];
						trias.put(args[1], new ArrayList<ModelSMD.SMDVertex>());
					}else
					{
						float[] vertex = new float[]{Float.parseFloat(args[1]), Float.parseFloat(args[2]), Float.parseFloat(args[3])};
						float[] normal = new float[]{Float.parseFloat(args[4]), Float.parseFloat(args[5]), Float.parseFloat(args[6])};
						float[] st = new float[]{Float.parseFloat(args[7]), Float.parseFloat(args[8])};
						SMDVertex vert = new SMDVertex();
						vert.parent = Integer.parseInt(args[0]);
						vert.setVertex(vertex, normal, st);
						trias.get(lastMaterial).add(currentTriangle);
						numPolygons++;
					}
				}
			}
			
			input.close();
		} catch (FileNotFoundException e)
		{
			System.err.println("Error: ModelSMD.read() - " + e.getMessage());
		} catch (IOException e)
		{
			System.err.println("Error: ModelSMD.read() - " + e.getMessage());
		}
		numPolygons /= 3;
	}
	
	public int findBone(SMDBone bone)
	{
		for (int i = 0; i < bones.size(); i++)
		{
			if(bone.id == bones.get(i).id)
				return i;
		}
		return -1;
	}
	
	public void setupShader()
	{
		vsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/animation.vert"), GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		fsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/animation.frag"), GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		shader_id = ShaderUtils.makeProgram(vsId, fsId);
		
		GL20.glBindAttribLocation(shader_id, 0, "parent");
		GL20.glBindAttribLocation(shader_id, 1, "in_Position");
		GL20.glBindAttribLocation(shader_id, 2, "in_TextureCoord");
		GL20.glBindAttribLocation(shader_id, 3, "in_Normal");
		
		GL20.glValidateProgram(shader_id);
		GLUtil.cerror(getClass().getName() + " setupShader");
	}
	
	public void compileBoneVBO()
	{
		
		
		
	}
	
	public void compileMeshVBO()
	{
		
	}
	
	private class SMDNode
	{
		public int id;
		public String name;
		public int parent;
		
		public SMDNode(int id, String name, int parent)
		{
			super();
			this.id = id;
			this.name = name;
			this.parent = parent;
		}
	}
	
	private class SMDBone
	{
		public int id;
		public Quat4f orientation = new Quat4f();
		public Vector3f position = new Vector3f();
		
		public void setEulerOrientation(float yaw, float pitch, float roll)
		{
			QuaternionUtil.setEuler(orientation, yaw, pitch, roll);
		}
		
		public void setPosition(float x, float y, float z)
		{
			position.set(x, y, z);
		}

		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof SMDBone)
			{
				SMDBone bone = (SMDBone)obj;
				return position.equals(bone.position) && orientation.equals(bone.orientation) && id == bone.id;
			}
			return false;
		}
	}
	
	private class SMDVertex
	{
		public int parent;
		public float[] vertex;
		public float[] normal;
		public float[] st;
		
		public void setVertex(float[] vertex, float[] normal, float[] st)
		{
			this.vertex = vertex;
			this.normal = normal;
			this.st = st;
		}
		
	}
	
	private class SMDVertexAnimation
	{
		
	}
	
	private class SMDMaterial
	{
		public int texture;
	}
}

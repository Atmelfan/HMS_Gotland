package level;

import hms_gotland_client.RenderEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

/**
 * @author Jeremy Adams (elias4444)
 *
 * Use these lines if reading from a file
 * FileReader fr = new FileReader(ref);
 * BufferedReader br = new BufferedReader(fr);

 * Use these lines if reading from within a jar
 * InputStreamReader fr = new InputStreamReader(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ref)));
 * BufferedReader br = new BufferedReader(fr);
 */

public class LevelCollisionShape
{
	
	private ArrayList<float[]> vertexsets = new ArrayList<float[]>(); // Vertex Coordinates
	
	private ArrayList<int[]> faces = new ArrayList<int[]>(); // Array of Faces (vertex sets)
	
	private int numpolys = 0;
	
	//// Statisitcs for drawing ////
	public float toppoint = 0;		// y+
	public float bottompoint = 0;	// y-
	public float leftpoint = 0;		// x-
	public float rightpoint = 0;	// x+
	public float farpoint = 0;		// z-
	public float nearpoint = 0;		// z+
	
	public LevelCollisionShape(File file, boolean clearVertexData)
	{
		read(file);
		centerit();
		compileVBO();
		cleanup();
	}

	private void cleanup() 
	{
		vertexsets.clear();
		
		faces.clear();
	}
	
	private void read(File file) {
		int linecounter = 0;
		
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String newline;
			boolean firstpass = true;
			
			while (((newline = br.readLine()) != null)) 
			{
				linecounter++;
				newline = newline.trim();
				if (newline.length() > 0) 
				{
					if (newline.charAt(0) == 'v' && newline.charAt(1) == ' ') 
					{
						float[] coords = new float[4];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) 
						{
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
					    }
						//// check for farpoints ////
						if (firstpass) 
						{
							rightpoint = coords[0];
							leftpoint = coords[0];
							toppoint = coords[1];
							bottompoint = coords[1];
							nearpoint = coords[2];
							farpoint = coords[2];
							firstpass = false;
						}
						if (coords[0] > rightpoint) {
							rightpoint = coords[0];
						}
						if (coords[0] < leftpoint) {
							leftpoint = coords[0];
						}
						if (coords[1] > toppoint) {
							toppoint = coords[1];
						}
						if (coords[1] < bottompoint) {
							bottompoint = coords[1];
						}
						if (coords[2] > nearpoint) {
							nearpoint = coords[2];
						}
						if (coords[2] < farpoint) {
							farpoint = coords[2];
						}
						/////////////////////////////
						vertexsets.add(coords);
					}
					if (newline.charAt(0) == 'f' && newline.charAt(1) == ' ') 
					{
						String[] coordstext = newline.split("\\s+");
						int[] v = new int[coordstext.length - 1];
						
						for (int i = 1;i < coordstext.length;i++) 
						{
							String fixstring = coordstext[i].replaceAll("//","/0/");
							String[] tempstring = fixstring.split("/");
							v[i-1] = Integer.valueOf(tempstring[0]).intValue();
						}
						faces.add(v);
					}
				}
			}
			br.close();
			
		}catch(FileNotFoundException e) 
		{
			System.out.println("Failed to find file: " + file.getName());
			e.printStackTrace();
		}catch (IOException e) 
		{
			System.out.println("Failed to read file: " + file.getName());
			e.printStackTrace();	
		}catch (NumberFormatException e) 
		{
			System.out.println("Malformed OBJ (on line " + linecounter + "): " + file.getName() + "\r \r" + e.getMessage());
		}
	}
	
	private void centerit() 
	{
		float xshift = (rightpoint - leftpoint) /2f;
		float yshift = (toppoint - bottompoint) /2f;
		float zshift = (nearpoint - farpoint) /2f;
		
		for (int i=0; i < vertexsets.size(); i++) 
		{
			float[] coords = new float[4];
			
			coords[0] = (vertexsets.get(i))[0] - leftpoint - xshift;
			coords[1] = (vertexsets.get(i))[1] - bottompoint - yshift;
			coords[2] = (vertexsets.get(i))[2] - farpoint - zshift;
			
			vertexsets.set(i, coords); // = coords;
		}
		
	}
	
	public float getXWidth() {
		float returnval = 0;
		returnval = rightpoint - leftpoint;
		return returnval;
	}
	
	public float getYHeight() {
		float returnval = 0;
		returnval = toppoint - bottompoint;
		return returnval;
	}
	
	public float getZDepth() {
		float returnval = 0;
		returnval = nearpoint - farpoint;
		return returnval;
	}
	
	public int numpolygons() {
		return numpolys;
	}
	
	public ArrayList<Vector3f> mesh = new ArrayList<>();

	private RigidBody body;
	
	public void compileVBO()
	{
		//Assemble face indice
		for (int i = 0; i < faces.size(); i++)
		{
			int[] tempfaces = faces.get(i);
			
			for (int w = 0; w < 3; w++)
			{
				////////Vertex////////
				mesh.add(new Vector3f(vertexsets.get(tempfaces[w] - 1)[0], vertexsets.get(tempfaces[w] - 1)[1], vertexsets.get(tempfaces[w] - 1)[2]));

			}
			numpolys++;
		}
		
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
		TriangleIndexVertexArray trimesh = new TriangleIndexVertexArray(numpolygons(), index, 3 * 4, numpolygons(), geom, 3 * 4);
		BvhTriangleMeshShape trimeshshape = new BvhTriangleMeshShape(trimesh,true);
		
		Transform groundTransform = new Transform();
		groundTransform.setIdentity();
		groundTransform.origin.set(new Vector3f(0F, 0F, 0F));
		float mass = 0F;
		Vector3f localInertia = new Vector3f(0F, 0F, 0F);
	    DefaultMotionState myMotionState = new DefaultMotionState(groundTransform);
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, trimeshshape, localInertia);
		body = new RigidBody(rbInfo);
		body.setRestitution(0.1f);
		body.setFriction(0.50f);
		body.setDamping(0f, 0f);
		//Add level body to level
		mesh.clear();
	}
}
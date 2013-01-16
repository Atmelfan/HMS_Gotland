package level;

import java.util.ArrayList;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.VertexData;

public class LevelVertexData extends VertexData
{

	private ArrayList<Vector3f> vertices;
	
	@Override
	public int getVertexCount()
	{
		return vertices.size();
	}

	@Override
	public int getIndexCount()
	{
		return vertices.size();
	}

	@Override
	public <T extends Tuple3f> T getVertex(int idx, T out)
	{
		out.set(vertices.get(idx));
		return out;
	}

	@Override
	public void setVertex(int idx, float x, float y, float z)
	{
		
	}

	@Override
	public int getIndex(int idx)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}

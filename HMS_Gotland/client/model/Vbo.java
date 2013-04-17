package model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

public class Vbo
{
	private List<Triangle> triangles = new ArrayList<>();
	private int id;
	
	public void addTriangle(Triangle triangle)
	{
		triangles.add(triangle);
	}
	
	public void compile(int usage)
	{
		id = GL15.glGenBuffers();
		FloatBuffer buffer = BufferUtils.createFloatBuffer(triangles.size() * 8 * 3);
		for (int i = 0; i < triangles.size(); i++)
		{
			buffer.put(triangles.get(i).getElements());
		}
		buffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
		{
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, usage);
		}
	}
	
	public void bind()
	{
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
	}
	
	public void clear()
	{
		triangles.clear();
	}

}

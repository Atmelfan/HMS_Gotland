package model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class Vbo
{	
	private List<float[]> elements = new ArrayList<>();
	private int numElements;
	private int id;
	private int target;
	
	public Vbo(int t)
	{
		target = t;
		id = GL15.glGenBuffers();
	}
	
	public void addElements(float... values)
	{
		numElements += values.length;
		elements.add(values);
	}
	
	public void compile(int usage)
	{
		FloatBuffer buffer = BufferUtils.createFloatBuffer(numElements);
		for (int i = 0; i < elements.size(); i++)
		{
			buffer.put(elements.get(i));
		}
		buffer.flip();
		GL15.glBindBuffer(target, id);
		{
			GL15.glBufferData(target, buffer, usage);
		}
	}
	
	public void bind()
	{
		GL15.glBindBuffer(target, id);
	}
	
	public void unbind()
	{
		GL15.glBindBuffer(target, 0);
	}
	
	public void clear()
	{
		elements.clear();
		numElements = 0;
	}
	
	public void destroy()
	{
		clear();
		unbind();
		GL15.glDeleteBuffers(id);
	}

	public int size() {
		return numElements;
	}
}

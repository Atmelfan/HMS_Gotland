package model;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class Vbo
{	
	protected ByteBuffer elements;
	private int id;
	private int target;
	
	public Vbo(int t, int size)
	{
		elements = BufferUtils.createByteBuffer(size);
		target = t;
		id = GL15.glGenBuffers();
	}
	
	public void addElements(float... values)
	{
		for (int i = 0; i < values.length; i++)
		{
			elements.putFloat(values[i]);
		}
	}
	
	public void addElements(int... values)
	{
		for (int i = 0; i < values.length; i++)
		{
			elements.putInt(values[i]);
		}
	}
	
	public void addElements(short... values)
	{
		for (int i = 0; i < values.length; i++)
		{
			elements.putShort(values[i]);
		}
	}
	
	public void addElements(byte... values)
	{
		for (int i = 0; i < values.length; i++)
		{
			elements.put(values[i]);
		}
	}
	
	public void compile(int usage)
	{
		elements.flip();
		GL15.glBindBuffer(target, id);
		{
			GL15.glBufferData(target, elements, usage);
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
	}
	
	public void destroy()
	{
		clear();
		unbind();
		GL15.glDeleteBuffers(id);
	}

	public int size() {
		return elements.capacity();
	}
}

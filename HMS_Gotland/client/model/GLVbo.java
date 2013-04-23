package model;

import hms_gotland_client.RenderEngine;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL10GL;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opengl.GL15;

public class GLVbo
{	
	private List<float[]> elements = new ArrayList<float[]>();
	private int numElements;
	private int id;
	private int target;
	
	public GLVbo(int target)
	{
		this.target = target;
		id = GL15.glGenBuffers();
	}
	
	public void addElements(float... values)
	{
		numElements += values.length;
		elements.add(values);
	}
	
	public void update(int index, float... value)
	{
		elements.set(index, value);
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

	public int getId() {
		return id;
	}
	
	public CLMem getCLmemory(int flags) {
		return CL10GL.clCreateFromGLBuffer(RenderEngine.clcontext, flags, id, null);
	}
}

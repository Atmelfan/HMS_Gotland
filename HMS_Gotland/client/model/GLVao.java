package model;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class GLVao
{
	int id;
	public GLVao()
	{
		id = GL30.glGenVertexArrays();
	}
	
	
	public void enableVertexAttrib(int i)
	{
		bind();
		GL20.glEnableVertexAttribArray(i);
	}
	
	public void addBuffer(int index, int size, int unit, int stride, int offset, GLVbo vbo)
	{
		bind();
		vbo.bind();
		GL20.glVertexAttribPointer(index, size, unit, false, stride, offset);
		GL20.glEnableVertexAttribArray(index);
	}
	
	public void drawArrays(int primitive, int first, int count)
	{
		bind();
		GL11.glDrawArrays(primitive, first, count);
	}
	
	public void bind()
	{
		GL30.glBindVertexArray(id);
	}
	
}

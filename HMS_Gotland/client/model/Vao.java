package model;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Util.VertexData;

public class Vao
{
	int id;
	public Vao()
	{
		id = GL30.glGenVertexArrays();
	}
	
	
	public void addBuffer(int index, Vbo vbo)
	{
		vbo.bind();
		{
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 32, 0);
			GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 32, 12);
			GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 32, 20);
		}
	}
	
}

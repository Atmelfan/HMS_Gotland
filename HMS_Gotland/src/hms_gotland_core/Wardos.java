package hms_gotland_core;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class Wardos
{
	private static final int FBO_HEIGHT = 512;
	private static final int FBO_WIDTH = 1024;
	
	public int textureID = 0;
	private int fboID = 0;
	private int rboID = 0;
	
	public Wardos()
	{
		textureID = GL11.glGenTextures();
		fboID = GL30.glGenFramebuffers();
		rboID  = GL30.glGenRenderbuffers();
	}
	
	public void renderToTexture()
	{
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
		{
			//Bind texture to fbo, write color to texture
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureID, 0);
			//Bind renderbuffer to fbo
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rboID);
			{
				GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, FBO_WIDTH, FBO_HEIGHT);
				GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, rboID);
				//Render Wardos screen
				_render();
			}
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		}
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
	private void _render()
	{
		// TODO Auto-generated method stub
		
	}

	public void tick()
	{
		
	}
	
	public void command(String s)
	{
		
	}
}

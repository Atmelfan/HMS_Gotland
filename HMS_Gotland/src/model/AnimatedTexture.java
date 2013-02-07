package model;

import java.io.File;
import java.io.FilenameFilter;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import Util.GLUtil;

public class AnimatedTexture
{
	private int[] textures;
	public AnimatedTexture(String path, final String name)
	{
		File f = new File(path);
		//List all images starting with the animations name
		String[] images = f.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File arg0, String arg1)
			{
				return arg1.startsWith(name) && arg1.endsWith(".png");
			}
		});
		textures = new int[images.length];
		for (int i = 0; i < images.length; i++)
		{
			textures[i] = GLUtil.loadPNGTexture(images[i], GL13.GL_TEXTURE0);
		}
	}
	
	public int numFrames()
	{
		return textures.length;
	}
	
	public int getTextureID(int time)
	{
		return textures[time];
	}
	
	public void destroy()
	{
		GL11.glBindTexture(0, GL13.GL_TEXTURE0);
		for (int i = 0; i < textures.length; i++)
		{
			GL11.glDeleteTextures(textures[i]);
		}
	}
}

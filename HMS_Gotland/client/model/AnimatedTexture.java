package model;


import hms_gotland_client.RenderEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import Util.GLUtil;


public class AnimatedTexture
{
	private int[] textures;
	public AnimatedTexture(RenderEngine renderer, String path, final String name)
	{
		File f = renderer.resources.getResource(path);
		//List all images starting with the animations name
		String[] images = f.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File arg0, String arg1)
			{
				return arg1.startsWith(name) && arg1.endsWith(".png");
			}
		});
		ByteBuffer buf = null;
		int tWidth = 0;
		int tHeight = 0;
		
		int texId;
		
		for (int i = 0; i < images.length; i++)
		{
			try
			{
				File file = new File(f, images[i]);
				// Open the PNG file as an InputStream
				InputStream in = new FileInputStream(file);
				// Link the PNG decoder to this stream
				PNGDecoder decoder = new PNGDecoder(in);

				// Get the width and height of the texture
				tWidth = decoder.getWidth();
				tHeight = decoder.getHeight();

				// Decode the PNG file in a ByteBuffer
				if(buf == null)
				{
					buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight() * images.length);
				}
				decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
				in.close();
				
			} catch (IOException e)
			{
				System.err.println("Failed to load texture: " + images[i]);
			}
		}
		buf.flip();
		// Create a new texture object in memory and bind it
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		texId = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

		// All RGB bytes are aligned to each other and each component is 1 byte
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		// Upload the texture data and generate mip maps (for scaling)
		GL12.glTexImage3D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, tWidth, tHeight, images.length, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		// Setup the ST coordinate system
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		// Setup what to do when the texture has to be scaled
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		GLUtil.cerror("RenderEngine.getTexture");
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

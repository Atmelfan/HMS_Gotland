package hms_gotland_client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import model.Model;
import model.ModelPool;

import Util.GLUtil;
import Util.ShaderUtils;

public class RenderEngine
{	
	private static final int STAR_COUNT = 10000;

	public Camera camera;
	
	public ModelPool modelpool = new ModelPool(this);

	private int starVao_id;
	private int vbovid;
	private int vbocid;
	private int starfield_vsId;
	private int starfield_fsId;
	private int starfield_shader_id;

	private HMS_Gotland game;
	public ResourceManager resources;

	private int vpMatrixPos;
	
	
	public RenderEngine(HMS_Gotland game, int width, int height)
	{
		this.game = game;
		resources = game.getResourceManager();
		try 
		{
			PixelFormat pixelFormat = new PixelFormat(24, 8, 8, 0, GLUtil.getMaxSamplings());
			ContextAttribs contextAtrributes = new ContextAttribs(3, 2);
			contextAtrributes.withForwardCompatible(true);
			contextAtrributes.withProfileCore(true);
			
			Display.setDisplayMode(new DisplayMode(width, height));
			//Display.setTitle(WINDOW_TITLE);
			Display.create(pixelFormat, contextAtrributes);
			
			GL11.glViewport(0, 0, width, height);
			AL.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		camera = new Camera(width, height, 0.1f, 1000);
		
		GL11.glViewport(0, 0, width, height);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_BLEND); 
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		
		setupStarField();
		GLUtil.cerror(getClass().getName() + " <init>");
	}

	private void setupStarField()
	{
		//Setup starfield shader
		starfield_vsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/stars.vert"), GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		starfield_fsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/stars.frag"), GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		starfield_shader_id = ShaderUtils.makeProgram(starfield_vsId, starfield_fsId);
		
		GL20.glBindAttribLocation(starfield_shader_id, 0, "in_Position");
		GL20.glBindAttribLocation(starfield_shader_id, 1, "in_Color");
		
		vpMatrixPos = GL20.glGetUniformLocation(starfield_shader_id, "viewprojMatrix");
		
		GL20.glValidateProgram(starfield_shader_id);
		GLUtil.cerror(getClass().getName() + " setupShader");
		
		//Compile VBO
		float[] starColors = new float[STAR_COUNT * 4];
		float[] starVertices = new float[STAR_COUNT * 3];
		float distance = 500f;
		for (int i = 0; i < STAR_COUNT; i++)
		{
			float yaw = 	(float) (Math.random() * 2 * Math.PI);
			float pitch = 	(float) (Math.random() * 2 * Math.PI);
			starVertices[i + 0] = (float) (distance * Math.cos(yaw) * Math.sin(pitch));
			starVertices[i + 1] = (float) (distance * Math.sin(yaw) * Math.sin(pitch));
			starVertices[i + 2] = (float) (distance * Math.cos(pitch));
			
			starColors[i + 0] = (float) Math.random() + 0.8f;
			starColors[i + 1] = 0.9f;
			starColors[i + 2] = 0.9f;
			starColors[i + 3] = (float) Math.random() + 0.2f;
		}
		
		starVao_id = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(starVao_id);
		{
			vbovid = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbovid);
			{
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, GLUtil.buffer(starVertices), GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			}
			
			vbocid = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocid);
			{
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, GLUtil.buffer(starColors), GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
			}
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
		}
		GL30.glBindVertexArray(0);
	}

	public float[] getViewProjectionMatrix()
	{
		return camera.getViewProjectionMatrix();
	}
	
	protected void tick()
	{
		parttick++;
		if(parttick >= 60) parttick = 0;
	}
	
	public Model getModel(String name)
	{
		return modelpool.getModel(name);
	}
	
	public void drawStarField()
	{
		ShaderUtils.useProgram(starfield_shader_id);
		{
			ShaderUtils.setUniformMatrix4(starfield_shader_id, "viewprojMatrix", vpMatrix);
			GL30.glBindVertexArray(starVao_id);
			{
				GL11.glDrawArrays(GL11.GL_POINTS, 0, STAR_COUNT);
			}
		}
	}
	
	public void destroy()
	{
		AL.destroy();
		Display.destroy();
	}

	private int parttick;
	public float getPartTick()
	{
		return parttick * 1f/60;
	}
	
	public int getTexture(String name, int textureUnit)
	{
		File file = resources.getResource(name);
		ByteBuffer buf = null;
		int tWidth = 0;
		int tHeight = 0;
		
		int texId;
		
		try
		{
			// Open the PNG file as an InputStream
			InputStream in = new FileInputStream(file);
			// Link the PNG decoder to this stream
			PNGDecoder decoder = new PNGDecoder(in);

			// Get the width and height of the texture
			tWidth = decoder.getWidth();
			tHeight = decoder.getHeight();

			// Decode the PNG file in a ByteBuffer
			buf = ByteBuffer.allocateDirect(4 * decoder.getWidth()
					* decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();
			in.close();
			
		} catch (IOException e)
		{
			System.err.println("Failed to load texture: " + e.getMessage());
			return 0;
		}
		// Create a new texture object in memory and bind it
		
		GL13.glActiveTexture(textureUnit);
		texId = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

		// All RGB bytes are aligned to each other and each component is 1 byte
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		// Upload the texture data and generate mip maps (for scaling)
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, tWidth, tHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		// Setup the ST coordinate system
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		// Setup what to do when the texture has to be scaled
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		GLUtil.cerror("RenderEngine.getTexture");
		return texId;
	}
}

package hms_gotland_client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;

import com.bulletphysics.linearmath.Transform;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import level.DrawableLevel;
import model.GLShader;
import model.Model;
import model.ModelPool;
import model.GLVao;
import model.GLVbo;

import Util.GLUtil;
import Util.ShaderUtils;

public class RenderEngine
{	
	private static final int STAR_COUNT = 10000;

	public static CLContext clcontext;
	public static CLCommandQueue queue;
	
	public Camera camera;
	public ModelPool modelpool = new ModelPool(this);
	private HMS_Gotland game;
	public ResourceManager resources;
	private int width;
	private int height;

	private GLShader shader;
	private GLVbo starVbo;
	private GLVao starVao;
	
	
	public RenderEngine(HMS_Gotland game, int width, int height)
	{
		this.game = game;
		this.width = width;
		this.height = height;
		resources = game.getResourceManager();
		PixelFormat pixelFormat = new PixelFormat(24, 8, 8, 0, GLUtil.getMaxSamplings());
		ContextAttribs contextAtrributes = new ContextAttribs(3, 2);
		contextAtrributes.withForwardCompatible(true);
		contextAtrributes.withProfileCore(true);
		try 
		{
			Display.setDisplayMode(new DisplayMode(width, height));
			//Display.setTitle(WINDOW_TITLE);
			Display.create(pixelFormat, contextAtrributes);
	//		setupOpenCL();
		} catch (LWJGLException e) {
			e.printStackTrace();
			Sys.alert("Error: ", e.getMessage());
			System.exit(-1);
		}
		setupOpenGL();
	}

	public void setupOpenGL()
	{
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
		origin.setIdentity();
	}
	
	Transform origin = new Transform();
	public void drawLevel(DrawableLevel level) {
		
		drawModel(level.getLevelModel(), 0, origin);
		
	}

	public void render(DrawableLevel level) {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		drawStarField();
		if(level != null)
		{
			drawLevel(level);
		}
	}
	
	private float[] tmpMatrix = new float[16];
	public synchronized void drawModel(String model, float frame, Transform tr)
	{
		Model mdl = modelpool.getModel(model);
		System.out.println(mdl + ", " + tr.origin);
		tr.getOpenGLMatrix(tmpMatrix);
		mdl.draw(frame, getViewProjectionMatrix(), tmpMatrix, this);
	}
	
	public void setFullScreen(boolean on)
	{
		if(on){
			try {
				Display.setDisplayMode(Display.getDesktopDisplayMode());
				GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
				Display.setFullscreen(true);
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
			
		}else{
			try {
				Display.setDisplayMode(new DisplayMode(width, height));
				GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setupStarField()
	{
		shader = new GLShader("Resources/shaders/stars");
		shader.bindAttribLocation(0, "in_Position");
		shader.bindAttribLocation(1, "in_Color");
		GLUtil.cerror(getClass().getName() + " setupShader");
		
		//Compile VBO
		starVbo = new GLVbo(GL15.GL_ARRAY_BUFFER);
		float distance = 500f;
		Random rnd = new Random();
		for (int i = 0; i < STAR_COUNT; i++)
		{
			float yaw = 	(float) (rnd.nextFloat() * 2 * Math.PI);
			float pitch = 	(float) (rnd.nextFloat() * 2 * Math.PI);
			starVbo.addElements((float) (distance * Math.cos(yaw) * Math.sin(pitch)));
			starVbo.addElements((float) (distance * Math.sin(yaw) * Math.sin(pitch)));
			starVbo.addElements((float) (distance * Math.cos(pitch)));
			
			starVbo.addElements((float) rnd.nextFloat() + 0.8f);
			starVbo.addElements(0.9f);
			starVbo.addElements(0.9f);
			starVbo.addElements((float) rnd.nextFloat() + 0.2f);
		}
		starVbo.compile(GL15.GL_STATIC_DRAW);
		
		starVao = new GLVao();
		starVao.bind();
		{
			starVao.addBuffer(0, 3, GL11.GL_FLOAT, 28, 0, starVbo);
			starVao.addBuffer(1, 4, GL11.GL_FLOAT, 28, 12, starVbo);
		}
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
		shader.bind();
		shader.setUniformMatrix4("viewprojMatrix", getViewProjectionMatrix());
		starVao.drawArrays(GL11.GL_POINTS, 0, STAR_COUNT);
	}
	
	public void destroy()
	{
		//AL.destroy();
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

package hms_gotland_core;

import java.io.File;
import java.nio.FloatBuffer;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import level.Level;
import model.ModelPool;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Matrix4f;

import entity.EntityPlayer;

import Util.GLUtil;
import Util.OSUtil;
import Util.ShaderUtils;

public class HMS_Gotland
{
	public static void main(String[] args) 
	{
		new HMS_Gotland();
	}
	
	// Setup variables
	private final String WINDOW_TITLE = "OpenGL 3 test";
	private final int WIDTH = 640;
	private final int HEIGHT = 480;
	// Shader variables
	private int vsId = 0;
	private int fsId = 0;
	private int pId = 0;
	// Texture variables
	private int[] texIds = new int[] {0, 0};
	private int textureSelector = 0;
	// Moving variables
	private int projectionMatrixLocation = 0;
	private int viewMatrixLocation = 0;
	public static int modelMatrixLocation = 0;
	private Matrix4f modelMatrix = null;
	
	private FloatBuffer matrix44Buffer = null;
	long lastFrame = 0;
	private int fps;
	private int currentfps;
	
	private Camera camera;
	private Level level;
	private Settings settings = new Settings();
	
	private long lastTick;
	
	public HMS_Gotland() 
	{
		System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + File.separator + "Resources" + File.separator + "native" + File.separator + OSUtil.getOS());
		run();
	}
	
	public void run()
	{
		setupOpenGL();
		System.out.println("====================INFO==================== ");
		System.out.println("Operating system: " + System.getProperty("os.name"));
		System.out.println("Graphics card: " + GL11.glGetString(GL11.GL_VENDOR));
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		System.out.println("Shader version: " + GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
		setCamera(new Camera(WIDTH, HEIGHT, 0.1f, 1000f));
		camera.setPos(new org.lwjgl.util.vector.Vector3f(0, -2, -10));
		setupMatrices();
		
		level = new Level("test", this);
		
		setupShaders();
		setupTextures();
		
		camera.owner = getPlayer();
		
		lastTick = lastFrame = Sys.getTime();
		
		while (!Display.isCloseRequested()) 
		{
			currentfps++;
			if(Sys.getTime() - lastTick >= 16)
			{
				level.tick();
				lastTick = Sys.getTime();
			}
			if(Sys.getTime() - lastFrame >= 1000)
			{
				lastFrame = Sys.getTime();
				fps = currentfps;
				currentfps = 0;
				Display.setTitle(WINDOW_TITLE + fps);
			}
			
			loopCycle();
			// Let the CPU synchronize with the GPU if GPU is tagging behind
			Display.update();
		}
		
		// Destroy OpenGL (Display)
		destroyOpenGL();
	}

	private void setupMatrices() 
	{
		// Setup model matrix
		modelMatrix = new Matrix4f();
		
		// Create a FloatBuffer with the proper size to store our matrices later
		matrix44Buffer = BufferUtils.createFloatBuffer(16);
	}

	private void setupTextures() {
		texIds[0] = GLUtil.loadPNGTexture("Resources/assets/asset1.png", GL13.GL_TEXTURE0);
		texIds[1] = GLUtil.loadPNGTexture("Resources/assets/asset2.png", GL13.GL_TEXTURE0);
	}

	private void setupOpenGL() {
		// Setup an OpenGL context with API version 3.2
		try {
			PixelFormat pixelFormat = new PixelFormat(24, 8, 8, 0, GLUtil.getMaxSamplings());
			ContextAttribs contextAtrributes = new ContextAttribs(3, 2);
			contextAtrributes.withForwardCompatible(true);
			contextAtrributes.withProfileCore(true);
			
			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
			Display.setTitle(WINDOW_TITLE);
			Display.create(pixelFormat, contextAtrributes);
			
			GL11.glViewport(0, 0, WIDTH, HEIGHT);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		
		
		// Setup an XNA like background color
		//GL11.glClearColor(0.4f, 0.6f, 0.9f, 0f);
		
		// Map the internal OpenGL coordinate system to the entire screen
		GL11.glViewport(0, 0, WIDTH, HEIGHT);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LESS);
	}
	
	private void setupShaders() {		
		// Load the vertex shader
		vsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/default.vert"), GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		fsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/default.frag"), GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		pId = ShaderUtils.makeProgram(vsId, fsId);

		// Position information will be attribute 0
		GL20.glBindAttribLocation(pId, 0, "in_Position");
		// Color information will be attribute 1
		GL20.glBindAttribLocation(pId, 1, "in_TextureCoord");
		// Textute information will be attribute 2
		GL20.glBindAttribLocation(pId, 2, "in_Normal");
		// Get matrices uniform locations
		projectionMatrixLocation = GL20.glGetUniformLocation(pId, "projectionMatrix");
		viewMatrixLocation = GL20.glGetUniformLocation(pId, "viewMatrix");
		modelMatrixLocation = GL20.glGetUniformLocation(pId, "modelMatrix");
		
		GL20.glValidateProgram(pId);
	}
	
	private void logicCycle() {
		float posDelta = 2;
		//-- Input processing
		
		while(Mouse.next())
		{
			if(!Mouse.getEventButtonState()) continue;
			
			if(Mouse.getEventButton() == 0 && !Mouse.isGrabbed())
			{
				Mouse.setGrabbed(true);
			}
		}
		
		if(Mouse.isGrabbed())
		{
			camera.setAngle(new org.lwjgl.util.vector.Vector3f(Mouse.getY(), Mouse.getEventX(), 0));
		}
		
		//Vector3f angle = VectorUtil.toBulletVector(camera.angle);
		
		//getPlayer().move(new Vector3f(0, 0, 0), angle);
		
		while(Keyboard.next()) 
		{			
			// Only listen to events where the key was pressed (down event)
			if (!Keyboard.getEventKeyState()) continue;
			
			// Switch textures depending on the key released
			switch (Keyboard.getEventKey()) 
			{
			case Keyboard.KEY_1:
				textureSelector = 0;
				break;
			case Keyboard.KEY_2:
				textureSelector = 1;
				break;
			}
			// Change model scale, rotation and translation values
			switch (Keyboard.getEventKey()) 
			{
				/*
				 * Game control keys
				 */
			case Keyboard.KEY_ESCAPE:
				Mouse.setGrabbed(false);
				break;
			case Keyboard.KEY_F5:
				level.reloadLevel();
				break;
				/*
				 * Movement keys
				 */
			case Keyboard.KEY_W:
				getPlayer().move(new Vector3f(0, 0, posDelta));
				break;
			case Keyboard.KEY_S:
				//getPlayer().move(new Vector3f(0, 0, -posDelta), angle);;
				break;
			case Keyboard.KEY_A:
				//getPlayer().move(new Vector3f(posDelta, 0, 0), angle);
				break;
			case Keyboard.KEY_D:
				//getPlayer().move(new Vector3f(-posDelta, 0, 0), angle);
				break;
				/*
				 * Character control keys
				 */
			case Keyboard.KEY_SPACE:
				//getPlayer().move(new Vector3f(0, posDelta, 0), angle);
				break;
			}
			
		}
		
		getCamera().update();
		//-- Update matrices
		// Reset view and model matrices
		modelMatrix = new Matrix4f();
		
		// Upload matrices to the uniform variables
		GL20.glUseProgram(pId);
		{
			getCamera().uploadBuffer(viewMatrixLocation, Camera.VIEW_MATRIX);
			getCamera().uploadBuffer(projectionMatrixLocation, Camera.PROJECTION_MATRIX);
			
			modelMatrix.store(matrix44Buffer); matrix44Buffer.flip();
			GL20.glUniformMatrix4(modelMatrixLocation, false, matrix44Buffer);
		}
		GL20.glUseProgram(0);
	}
	
	private void renderCycle() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GL20.glUseProgram(pId);
		
		// Bind the texture
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texIds[textureSelector]);
		
		// Bind to the VAO that has all the information about the vertices
		level.draw();
		
		GL20.glUseProgram(0);
	}
	
	private void loopCycle() 
	{
		// Update logic
		logicCycle();
		// Update rendered frame
		renderCycle();
		
	}
	
	private void destroyOpenGL() {	
		// Delete the texture
		GL11.glDeleteTextures(texIds[0]);
		GL11.glDeleteTextures(texIds[1]);
		
		// Delete the shaders
		GL20.glUseProgram(0);
		GL20.glDetachShader(pId, vsId);
		GL20.glDetachShader(pId, fsId);
		
		GL20.glDeleteShader(vsId);
		GL20.glDeleteShader(fsId);
		GL20.glDeleteProgram(pId);
		
		level.destroy();
		
		Display.destroy();
	}

	/**
	 * @return the camera
	 */
	public Camera getCamera()
	{
		return camera;
	}

	/**
	 * @param camera the camera to set
	 */
	public void setCamera(Camera camera)
	{
		this.camera = camera;
	}

	/**
	 * @return the player
	 */
	public EntityPlayer getPlayer()
	{
		return level.player;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(EntityPlayer player)
	{
		level.player = player;
	}

}

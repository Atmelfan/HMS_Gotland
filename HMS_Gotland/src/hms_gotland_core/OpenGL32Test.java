package hms_gotland_core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;

import Renderers.ModelObj;
import Util.GLUtil;
import Util.OSUtil;
import Util.ShaderUtils;
import Util.VertexData;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class OpenGL32Test
{
	// Entry point for the application
	public static void main(String[] args) 
	{
		new OpenGL32Test();
	}
	
	// Setup variables
	private final String WINDOW_TITLE = "OpenGL 3.2 test - fps: ";
	private final int WIDTH = 640;
	private final int HEIGHT = 480;
	// Quad variables
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
	private int modelMatrixLocation = 0;
	private Matrix4f modelMatrix = null;
	
	private FloatBuffer matrix44Buffer = null;
	long lastFrame = 0;
	private int fps;
	private int cfps;
	private Camera camera;
	private ModelObj obj;
	
	public OpenGL32Test() 
	{
		System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + File.separator + "Resources" + File.separator + "native" + File.separator + OSUtil.getOS());
		
		// Initialize OpenGL (Display)
		setupOpenGL();
		
		setupQuad();
		this.setupShaders();
		this.setupTextures();
		this.setupMatrices();
		lastFrame = Sys.getTime();
		
		while (!Display.isCloseRequested()) {
			// Do a single loop (logic/render)
			
			cfps++;
			if(Sys.getTime() - lastFrame >= 1000)
			{
				lastFrame = Sys.getTime();
				fps = cfps;
				cfps = 0;
				Display.setTitle(WINDOW_TITLE + fps);
			}
			loopCycle();
			// Force a maximum FPS of about 60
			//Display.sync(60);
			// Let the CPU synchronize with the GPU if GPU is tagging behind
			Display.update();
		}
		
		// Destroy OpenGL (Display)
		this.destroyOpenGL();
	}

	private void setupMatrices() 
	{
		//Setup camera
		camera = new Camera(WIDTH, HEIGHT, 0.1f, 1000f);
		
		// Setup model matrix
		modelMatrix = new Matrix4f();
		
		// Create a FloatBuffer with the proper size to store our matrices later
		matrix44Buffer = BufferUtils.createFloatBuffer(16);
	}

	private void setupTextures() {
		texIds[0] = GLUtil.loadPNGTexture("Resources/assets/asset1.png", GL13.GL_TEXTURE0);
		texIds[1] = GLUtil.loadPNGTexture("Resources/assets/asset2.png", GL13.GL_TEXTURE0);
		
		this.exitOnGLError("setupTexture");
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
		
		this.exitOnGLError("setupOpenGL");
	}
	
	private void setupQuad() {
		// We'll define our quad using 4 vertices of the custom 'TexturedVertex' class
		obj = new ModelObj(new File("Resources/models/crawler.obj"));
		// Set the default quad rotation, scale and position values
		
		this.exitOnGLError("setupQuad");
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
		GL20.glBindAttribLocation(pId, 1, "in_Color");
		// Textute information will be attribute 2
		GL20.glBindAttribLocation(pId, 2, "in_TextureCoord");
		// Get matrices uniform locations
		projectionMatrixLocation = GL20.glGetUniformLocation(pId, "projectionMatrix");
		viewMatrixLocation = GL20.glGetUniformLocation(pId, "viewMatrix");
		modelMatrixLocation = GL20.glGetUniformLocation(pId, "modelMatrix");
		
		GL20.glValidateProgram(pId);
		
		this.exitOnGLError("setupShaders");
	}
	
	private void logicCycle() {
		//-- Input processing
		float posDelta = 0.1f;
		
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
			case Keyboard.KEY_ESCAPE:
				Mouse.setGrabbed(false);
				break;
			case Keyboard.KEY_UP:
				camera.pos.x += posDelta;
				break;
			case Keyboard.KEY_DOWN:
				camera.pos.x -= posDelta;
				break;
			// Scale
			case Keyboard.KEY_P:
				camera.pos.y += posDelta;
				break;
			case Keyboard.KEY_M:
				camera.pos.y -= posDelta;
				break;
			// Rotation
			case Keyboard.KEY_LEFT:
				camera.pos.z += posDelta;
				break;
			case Keyboard.KEY_RIGHT:
				camera.pos.z -= posDelta;
				break;
			}
			
		}
		
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
			camera.angle.x -= Mouse.getDY();
			camera.angle.y += Mouse.getDX();
		}
		
		camera.update();
		//-- Update matrices
		// Reset view and model matrices
		modelMatrix = new Matrix4f();
		
		// Upload matrices to the uniform variables
		GL20.glUseProgram(pId);
		{
			camera.uploadBuffer(projectionMatrixLocation, Camera.PROJECTION_MATRIX);
			camera.uploadBuffer(viewMatrixLocation, Camera.VIEW_MATRIX);
			modelMatrix.store(matrix44Buffer); matrix44Buffer.flip();
			GL20.glUniformMatrix4(modelMatrixLocation, false, matrix44Buffer);
		}
		GL20.glUseProgram(0);
		
		this.exitOnGLError("logicCycle");
	}
	
	private void renderCycle() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GL20.glUseProgram(pId);
		
		// Bind the texture
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texIds[textureSelector]);
		
		// Bind to the VAO that has all the information about the vertices
		obj.draw();
		
		GL20.glUseProgram(0);
		
		this.exitOnGLError("renderCycle");
	}
	
	private void loopCycle() {
		// Update logic
		this.logicCycle();
		// Update rendered frame
		this.renderCycle();
		
		this.exitOnGLError("loopCycle");
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
		
		obj.destroy();
		
		this.exitOnGLError("destroyOpenGL");
		
		Display.destroy();
	}
	
	private void exitOnGLError(String errorMessage) {
		int errorValue = GL11.glGetError();
		
		if (errorValue != GL11.GL_NO_ERROR) {
			String errorString = GLU.gluErrorString(errorValue);
			System.err.println("ERROR - " + errorMessage + ": " + errorString);
			
			if (Display.isCreated()) Display.destroy();
			System.exit(-1);
		}
	}
}

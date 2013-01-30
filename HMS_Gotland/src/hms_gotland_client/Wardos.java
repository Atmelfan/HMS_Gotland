package hms_gotland_client;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBTessellationShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;

import Util.GLUtil;
import Util.ShaderUtils;

public class Wardos
{
	private static final int FBO_HEIGHT = 512;
	private static final int FBO_WIDTH = 1024;
	
	public int textureID = 0;
	private int gui_id;
	private int fboID = 0;
	private int rboID = 0;
	private int vaoId;
	private int vsId;
	private int fsId;
	private int shader_id;
	private int indicesCount;
	
	private int vboiId;
	private int vbovId;
	private int vbocId;
	private FontRenderer font;
	
	public Wardos()
	{
		
		textureID = GL11.glGenTextures();
		fboID = GL30.glGenFramebuffers();
		rboID  = GL30.glGenRenderbuffers();
		setupShader();
		setupQuad();
		gui_id = GLUtil.loadPNGTexture("Resources/assets/WARDOS.png", GL13.GL_TEXTURE0);
		font = new FontRenderer("Agency FB", 60);
	}
	
	public void setupShader()
	{
		vsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/wardos.vert"), GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		fsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/wardos.frag"), GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		shader_id = ShaderUtils.makeProgram(vsId, fsId);
		
		GL20.glBindAttribLocation(shader_id, 0, "in_Position");
		GL20.glBindAttribLocation(shader_id, 1, "in_TextureCoord");
		
		GL20.glValidateProgram(shader_id);
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
				ShaderUtils.useProgram(0);
			}
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		}
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
	public void _render()
	{
		
		ShaderUtils.useProgram(shader_id);
		{
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, gui_id);
			// Bind to the VAO that has all the information about the vertices
			GL30.glBindVertexArray(vaoId);
			{
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				
				// Bind to the index VBO that has all the information about the order of the vertices
				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
				
				// Draw the vertices
				GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);
				
				// Put everything back to default (deselect)
				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
			}
			GL30.glBindVertexArray(0);
		}
		ShaderUtils.useProgram(0);
		
		font.drawString(1, 420, "WARDOS", 0f, 1f, 0f, 1f);
	}

	public void setupQuad() 
	{		
		// Vertices, the order is not important.
		float[] vertices = {
				-1f, 1f, 0f,	// Left top			ID: 0
				-1f, -1f, 0f,	// Left bottom		ID: 1
				1f, -1f, 0f,	// Right bottom		ID: 2
				1f, 1f, 0f		// Right left		ID: 3
		};
		// Sending data to OpenGL requires the usage of (flipped) byte buffers
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();
		
		// Vertices, the order is not important.
		float[] texture = {
				0, 0,	// Left top			ID: 0
				0, 1,	// Left bottom		ID: 1
				1, 1,	// Right bottom		ID: 2
				1, 0,	// Right left		ID: 3
		};
		// Sending data to OpenGL requires the usage of (flipped) byte buffers
		FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(texture.length);
		textureBuffer.put(texture);
		textureBuffer.flip();
		
		// OpenGL expects to draw vertices in counter clockwise order by default
		byte[] indices = {
				// Left bottom triangle
				0, 1, 2,
				// Right top triangle
				2, 3, 0
		};
		indicesCount = indices.length;
		ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();
		
		// Create a new Vertex Array Object in memory and select it (bind)
		// A VAO can have up to 16 attributes (VBO's) assigned to it by default
		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);
		{
			// Create a new Vertex Buffer Object in memory and select it (bind)
			// A VBO is a collection of Vectors which in this case resemble the location of each vertex.
			vbovId = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbovId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STREAM_DRAW);
			// Put the VBO in the attributes list at index 0
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			
			vbocId = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureBuffer, GL15.GL_STATIC_DRAW);
			// Put the VBO in the attributes list at index 0
			GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
		GL30.glBindVertexArray(0);
		
		// Create a new VBO for the indices and select it (bind)
		vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		// Deselect (bind to 0) the VBO
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);		
	}
	
	public void tick()
	{
		
	}
	
	public void command(String s)
	{
		
	}
}
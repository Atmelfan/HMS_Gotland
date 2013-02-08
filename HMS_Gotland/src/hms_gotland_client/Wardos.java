package hms_gotland_client;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.glu.GLU;

import Util.GLUtil;
import Util.ShaderUtils;

public class Wardos
{	
	private int gui_id;
	private int vaoId;
	private int vsId;
	private int fsId;
	private int shader_id;
	private int indicesCount;
	
	private int vboiId;
	private int vbovId;
	private int vbocId;
	private FontRenderer font;
	private HMS_Gotland game;
	
	public Wardos(HMS_Gotland game)
	{
		//Blablablabla code blabla and bla.
		//PS blablabla... bla!
		this.game = game;
		setupShader();
		setupQuad();
		gui_id = GLUtil.loadPNGTexture("Resources/assets/WardosScreen.png", GL13.GL_TEXTURE0);
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
	
	public void _render()
	{
		GL11.glDisable(GL11.GL_DEPTH_TEST);
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
				
				// Draw wthe vertices
				GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);
				
				// Put everything back to default (deselect)
				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
			}
			GL30.glBindVertexArray(0);
		}
		ShaderUtils.useProgram(0);
		
		font.drawString(4, 6, "Coord: N/A", 0f, 0.5f, 0f, 175/255f);
		//font.drawStringRightAdjusted(1024, 0, "Coord: " + game.getPlayer().getPos().toString(), 0f, 0.5f, 0f, 175/255f);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
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
		
		// Vertices, the order is not important.
		float[] texture = {
				0, 0,	// Left top			ID: 0
				0, 1,	// Left bottom		ID: 1
				1, 1,	// Right bottom		ID: 2
				1, 0,	// Right left		ID: 3
		};
		
		// OpenGL expects to draw vertices in counter clockwise order by default
		byte[] indices = {
				// Left bottom triangle
				0, 1, 2,
				// Right top triangle
				2, 3, 0
		};
		indicesCount = indices.length;
		
		// Create a new Vertex Array Object in memory and select it (bind)
		// A VAO can have up to 16 attributes (VBO's) assigned to it by default
		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);
		{
			// Create a new Vertex Buffer Object in memory and select it (bind)
			// A VBO is a collection of Vectors which in this case resemble the location of each vertex.
			vbovId = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbovId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, GLUtil.buffer(vertices), GL15.GL_STREAM_DRAW);
			// Put the VBO in the attributes list at index 0
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			
			vbocId = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, GLUtil.buffer(texture), GL15.GL_STATIC_DRAW);
			// Put the VBO in the attributes list at index 0
			GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
		GL30.glBindVertexArray(0);
		
		// Create a new VBO for the indices and select it (bind)
		vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, GLUtil.buffer(indices), GL15.GL_STATIC_DRAW);
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

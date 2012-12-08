package Util;

import hms_gotland_core.RenderHandler;

import java.nio.FloatBuffer;

import javax.vecmath.AxisAngle4f;

import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class RenderObject 
{
	private Matrix4f modelMatrix = new Matrix4f();
	
	public Vector3f pos = new Vector3f();
	public AxisAngle4f ori = new AxisAngle4f();
	
	private int fsId;
	private int vsId;

	private int projectionMatrixLocation;
	private int viewMatrixLocation;
	private int modelMatrixLocation;

	private int pId;

	private FloatBuffer buffer;


	public RenderObject()
	{
		setupShaders();
	}
	
	public void draw()
	{
		// Translate and rotate model
		Matrix4f.translate(pos, modelMatrix, modelMatrix);
		
		Matrix4f.rotate((float)Math.toRadians(ori.z), new Vector3f(0, 0, 1), modelMatrix, modelMatrix);
		Matrix4f.rotate((float)Math.toRadians(ori.y), new Vector3f(0, 1, 0), modelMatrix, modelMatrix);
		Matrix4f.rotate((float)Math.toRadians(ori.x), new Vector3f(1, 0, 0), modelMatrix, modelMatrix);
		//Upload matrixes
		GL20.glUseProgram(pId);

		RenderHandler.projectionMatrix.store(buffer); buffer.flip();
		GL20.glUniformMatrix4(projectionMatrixLocation, false, buffer);
		viewMatrix.store(buffer); buffer.flip();
		GL20.glUniformMatrix4(viewMatrixLocation, false, buffer);
		modelMatrix.store(buffer); buffer.flip();
		GL20.glUniformMatrix4(modelMatrixLocation, false, buffer);

		GL20.glUseProgram(0);
	}
	
	private void setupShaders() 
	{		
		// Load the vertex shader
		vsId = createShader(shader_vert, GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		fsId = createShader(shader_frag, GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		pId = GL20.glCreateProgram();
		GL20.glAttachShader(pId, vsId);
		GL20.glAttachShader(pId, fsId);
		GL20.glLinkProgram(pId);

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
	}
	
	private int createShader(String source, int type)
	{
		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, source);
		GL20.glCompileShader(shaderID);
		return shaderID;
	}
	
	
	
	private String shader_vert =
			"#version 150 core\n" +
			"\n" +
			"uniform mat4 projectionMatrix;\n" +
			"uniform mat4 viewMatrix;\n" +
			"uniform mat4 modelMatrix;\n" +
			"\n" +
			"in vec4 in_Position;\n" +
			"in vec4 in_Color;\n" +
			"in vec2 in_TextureCoord;\n" +
			"\n" +
			"out vec4 pass_Color;\n" +
			"out vec2 pass_TextureCoord;\n" +
			"\n" +
			"void main(void) \n" +
			"{\n" +
			"	gl_Position = in_Position;\n" +
			"	n\n" +
			"	gl_Position = projectionMatrix * viewMatrix * modelMatrix * in_Position;\n" +
			"	\n" +
			"	pass_Color = in_Color;\n" +
			"	pass_TextureCoord = in_TextureCoord;\n" +
			"}\n";
	
	private String shader_frag = 
			"#version 150 core\n" +
			"uniform sampler2D texture_diffuse;\n" +
			"\n" +
			"in vec4 pass_Color;\n" +
			"in vec2 pass_TextureCoord;\n" +
			"\n" +
			"out vec4 out_Color;\n" +
			"\n" +
			"void main(void)\n" +
			"{\n" +
			"	out_Color = pass_Color;\n" +
			"	out_Color = texture2D(texture_diffuse, pass_TextureCoord);\n" +
			"}\n";
}

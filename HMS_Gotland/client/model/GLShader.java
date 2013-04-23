package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLContext;

import Util.GLUtil;
import Util.ShaderUtils;

public class GLShader {
	public static int VERTEX = GL20.GL_VERTEX_SHADER;
	public static int FRAGMENT = GL20.GL_FRAGMENT_SHADER;
	public static int GEOMETRY = GL32.GL_GEOMETRY_SHADER;
	
	private int id;
	private int vid = 0;
	private int gid = 0;
	private int fid = 0;
	private int stdmatrices = -1;
	private int stdlight = -1;
	
	public GLShader(String name)
	{
		setup(name);
		setupUniformBlocks();
		GLUtil.cerror("Error loading " + name + " shader");
	}
	
	private void setupUniformBlocks()
	{
		if(stdmatrices != -1)
		{
			stdmatrices = GL31.glGetUniformBlockIndex(id, "matrices");
			GL31.glUniformBlockBinding(id, stdmatrices, 0);
		}
		if(stdlight != -1)
		{
			stdlight = GL31.glGetUniformBlockIndex(id, "lights");
			GL31.glUniformBlockBinding(id, stdlight, 1);
		}
	}

	public void bindAttribLocation(int i, String name)
	{
		GL20.glBindAttribLocation(id, i, name);
	}
	
	public void bind()
	{
		GL20.glUseProgram(id);
	}
	
	public void destroy()
	{
		GL20.glDetachShader(id, vid);
		GL20.glDetachShader(id, gid);
		GL20.glDetachShader(id, fid);
		GL20.glDeleteProgram(id);
		GL20.glDeleteShader(vid);
		GL20.glDeleteShader(gid);
		GL20.glDeleteShader(fid);
	}
	
	private void setup(String name) {
		vid = makeShader(loadText(name + ".vert"), VERTEX);
		gid = makeShader(loadText(name + ".geom"), GEOMETRY);
		fid = makeShader(loadText(name + ".frag"), FRAGMENT);
		
		id = makeProgram(vid, gid, fid);
	}

	private int makeProgram(int... shaders)
	{
		int id = GL20.glCreateProgram();
		for (int shader : shaders)
		{
			GL20.glAttachShader(id, shader);
			// GLException.checkGLError();
		}
		GL20.glLinkProgram(id);
		
		String s = GL20.glGetProgramInfoLog(id, 1000);
		if(GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) != GL11.GL_TRUE)
		{
			System.err.println("#####Shader link error#####\n" + s);
		}
		// GLException.checkGLError();
		return id;
	}
	
	private int makeShader(String source, int type)
	{
		if(source == null)
			return 0;
		if(source.contains("#matrices"))
		{
			source.replace("#matrices", 
					"//#matrices" + 
					"layout (std140) uniform matrices {\n" +
					"	mat4 proj;\n" +
					"	mat4 view;\n" +
					"	mat4 modl\n" +
					"}\n");
			stdmatrices = 0;
		}
		if(source.contains("#light"))
		{
			source.replace("#light", 
					"//#light" + 
					"layout (std140) uniform lights {\n" +
					"	vec4 eye;\n" +
					"	vec4 norm;\n" +
					"}\n");
			stdlight = 0;
		}
		int id = GL20.glCreateShader(type);
		GL20.glShaderSource(id, source);
		// GLException.checkGLError();
		GL20.glCompileShader(id);
		String s = GL20.glGetShaderInfoLog(id, 1000);
		if(GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
		{
			System.err.println("#####Error compiling shader!#####\n" + source);
			System.err.println(s);
			return 0;
		}
		// GLException.checkGLError();
		return id;
	}
	
	private String loadText(String s)
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(new File(s)));
			s = "";
			String line = br.readLine();
			while (line != null)
			{
				s += line;
				s += "\n";
				line = br.readLine();
			}
		} catch (IOException ex)
		{
			s = null;
		}
		
		if(br != null)
		{
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return s;
	}
	
	private  HashMap<String, Integer> uniformPositionCache = new HashMap<String, Integer>();
	public int getUniformVarPos(String var)
	{
		Integer i = uniformPositionCache.get(var);
		if(i == null)
		{
			i = GL20.glGetUniformLocation(id, var);
			uniformPositionCache.put(var, i);
		}
		return GL20.glGetUniformLocation(id, var);
	}

	/**
	 * Sets the value of the specified uniform variable to that given.
	 */
	public void setUniformVar(String name, float... values)
	{
		int loc = getUniformVarPos(name);
		switch (values.length)
		{
		case 1:
			GL20.glUniform1f(loc, values[0]);
			break;
		case 2:
			GL20.glUniform2f(loc, values[0], values[1]);
			break;
		case 3:
			GL20.glUniform3f(loc, values[0], values[1], values[2]);
			break;
		case 4:
			GL20.glUniform4f(loc, values[0], values[1], values[2], values[3]);
			break;
		default:
			FloatBuffer buff = BufferUtils.createFloatBuffer(values.length);
			buff.put(values);
			buff.rewind();
			GL20.glUniform1(loc, buff);
		}
		// GLException.checkGLError();
	}

	private static FloatBuffer temp = BufferUtils.createFloatBuffer(16);
	public void setUniformMatrix4(String name, float[] values)
	{
		int loc = getUniformVarPos(name);
		temp.put(values); temp.flip();
		GL20.glUniformMatrix4(loc, false, temp);
	}
	
	/**
	 * Sets the value of the specified uniform variable to that given.
	 */
	public void setUniformVar(String name, int... values)
	{
		int loc = getUniformVarPos(name);
		// GLException.checkGLError();
		switch (values.length)
		{
		case 1:
			GL20.glUniform1i(loc, values[0]);
			break;
		case 2:
			GL20.glUniform2i(loc, values[0], values[1]);
			break;
		case 3:
			GL20.glUniform3i(loc, values[0], values[1], values[2]);
			break;
		case 4:
			GL20.glUniform4i(loc, values[0], values[1], values[2], values[3]);
			break;
		default:
			IntBuffer buff = BufferUtils.createIntBuffer(values.length);
			buff.put(values);
			buff.rewind();
			GL20.glUniform1(loc, buff);
		}
	}
}



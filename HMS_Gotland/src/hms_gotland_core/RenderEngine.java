package hms_gotland_core;

import java.util.HashMap;

import Renderers.Model;
import Renderers.ModelPool;
import Util.ShaderUtils;

public class RenderEngine
{
	public static int  VERTEX_ATTRIB_POINTER = 0;
	public static int TEXTURE_ATTRIB_POINTER = 1;
	public static int  NORMAL_ATTRIB_POINTER = 2;
	
	public ModelPool modelpool = new ModelPool();
	
	private HashMap<String, Integer> shaders = new HashMap<>();
	
	protected void loop()
	{
		
	}
	
	public Model getModel(String name)
	{
		return modelpool.getModel(name);
	}
	
	public void destroy()
	{
		for (int i : shaders.values())
		{
			
		}
	}
	
	public void bindShader(String name)
	{
		ShaderUtils.useProgram(shaders.get(name));
	}
	
	public void unbindShader()
	{
		ShaderUtils.useProgram(0);
	}
	
	
	
}

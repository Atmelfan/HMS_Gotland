package model;

import hms_gotland_client.RenderEngine;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

import org.lwjgl.Sys;

public class ModelPool
{
	private HashMap<String, Model> models = new HashMap<String, Model>();

	private RenderEngine renderer;

	public ModelPool(RenderEngine renderer)
	{
		this.renderer = renderer;
	}
	
	public Model getModel(String name)
	{
		if(!models.containsKey(name))//Model is not loaded...
		{
			load(name);
			return models.get(name);
		}else//Model is loaded
		{
			return models.get(name);
		}
	}

	private boolean load(String name)
	{
		File file = renderer.resources.getResource(name);
		System.out.print(file.getAbsolutePath());
		if(!file.exists())//Model does not exist
		{
			System.out.println("ModelPool::file does not exist(" + file.getAbsolutePath() + ")");
			return false;
		}
		Model model = null;
		
		if(file.getName().endsWith(".obj"))
		{
			model = new ModelObj(renderer, file);
		}
		else if(file.getName().endsWith(".md2"))
		{
			model = new ModelMD2(renderer, file);
		}
		else if(file.getName().endsWith(".md3"))
		{
			model = new ModelMD3(renderer, file);
		}
		//Add model(or null if none found) to map
		return models.put(name, model) != null;
	}
	
	public boolean loadFolder(File file)
	{
		if(!file.exists() || !file.isDirectory())//Folder does not exist or isn't a folder
		{
			System.out.println("ModelPool::dir does not exist(" + file.getPath() + ")");
			return false;
		}
		//Get all files ending with .obj
		File[] files = file.listFiles(modelFilter);
		//Loop, load and store all models into hashmap
		for (int i = 0; i < files.length; i++)
		{
			if(files[i].getName().endsWith(".obj"))
			{
				System.out.println("ModelPool::load(" +  files[i].getName() + ") - " + 
						(models.put(files[i].getName(), new ModelObj(renderer, files[i])) == null ? "Failed!" : "Success!"));
			}
			else if(files[i].getName().endsWith(".md2"))
			{
				System.out.println("ModelPool::load(" +  files[i].getName() + ") - " + 
						(models.put(files[i].getName(), new ModelMD2(renderer, files[i])) == null ? "Failed!" : "Success!"));
			}
			else if(files[i].getName().endsWith(".md3"))
			{
				System.out.println("ModelPool::load(" +  files[i].getName() + ") - " + 
						(models.put(files[i].getName(), new ModelMD3(renderer, files[i])) == null ? "Failed!" : "Success!"));
			}
			
		}
		
		return true;
	}
	
	private static FilenameFilter modelFilter = new FilenameFilter()
	{
		@Override
		public boolean accept(File arg0, String arg1)
		{
			return arg1.endsWith(".obj") || arg1.endsWith(".md2") || arg1.endsWith(".md3");
		}
	};

	public void destroyUnused()
	{
		// TODO Auto-generated method stub
		
	}
}

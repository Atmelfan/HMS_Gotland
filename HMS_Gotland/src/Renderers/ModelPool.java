package Renderers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

public class ModelPool
{
	private HashMap<String, Model> models = new HashMap<String, Model>();
	
	private final String DEFAULT_MODEL_PATH = "Resources/models/";

	public Model getModel(String name)
	{
		if(!models.containsKey(name))//Model is not loaded...
		{
			load(DEFAULT_MODEL_PATH, name);//Load model
			return models.get(name);
		}else//Model is loaded
		{
			return models.get(name);
			
		}
	}

	private boolean load(String dir, String name)
	{
		File file = new File(dir + name);
		if(!file.exists())//Model does not exist
		{
			System.out.println("ModelPool::file does not exist(" + file.getAbsolutePath() + ")");
			return false;
		}	
		ModelObj model = new ModelObj(file, true);
		System.out.println("ModelPool::load(" +  file.getName() + ") - " + model.numpolygons() + " triangles");
		return models.put(name, model) != null;
	}
	
	/**
	 * Used when loading a new level
	 * @param file
	 * @return
	 */
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
			
			if(!models.containsKey(files[i].getName()))
			{
				Model model = new ModelObj(files[i], true);
				System.out.println("ModelPool::load(" +  files[i].getName() + ") - " + model.numpolygons() + " polygons");
				models.put(files[i].getName(), model);
			}else
			{
				System.out.println("ModelPool::skipped(" +  files[i].getName() + "), already loaded.");
			}
			
		}
		
		return true;
	}
	
	private static FilenameFilter modelFilter = new FilenameFilter()
	{
		@Override
		public boolean accept(File arg0, String arg1)
		{
			return arg1.endsWith(".obj") || arg1.endsWith(".md2");
		}
	};
}

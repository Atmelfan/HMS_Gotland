package level;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.bulletphysics.dynamics.RigidBody;

import Renderers.ModelObj;

public class Level
{
	private static String DEFAULT_LEVEL_PATH = "/Resources/level/";
	
	public String name;
	
	public ModelObj model;
	
	public RigidBody level;
	
	public Level(String name)
	{
		
	}
	
	public void read(File file)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String line = "";
			int lineCount = 0;
			
			while((line = reader.readLine()) != null)
			{
				lineCount++;
				line = line.toLowerCase().trim();
				
				if(line.startsWith("&name"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 1)
					{
						name = lines[1];
					}else
					{
						throw new InvalidLevelException(file.getName(), "Invalid &name command", lineCount);
					}
				}
				if(line.startsWith("&obj"))
				{
					String[] lines = line.split(" ");
					if(lines.length > 1)
					{
						model = new ModelObj(new File(file, lines[1]));
					}else
					{
						throw new InvalidLevelException(file.getName(), "Invalid &obj command", lineCount);
					}
				}
			}
			
		}catch (FileNotFoundException e)
		{
			System.out.println("Could not find level file!");
			e.printStackTrace();
		} catch (IOException e)
		{
			System.out.println("Could not read level file!");
			e.printStackTrace();
		} catch (InvalidLevelException e)
		{
			System.out.println("Invalid level file!");
			e.printStackTrace();
		}
	}
}

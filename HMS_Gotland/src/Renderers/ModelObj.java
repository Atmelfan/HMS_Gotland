package Renderers;

import java.io.File;

public class ModelObj extends Model
{
	

	
	/*public static int IDs = 0;
	private int id = IDs++;
	
	@Override
	public int hashCode()
	{
		return id;
	}*/
	
	public ModelObj(File file)
	{
		// TODO Auto-generated constructor stub
	}

	public static ModelObj get(File file)
	{
		ModelObj model = new ModelObj(file);
		return null;
	}
}

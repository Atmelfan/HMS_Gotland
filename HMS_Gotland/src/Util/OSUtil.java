package Util;

import java.io.File;

public class OSUtil 
{

	public static String getOS()
	{
		String os = System.getProperty("os.name").toLowerCase();
		System.out.println("Operating system: " + os);
		
		//Tested platform.
		if(os.contains("win"))
		{
			return "windows";
		}
		//Tested platform.
		if(os.contains("linux") || os.contains("unix"))
		{
			return "linux";
		}
		//Untested platform!
		if(os.contains("solaris") || os.contains("sunos"))
		{
			return "solaris";
		}
		//Untested platform!
		if(os.contains("mac"))
		{
			return "macosx";
		}
		
		return "unknown";	
	}
	
	public static String getSavePath()
	{
		return System.getProperty("user.home") + File.separator + ".hms_gotland" + File.separator;
	}
}

package Util;

public class OSUtil 
{

	public static String getOS()
	{
		String os = System.getProperty("os.name").toLowerCase();
		
		if(os.contains("win"))
		{
			return "windows";
		}
		
		if(os.contains("linux") || os.contains("unix"))
		{
			return "linux";
		}
		
		if(os.contains("solaris") || os.contains("sunos"))
		{
			return "solaris";
		}
		
		if(os.contains("mac"))
		{
			return "macosx";
		}
		
		return "unknown";	
	}
	
	public static String getContentPath()
	{
		return getJarPath().replace("bin/", "");
	}
	
	public static String getJarPath()
	{
		return OSUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}
}

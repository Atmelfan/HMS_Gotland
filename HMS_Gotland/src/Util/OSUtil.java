package Util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class OSUtil 
{

	public static String getOS()
	{
		String os = System.getProperty("os.name").toLowerCase();
		
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
	
	public static String getSessionRequestHash(String username) throws UnsupportedEncodingException
	{
		MessageDigest md = null;
	    try {
	        md = MessageDigest.getInstance("SHA-1");
	    }
	    catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } 
	    return new String(md.digest(username.getBytes("UTF-8")));
	}
	
	public static String getSessionLoginHash(int sessionID, String username, String password) throws UnsupportedEncodingException
	{
		MessageDigest md = null;
	    try {
	        md = MessageDigest.getInstance("SHA-1");
	    }
	    catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    byte[] t = md.digest(
	    		(new String(md.digest(username.getBytes("UTF-8"))) + new String(md.digest(password.getBytes("UTF-8")))).getBytes("UTF-8")
	    		);
	    return new String(t);
	}
}

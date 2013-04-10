package Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OSUtil 
{

	public static String getOS()
	{
		String os = System.getProperty("os.name").toLowerCase();
		
		if(os.contains("win"))
		{
			return "windows";
		}
		if(os.contains("ux") || os.contains("ix"))
		{
			return "linux";
		}
		if(os.contains("mac"))
		{
			return "macosx";
		}
		
		return "unknown";	
	}
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("YY-MM-dd/HH.mm.ss");
	public static String getTime()
	{
		Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	return dateFormat.format(cal.getTime());
	}
	
	public static byte[] generateMD5(FileInputStream inputStream){
	    if(inputStream==null){

	        return null;
	    }
	    MessageDigest md;
	    try {
	        md = MessageDigest.getInstance("MD5");
	        FileChannel channel = inputStream.getChannel();
	        ByteBuffer buff = ByteBuffer.allocate(2048);
	        while(channel.read(buff) != -1)
	        {
	            buff.flip();
	            md.update(buff);
	            buff.clear();
	        }
	        return md.digest();
	    }
	    catch (NoSuchAlgorithmException e)
	    {
	        return null;
	    } 
	    catch (IOException e) 
	    {
	        return null;
	    }
	    finally
	    {
	        try {
	            if(inputStream!=null)inputStream.close();
	        } catch (IOException e) {

	        }
	    } 
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException 
	{
		 if(!destFile.exists()) 
		 {
			 destFile.createNewFile();
		 }
		 
		 FileChannel source = null;
		 FileChannel destination = null;
		 try 
		 {
			 source = new FileInputStream(sourceFile).getChannel();
			 destination = new FileOutputStream(destFile).getChannel();
		  	 destination.transferFrom(source, 0, source.size());
		 }
		 finally 
		 {
			 if(source != null) {
				 source.close();
			 }
			 if(destination != null) 
			 {
				 destination.close();
			 }
		}
	}
}

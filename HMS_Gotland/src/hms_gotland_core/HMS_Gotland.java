package hms_gotland_core;

import java.io.File;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import Util.OSUtil;

public class HMS_Gotland
{
	public static HMS_Gotland game;
	private boolean exit;
	
	private void run()
	{
		init();
		
		while(!exit && !Display.isCloseRequested())
		{
			//Game loop
		}
		
	}

	private void init()
	{
		System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + File.separator + "Resources" + File.separator + "native" + File.separator + OSUtil.getOS());
		try
		{
			Display.create();
		} catch (LWJGLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		game = new HMS_Gotland();
		game.run();
	}
}

package hms_gotland_core;

import java.io.File;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import Util.OSUtil;

public class HMS_Gotland
{
	public static HMS_Gotland game;
	
	public DisplayHandler display = new DisplayHandler(this);
	
	private void run()
	{
		init();
		
		while(!display.isCloseRequested())
		{
			display.update();
		}
		
	}

	private void init()
	{
		System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + File.separator + "Resources" + File.separator + "native" + File.separator + OSUtil.getOS());
		display.init();
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

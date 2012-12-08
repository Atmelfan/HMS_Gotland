package hms_gotland_core;

import java.io.File;

import Renderers.ModelPool;
import Util.OSUtil;

public class HMS_Gotland
{
	public static HMS_Gotland game;
	
	//Display
	public DisplayHandler display = new DisplayHandler(this);
	//Renderer
	public RenderHandler renderer = new RenderHandler(this);
	
	
	private void run()
	{
		System.out.println("===============INFO===============");
		init();
		
		while(!display.isCloseRequested())
		{
			display.update();
		}
		
	}

	private void init()
	{
		//Point LWJGL to system natives
		System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + File.separator + "Resources" + File.separator + "native" + File.separator + OSUtil.getOS());
		//Initiate display
		display.init();
		//Initiate renderer
		renderer.init();
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

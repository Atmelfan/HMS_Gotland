package hms_gotland_core;

import java.io.File;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;

import Renderers.ModelPool;
import Util.OSUtil;
import Util.ScreenShot;

public class HMS_Gotland
{
	public static HMS_Gotland game;
	
	//Display
	public DisplayHandler display = new DisplayHandler(this);
	//Renderer
	public RenderHandler renderer = new RenderHandler(this);

	private long lastTick;
	
	
	private void run()
	{
		init();
		
		while(!display.isCloseRequested())
		{
			updateTiming();
			display.update();
			while(Keyboard.next())
			{
				if(Keyboard.getEventKeyState())
				{
					if(Keyboard.getEventKey() == Keyboard.KEY_F1)
					{
						ScreenShot.takeScreenShot();
					}
				}
			}
		}
		
	}
	
	public void updateTiming()
	{
		if(Sys.getTime() - lastTick >= 16)//Tick ~60 times per second
		{
			tick();
			lastTick = Sys.getTime();
		}
	}
	
	private void tick()
	{
		// TODO Auto-generated method stub
		
	}

	private void init()
	{
		//Initiate timing
		lastTick = Sys.getTime();
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
		System.out.println("===============HMS_GOTLAND===============");
		//Point LWJGL to system natives
		System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + File.separator + "Resources" + File.separator + "native" + File.separator + OSUtil.getOS());
		game = new HMS_Gotland();
		game.run();
	}
}

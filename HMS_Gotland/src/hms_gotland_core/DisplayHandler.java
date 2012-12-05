package hms_gotland_core;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public class DisplayHandler 
{

	
	
	private static final boolean IS_THE_SCREEN_FOCUS_ISSUE_FIXED_YET = true;

	public DisplayHandler(HMS_Gotland game) 
	{
		// TODO Auto-generated constructor stub
	}

	public void init()
	{
		try
		{
                    DisplayMode mode = new DisplayMode(1000, 600);
                    Display.setResizable(IS_THE_SCREEN_FOCUS_ISSUE_FIXED_YET);
                    Display.setDisplayMode(mode);
                    Display.setTitle("The HMS Gotland(pre-pre-pre-alpha)");
                    Display.create();
                    Mouse.setGrabbed(true);
		}catch(LWJGLException e)
		{
                    e.printStackTrace();
		}
	}
	
	public void setDisplayMode(int width, int height, boolean fullscreen) 
	{

		// return if requested DisplayMode is already set
        if ((Display.getDisplayMode().getWidth() == width) && 
			(Display.getDisplayMode().getHeight() == height) && 
			(Display.isFullscreen() == fullscreen)) 
        {
			return;
		}
		
		try {
			DisplayMode targetDisplayMode = null;
			
			if (fullscreen) 
			{
				DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;
				
				for (int i=0;i<modes.length;i++) 
				{
					DisplayMode current = modes[i];
					
					if ((current.getWidth() == width) && (current.getHeight() == height)) 
					{
						if ((targetDisplayMode == null) || (current.getFrequency() >= freq))
						{
							if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) 
							{
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}
						}

						// if we've found a match for bpp and frequence against the 
						// original display mode then it's probably best to go for this one
						// since it's most likely compatible with the monitor
						if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel()) &&
						    (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) 
						{
							targetDisplayMode = current;
							break;
						}
					}
				}
			} else 
			{
				targetDisplayMode = new DisplayMode(width,height);
			}
			
			if (targetDisplayMode == null) {
				System.out.println("Failed to find value mode: "+width+"x"+height+" fs="+fullscreen);
				return;
			}

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);
			
		} catch (LWJGLException e) {
			System.out.println("Unable to setup mode "+width+"x"+height+" fullscreen="+fullscreen + e);
		}
	}
	
    public void onResize()
    {
       GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }
	
    public void update()
    {
		//Display.setTitle(game.getTitle());
    	if(Display.wasResized())
    	{
            onResize();
    	}
        Display.update();
        Display.sync(60);
    	
    }

    public boolean isFulllScreen() 
    {
        return Display.isFullscreen();
    }
	
    public boolean isCloseRequested()
    {
	return Display.isCloseRequested();
    }
}

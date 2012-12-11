package hms_gotland_core;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

public class DisplayHandler 
{

	private int MAX_SAMPLES = 0;

	public DisplayHandler(HMS_Gotland game) 
	{
		// TODO Auto-generated constructor stub
	}

	public void init()
	{
		try
		{
			Pbuffer tmp = new Pbuffer(64, 64, new PixelFormat(), null);
			tmp.makeCurrent();
			MAX_SAMPLES = GL11.glGetInteger(GL30.GL_MAX_SAMPLES);
			tmp.destroy();
			System.out.println("System capable of " + MAX_SAMPLES + "x multisampling.");
		} catch (LWJGLException e1)
		{
			System.err.println("Failed to check multisampling capability :(");
			e1.printStackTrace();
		}
		
		try
		{
            DisplayMode mode = new DisplayMode(1000, 600);
            Display.setResizable(true);
            Display.setDisplayMode(mode);
            
            PixelFormat pixel = new PixelFormat(8, 24, 0, MAX_SAMPLES);
            ContextAttribs context = new ContextAttribs(3, 2);
            
            Display.create(pixel, context);
            Mouse.setGrabbed(true);
		}catch(LWJGLException e)
		{
			System.err.println("Failed to launch OpenGL 3.2 display :(");
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
				System.err.println("Failed to find value mode: "+width+"x"+height+" fs="+fullscreen);
				return;
			}

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);
			
		} catch (LWJGLException e) {
			System.err.println("Unable to setup mode "+width+"x"+height+" fullscreen="+fullscreen + e);
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

    public boolean isFullScreen() 
    {
        return Display.isFullscreen();
    }
	
    public boolean isCloseRequested()
    {
    	return Display.isCloseRequested();
    }
    
    public static int getX(float x)
    {
    	return (int)(Display.getWidth() * x);
    }
    
    public static int getY(float y)
    {
    	return (int)(Display.getHeight() * y);
    }

    @Override
    public String toString()
    {
    	return "Display[Fullscreen=" + isFullScreen() + 
    			", Title=" + Display.getTitle() + 
    			", Pos={" + Display.getX() + "," + Display.getY() + "}" +
    			", Size={" + Display.getHeight() + "," + Display.getWidth() + "}]";
    }
}

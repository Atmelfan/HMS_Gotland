package Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class ScreenShot
{
	public static void takeScreenShot()
	{
		GL11.glReadBuffer(GL11.GL_FRONT);
		int width = Display.getDisplayMode().getWidth();
		int height= Display.getDisplayMode().getHeight();
		int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer );
		new ScreenShotSaveThread(buffer).start();
	}
}

class ScreenShotSaveThread extends Thread
{
	private ByteBuffer buffer;

	private SimpleDateFormat file_time = new SimpleDateFormat("HH.mm.ss");
	private SimpleDateFormat folder_date = new SimpleDateFormat("YY-MMM");
	
	public ScreenShotSaveThread(ByteBuffer buffer)
	{
		super("ScreenShotSaveThread");
		this.buffer = buffer;

	}
	
	@Override
	public void run()
	{
		File file = new File(OSUtil.getSavePath() + "screenshots"  + File.separator
				+ time(folder_date) + File.separator + "screenshot_" + time(file_time) + ".png");
		if(!file.getParentFile().exists())
		{
			file.getParentFile().mkdirs();
		}
		  
		try
		{
			ImageIO.write(ImageIO.read(new ByteArrayInputStream(buffer.array())), "PNG", file);
		} catch (IOException e)
		{
			System.err.println("Error: ScreenShotSaveThread.run() - " + e.getMessage());
		}
		
		
	}
	
	private String time(SimpleDateFormat s) 
	{
    	Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	return s.format(cal.getTime());
    }
}

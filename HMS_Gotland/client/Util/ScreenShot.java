package Util;

import java.awt.image.BufferedImage;
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
		new ScreenShotSaveThread(buffer, height, width).start();
	}
}

class ScreenShotSaveThread extends Thread
{
	private ByteBuffer buffer;

	private SimpleDateFormat file_time = new SimpleDateFormat("ddd-HH.mm.ss");
	private SimpleDateFormat folder_date = new SimpleDateFormat("yy-MMM");

	private int width;

	private int height;
	
	public ScreenShotSaveThread(ByteBuffer buffer, int h, int w)
	{
		super("ScreenShotSaveThread");
		width = w;
		height = h;
		this.buffer = buffer;
		//setPriority(MIN_PRIORITY);
	}
	
	@Override
	public void run()
	{
		File file = new File(System.getProperty("user.home") + File.separator + "pictures" + File.separator + "screenshots"
			+ File.separator + time(folder_date) + File.separator + "screenshot_" + time(file_time) + ".png");
		if(!file.getParentFile().exists())
		{
			file.getParentFile().mkdirs();
		}
		  
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		  
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int i = (x + (width * y)) * 3;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
			}
		}
		  
		try {
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	private String time(SimpleDateFormat s) 
	{
    	Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	return s.format(cal.getTime());
    }
}

package Renderers;

import org.lwjgl.opengl.Display;

public abstract class Gui
{
	public abstract void init();
	
	public abstract void tick();
	
	protected abstract void _updateVBO();
	
	protected int x(float f)
	{
		return (int) (Display.getWidth() * f);
	}
	
	protected int y(float f)
	{
		return (int) (Display.getHeight() * f);
	}

}

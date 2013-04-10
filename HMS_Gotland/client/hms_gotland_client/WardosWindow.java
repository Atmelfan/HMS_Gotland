package hms_gotland_client;

public class WardosWindow
{
	public float x = 0, y = 0;
	public float height = 0, width = 0;
	
	public WardosWindow()
	{
	}
	
	public void getElements()
	{
		
	}
	
	protected class Button
	{
		float x = 0, y = 0;
		public float height = 0, width = 0;
		
		public float[] getElements()
		{
			return new float[]{};
		}
	}
	
	protected class Slider
	{
		float x = 0, y = 0;
		public float height = 0, width = 0;
		
		public float[] getElements()
		{
			return new float[]{};
		}
	}
	
	protected class ProgressBar
	{
		float x = 0, y = 0;
		public float height = 0, width = 0;
		
		public float[] getElements()
		{
			return new float[]{};
		}
	}
}

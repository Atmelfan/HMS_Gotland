package Renderers;

import Util.RenderObject;

public class ModelObject extends RenderObject
{
	Model model;
	
	public ModelObject(Model model)
	{
		super();
		this.model = model;
	}
	
	@Override
	public void draw()
	{
		super.draw();//Binds matrix and stuff...
		model.draw();
	}
}

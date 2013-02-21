package level;

import java.util.ArrayList;
import java.util.HashMap;

public class Script
{
	private HashMap<String, Script_event> events = new HashMap<>();
	
	public void runEvent(String event)
	{
		Script_event sevent = events.get(event);
		if(sevent != null)
		{
			sevent.run();
		}
	}
	
	private class Script_event
	{
		public ArrayList<String> cmds;

		public void run()
		{
			
		}
	}
	
}

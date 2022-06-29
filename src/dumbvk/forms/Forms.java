package dumbvk.forms;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

class Forms
{		
	Form form;
	List list;
	MIDlet midlet;
	
	public Forms(MIDlet m)
	{
		midlet = m;
	}
	
	public void show()
	{
		if (form != null)
			Display.getDisplay(midlet).setCurrent(form);
		else 
		if (list != null)
			Display.getDisplay(midlet).setCurrent(list);
		doAfterShow();
		System.gc();
	}

	/*Overridable*/
	protected void doAfterShow() {};
	
	public Displayable getDisplayable()
	{
		if (form != null)
			return form;
		else
		if (list != null)
			return list;
		
		return null;
	}
}


















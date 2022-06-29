package dumbvk;

import javax.microedition.midlet.*;

import dumbvk.forms.LoginForm;

public class DumbVKMidlet extends MIDlet
{
	public DumbVKMidlet()
	{
		super();
	}
	
	public void startApp()
	{	
		(new LoginForm(this)).show();
	}
	
	public void pauseApp()
	{ }
	
	public void destroyApp(boolean unconditional)
	{
		notifyDestroyed();
	}
}

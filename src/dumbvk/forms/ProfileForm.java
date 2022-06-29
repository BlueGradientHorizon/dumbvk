package dumbvk.forms;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import org.json.me.*;

import dumbvk.vkutils.VKSession;
import dumbvk.Utils;

public class ProfileForm extends Forms implements CommandListener
{
	
	private Command settings, exit;
	private VKSession session;
	
	public ProfileForm(MIDlet m, VKSession session)
	{
		super(m);
		
		this.session = session;
		
		//form = new Form("Мой профиль");
		list = new List("Мой профиль", List.IMPLICIT);
		settings = new Command("Настройки", Command.ITEM, 1);
		exit = new Command("Выйти из приложения", Command.EXIT, 1);
		list.addCommand(settings);
		//form.addCommand(settings);
		list.addCommand(exit);
		//form.addCommand(exit);
		
		list.append("", null);
		//form.append(img);
		
		//form.setCommandListener(this);
		list.setCommandListener(this);
	}
	
	protected void doAfterShow()
	{
		String m = session.method("users.get", "fields=photo_50");
		JSONObject jobj;
		if (m != null)
		{
			try 
			{
				jobj = new JSONObject(m).getJSONArray("response").getJSONObject(0);
				//img.setLabel(Utils.toUtf8(jobj.getString("first_name")+" "+jobj.getString("last_name")));
				//img.setImage(Image.createImage(Utils.fromUrl(jobj.getString("photo_50"), true)));
				list.set(
						0, 
						Utils.toUtf8(jobj.getString("first_name")+" "+jobj.getString("last_name")), 
						Image.createImage(Utils.fromUrl(jobj.getString("photo_50"), true)));
				list.setFont(0, Font.getFont(Font.SIZE_LARGE | Font.STYLE_BOLD));
				
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			} 
		}
		else
		{
			System.out.println(session.getLastApiError());
		}
	}
	
	public void commandAction(Command c, Displayable d)
	{
		if (c == exit)
			midlet.notifyDestroyed();
	}
}

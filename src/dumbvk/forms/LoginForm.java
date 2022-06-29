package dumbvk.forms;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import dumbvk.Records;
import dumbvk.Request;
import dumbvk.vkutils.VKSession;

public final class LoginForm extends Forms implements CommandListener
{	
	private Command ok, exit, test;
	private TextField login, passw;
	
	public LoginForm(MIDlet m)
	{
		super(m);
		
		form = new Form("Авторизация");
		login = new TextField("Телефон или e-mail", "safana53656@gmail.com", 50, TextField.ANY);
		passw = new TextField("Пароль", "Ernesto3495", 50, TextField.PASSWORD);
		ok = new Command("Вход", Command.OK, 1);
		exit = new Command("Выйти", Command.EXIT, 2);
		test = new Command("test", Command.OK, 3);
		
		form.append(login);
		form.append(passw);
		form.addCommand(ok);
		form.addCommand(exit);
		form.addCommand(test);
		
		form.setCommandListener(this);
	}
	
	/*@Override*/
	protected void doAfterShow()
	{
		System.out.println("loginform, need_code exist: "+Records.isRecordExist("need_code"));
		if (Records.isRecordExist("need_code"))
		{
			Records.deleteRecord("need_code");
			VKSession session = new VKSession();
			session.setCredentials(Records.getRecordString("login"), Records.getRecordString("password"));
			(new SMSCodeForm(midlet, session)).show();
		}
		System.out.println("loginform, access_token exist: "+Records.isRecordExist("access_token"));
		if (Records.isRecordExist("access_token"))
		{
			VKSession session = new VKSession();
			session.setAccessToken(Records.getRecordString("access_token"));
			(new ProfileForm(midlet, session)).show();
		}
	}
	
	public void commandAction(Command c, Displayable d)
	{
		if (c == ok)
		{
			if (login.getString().length() == 0 || 
				passw.getString().length() == 0)
			{
				Alert msg = new Alert(	"Пустые поля", 
										"Все поля должны быть заполнены", 
										null, 
										AlertType.ERROR);
				msg.setTimeout(2000);
				Display.getDisplay(midlet).setCurrent(msg, form);
				return;
			}
			
			Records.setRecord("login", login.getString());
			Records.setRecord("password", passw.getString());
			
			VKSession session = new VKSession();
			session.setCredentials(login.getString(), passw.getString());
			boolean l = session.Login();

			if (l)
				(new ProfileForm(midlet, session)).show();
			else
			if (session.isCaptchaNeeded())
			{
				//TODO: show captcha form
				Display.getDisplay(midlet).setCurrent(
						new Alert("Ошибка", "Работа с капчой еще не реализована, извините за неудобства", null, AlertType.ERROR), 
						form);
			}
			else if (session.isCodeNeeded())
				(new SMSCodeForm(midlet, session)).show();
			else
			{
				Display.getDisplay(midlet).setCurrent(
						new Alert("Ошибка", session.getLastErrorMessage(), null, AlertType.ERROR), 
						form);
			}
		}
		
		if (c == exit)
		{
			midlet.notifyDestroyed();
		}
		
		if (c == test)
		{
			try 
			{
				Request r = new Request("POST", "http://time100.ru/api", null, Request.APPLICATION_X_WWW_FORM_URLENCODED);
				r.send();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
}

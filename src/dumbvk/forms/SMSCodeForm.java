package dumbvk.forms;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import dumbvk.Records;
import dumbvk.vkutils.VKSession;

public class SMSCodeForm extends Forms implements CommandListener
{
	private TextField code;
	private Command sendcode, abort;
	private VKSession session;
	
	public SMSCodeForm(MIDlet m, VKSession session)
	{
		super(m);
		this.session = session;
		
		Records.setRecord("need_code", null);
		
		form = new Form("Код подтверждения");
		code = new TextField("Код из СМС", null, 6, TextField.NUMERIC);
		sendcode = new Command("Отправить", Command.OK, 1);
		abort = new Command("Назад", Command.CANCEL, 2);
		
		form.append(code);
		form.append("Если на данном устройстве нет возможности прочитать сообщение не выходя из приложения, "+
					"вы можете закрыть DumbVK, прочитать сообщение, запомнить код, а при следующем запуске этого приложения "+
					"ввести код, не вводя логин и пароль заново.");
		form.addCommand(sendcode);
		form.addCommand(abort);
		form.setCommandListener(this);
	}
	
	public void commandAction(Command c, Displayable d)
	{
		if (c == sendcode)
		{
			session.setCode(code.getString());
			boolean l = session.Login();
			System.out.println("l="+String.valueOf(l)+", code="+code.getString());
			if (l)
			{
				Records.cleanRecords();
				Records.setRecord("access_token", session.getAccessToken());
				Records.setRecord("user_id", session.getUserId());
				(new ProfileForm(midlet, session)).show();
			}
			else
			{
				Display.getDisplay(midlet).setCurrent(
						new Alert("Ошибка", session.getLastErrorMessage(), null, AlertType.ERROR));
			}
		}
		else
		if (c == abort)
		{
			Records.cleanRecords();
			(new LoginForm(midlet)).show();
		}
	}
}

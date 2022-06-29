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
		
		form = new Form("��� �������������");
		code = new TextField("��� �� ���", null, 6, TextField.NUMERIC);
		sendcode = new Command("���������", Command.OK, 1);
		abort = new Command("�����", Command.CANCEL, 2);
		
		form.append(code);
		form.append("���� �� ������ ���������� ��� ����������� ��������� ��������� �� ������ �� ����������, "+
					"�� ������ ������� DumbVK, ��������� ���������, ��������� ���, � ��� ��������� ������� ����� ���������� "+
					"������ ���, �� ����� ����� � ������ ������.");
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
						new Alert("������", session.getLastErrorMessage(), null, AlertType.ERROR));
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

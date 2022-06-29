package dumbvk.vkutils;

import org.json.me.*;

import dumbvk.Request;
import dumbvk.Settings;
import dumbvk.Utils;

public final class VKSession 
{
	private String login = null, password = null;
	private boolean need_captcha = false;
	private boolean need_code = false;
	private String lasterr = null;
	private String apilasterr = null;

	private String ACCESS_TOKEN = null;
	private String user_id;
	private String captcha_sid = null;
	private String captcha_img = null;
	private String captcha_key = null;
	private String code = null;
	
	public boolean Login()
	{
		Request r;
		try 
		{
			/* ������ �� ����� */
			String postparams = "grant_type=password&"+
								"2fa_supported=1&"+
								"force_sms=1&"+
								"username="+login+"&" +
								"password="+password+"&"+
								"client_id=6146827&"+
								"client_secret=qVxWRF1CwHERuIrKBnqe&"+
								"scope=notify";
			if (captcha_key != null)
				postparams += "&captcha_sid="+captcha_sid+
							  "&captcha_key="+captcha_key;
			if (code != null)
				postparams += "&code=" + code;
			r = new Request("POST", Settings.OAUTH_URL_DEFAULT + "token", postparams, Request.APPLICATION_X_WWW_FORM_URLENCODED);
			
			r.send();
			if (!r.getSuccess())
			{
				lasterr = "�� ���������� ��������� ������ /token";
				return false;
			}
		} 
		catch (Exception e) 
		{
			/* ������ �� ����� ������ */
			lasterr = "��������� ������ �������� ������� /token";
			e.printStackTrace();
			return false;
		}
		System.out.println(r.getResponse()); // ����� �� ������ ������
		JSONObject jobj;
		// �������� ������� ����� �� ������ ������
		try 
		{
			jobj = new JSONObject(r.getResponse());
			
			// ����� �� ������ ������ �������� ������
			if (jobj.has("error"))
			{
				String error = jobj.getString("error");
				
				// ��� ������ - �����
				if (error.equals("need_captcha"))
				{
					System.out.println("need captcha");
					captcha_sid = jobj.getString("captcha_sid");
					captcha_img = jobj.getString("captcha_img");
					need_captcha = true;
					lasterr = "��������� ���� �����";
					return false;
				}
				else 
				// ��� ������ - ���������
				if (error.equals("need_validation") && jobj.has("validation_sid"))
				{
					String validatephone_postparams = 
							"sid="+jobj.getString("validation_sid")+"&"+
							"v=5.131";
					Request smscode_req;
					// ���������� ������ �� ���������
					try 
					{
						smscode_req = new Request("POST", 
												  Settings.API_URL_DEFAULT + "method/auth.validatePhone", 
												  validatephone_postparams, 
												  Request.APPLICATION_X_WWW_FORM_URLENCODED);
						smscode_req.send();
						if (!smscode_req.getSuccess())
						{
							lasterr = "�� ������� ��������� ������ � ������ auth.validatePhone";
							return false;
						}
						System.out.println("sms code sent");
					} 
					// ������ �� ��������� �� ������
					catch (Exception e) 
					{
						// TODO Auto-generated catch block
						System.out.println("failed to send validatePhone request");
						lasterr = "��������� ������ �������� ������� � ������ auth.validatePhone";
						e.printStackTrace();
						return false;
					}
					
					JSONObject validatephone_jobj;
					// ������� ������� ����� �� ���������
					try
					{
						validatephone_jobj = new JSONObject(smscode_req.getResponse());
						// ������ �� ��������� �������������� �������
						if (validatephone_jobj.has("response"))
						{
							validatephone_jobj = validatephone_jobj.getJSONObject("response");
							System.out.println(validatephone_jobj);
							// ������ ������ ��������� ���������� �� �� �������� ���� sid 
							// (�� ������ � ����� ������ ����� ����� ���� :/)
							if (!validatephone_jobj.has("sid"))
							{
								System.out.println("validatePhone, key 'sid' in json object 'response' not found");
								lasterr = "� ������ �� ������ � ������ auth.ValidatePhone � JSON-������� 'response' �� ��� ������ ���� 'sid'";
								return false;
							}
							
							System.out.println("need code");
							need_code = true;
							lasterr = "��������� ���� ���� �� ��� ���������";
							return false;
						}
						else
						// � ������ ��������� ������
						if (validatephone_jobj.has("error"))
						{
							validatephone_jobj = validatephone_jobj.getJSONObject("error");
							System.out.println(validatephone_jobj);
							
							lasterr = "������ ������ ������ � ����� "+validatephone_jobj.getString("error_code")+": "+
									  validatephone_jobj.getString("error_msg");
							return false;
						}
						// ����������� ������ ��������� �� ������ ���������
						else
						{
							lasterr = "� ������ �� ������ � ������ auth.validatePhone �� ���������� JSON-������� 'response' ���� 'error'";
							return false;
						}
					}
					// ������� ������ �� ��������� �� ������
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						System.out.println("failed to parse validatePhone request");
						lasterr = "��������� ������ ��� ������ JSON-������ �� ������ auth.validatePhone";
						e.printStackTrace();
						return false;
					}
				}
				else
				// ��� ������ - ������������ ������ ���� ������ ������ ������ ����
				if ((error.equals("invalid_client") || error.equals("invalid_request")) && 
					jobj.has("error_description"))
				{
					lasterr = Utils.toUtf8(jobj.getString("error_description"));
					return false;
				}
				else
				// ������ ������ �� ������ ������ ����������
				{
					lasterr = "��� ������ �� ������� /token �����������";
					return false;
				}
			}
			else
			// ����� �� ������ ������ ��� �������� �����
			if (jobj.has("access_token"))
			{
				ACCESS_TOKEN = jobj.getString("access_token");
				user_id = jobj.getString("user_id");
				lasterr = null;
				return true;
			}
			// ������ ������ �� ������ ������ ����������
			else
			{
				System.out.println("error while parsing jobj");
				lasterr = "� ������ �� ������ /token �� ���������� JSON-������� 'error' ���� ����� 'access_token'";
				return false;
			}
		} 
		catch (JSONException e) 
		{
			// ������� ������ �� ������ ������ �� ������
			lasterr = "��������� ������ ��� ������ JSON-������ �� ������� /token";
			e.printStackTrace();
			return false;
		}
	}
	
	public String method(String method, String postparams)
	{
		if (postparams.indexOf("access_token=") < 0)
			postparams += "&access_token="+this.ACCESS_TOKEN;
		if (postparams.indexOf("v=") < 0)
			postparams += "&v=5.131";
		System.out.println("vkapi method postparams: "+postparams);
		Request r;
		try 
		{
			r = new Request("POST", Settings.API_URL_DEFAULT+"/method/"+method, postparams, Request.APPLICATION_X_WWW_FORM_URLENCODED);
			r.send();
			if (r.getSuccess())
			{
				apilasterr = null;
				return r.getResponse();
			}
			else
			{
				apilasterr = "�� ������� ��������� API ������ � ������ "+method;
				return null;
			}
		} 
		catch (Exception e) 
		{
			apilasterr = "��������� ������ ��� ���������� API ������� � ������ "+method;
			e.printStackTrace();
			return null;
		}
	}
	
	public String getLastApiError()
	{
		return apilasterr;
	}
	
	public void setCredentials(String login, String password)
	{
		this.login = login;
		this.password = password;
	}
	
	public boolean isCaptchaNeeded()
	{
		return need_captcha;
	}
	
	public boolean isCodeNeeded()
	{
		return need_code;
	}
	
	public String getCaptchaSid()
	{
		return captcha_sid;
	}
	
	public String getCaptchaImg()
	{
		return captcha_img;
	}
	
	public void setCaptchaKey(String captcha_key)
	{
		this.captcha_key = captcha_key;
		need_captcha = false;
	}
	
	public void setCode(String code)
	{
		this.code = code;
		need_code = false;
	}
	
	public String getAccessToken()
	{
		return ACCESS_TOKEN;
	}
	
	public void setAccessToken(String access_token)
	{
		this.ACCESS_TOKEN = access_token;
	}
	
	public String getUserId()
	{
		return this.user_id;
	}
	
	public String getLastErrorMessage()
	{
		return lasterr;
	}
}

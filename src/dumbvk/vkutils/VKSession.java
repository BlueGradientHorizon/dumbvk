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
			/* запрос на токен */
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
				lasterr = "Не получилось отправить запрос /token";
				return false;
			}
		} 
		catch (Exception e) 
		{
			/* запрос на токен ошибка */
			lasterr = "Произошла ошибка отправки запроса /token";
			e.printStackTrace();
			return false;
		}
		System.out.println(r.getResponse()); // ответ на запрос токена
		JSONObject jobj;
		// пытаемся парсить ответ на запрос токена
		try 
		{
			jobj = new JSONObject(r.getResponse());
			
			// ответ на запрос токена содержит ошибку
			if (jobj.has("error"))
			{
				String error = jobj.getString("error");
				
				// эта ошибка - капча
				if (error.equals("need_captcha"))
				{
					System.out.println("need captcha");
					captcha_sid = jobj.getString("captcha_sid");
					captcha_img = jobj.getString("captcha_img");
					need_captcha = true;
					lasterr = "Требуется ввод капчи";
					return false;
				}
				else 
				// эта ошибка - валидация
				if (error.equals("need_validation") && jobj.has("validation_sid"))
				{
					String validatephone_postparams = 
							"sid="+jobj.getString("validation_sid")+"&"+
							"v=5.131";
					Request smscode_req;
					// отправляем запрос на валидацию
					try 
					{
						smscode_req = new Request("POST", 
												  Settings.API_URL_DEFAULT + "method/auth.validatePhone", 
												  validatephone_postparams, 
												  Request.APPLICATION_X_WWW_FORM_URLENCODED);
						smscode_req.send();
						if (!smscode_req.getSuccess())
						{
							lasterr = "Не удалось отправить запрос к методу auth.validatePhone";
							return false;
						}
						System.out.println("sms code sent");
					} 
					// запрос на валидацию не удался
					catch (Exception e) 
					{
						// TODO Auto-generated catch block
						System.out.println("failed to send validatePhone request");
						lasterr = "Произошла ошибка отправки запроса к методу auth.validatePhone";
						e.printStackTrace();
						return false;
					}
					
					JSONObject validatephone_jobj;
					// пробуем парсить ответ на валидацию
					try
					{
						validatephone_jobj = new JSONObject(smscode_req.getResponse());
						// запрос на валидацию предварительно успешен
						if (validatephone_jobj.has("response"))
						{
							validatephone_jobj = validatephone_jobj.getJSONObject("response");
							System.out.println(validatephone_jobj);
							// формат ответа валидации неизвестен тк не содержит поля sid 
							// (хз только в каком случае такое может быть :/)
							if (!validatephone_jobj.has("sid"))
							{
								System.out.println("validatePhone, key 'sid' in json object 'response' not found");
								lasterr = "В ответе на запрос к методу auth.ValidatePhone в JSON-объекте 'response' не был найден ключ 'sid'";
								return false;
							}
							
							System.out.println("need code");
							need_code = true;
							lasterr = "Требуется ввод кода из СМС сообщения";
							return false;
						}
						else
						// в ответе валидации ошибка
						if (validatephone_jobj.has("error"))
						{
							validatephone_jobj = validatephone_jobj.getJSONObject("error");
							System.out.println(validatephone_jobj);
							
							lasterr = "Сервер вернул ошибку с кодом "+validatephone_jobj.getString("error_code")+": "+
									  validatephone_jobj.getString("error_msg");
							return false;
						}
						// неизвестный формат сообщения на запрос валидации
						else
						{
							lasterr = "В ответе на запрос к методу auth.validatePhone не обнаружено JSON-объекта 'response' либо 'error'";
							return false;
						}
					}
					// парсинг ответа на валидацию не удался
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						System.out.println("failed to parse validatePhone request");
						lasterr = "Произошла ошибка при чтении JSON-ответа от метода auth.validatePhone";
						e.printStackTrace();
						return false;
					}
				}
				else
				// эта ошибка - неправильные данные либо другая ошибка такого рода
				if ((error.equals("invalid_client") || error.equals("invalid_request")) && 
					jobj.has("error_description"))
				{
					lasterr = Utils.toUtf8(jobj.getString("error_description"));
					return false;
				}
				else
				// формат ответа на запрос токена неизвестен
				{
					lasterr = "Тип ошибки от запроса /token неизвестнен";
					return false;
				}
			}
			else
			// ответ на запрос токена уже содержит токен
			if (jobj.has("access_token"))
			{
				ACCESS_TOKEN = jobj.getString("access_token");
				user_id = jobj.getString("user_id");
				lasterr = null;
				return true;
			}
			// формат ответа на запрос токена неизвестен
			else
			{
				System.out.println("error while parsing jobj");
				lasterr = "В ответе на запрос /token не содержится JSON-объекта 'error' либо ключа 'access_token'";
				return false;
			}
		} 
		catch (JSONException e) 
		{
			// парсинг ответа на звпрос токена не удался
			lasterr = "Произошла ошибка при чтении JSON-ответа от запроса /token";
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
				apilasterr = "Не удалось выполнить API запрос к методу "+method;
				return null;
			}
		} 
		catch (Exception e) 
		{
			apilasterr = "Произошла ошибка при выполнении API запроса к методу "+method;
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

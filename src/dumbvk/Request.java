package dumbvk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public final class Request 
{
	private String method;
	private String URL;
	private String reqbody;
	private String respbody;
	private String mimetype;
	
	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
	public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	public static final String IMAGE_GIF = "image/gif";
	public static final String IMAGE_JPEG = "image/jpeg";
	public static final String IMAGE_PNG = "image/png";
	public static final String AUDIO_MPEG = "audio/mpeg";
	public static final String AUDIO_OGG = "audio/ogg";
	public static final String AUDIO_WAV = "audio/wav";
	public static final String AUDIO_WEBM = "audio/webm";
	
	private static final String USER_AGENT = 
			//"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36";
			//"KateMobileAndroid/78.1 lite-500 (Android 11; SDK 30; arm64-v8a; Xiaomi POCO X3 NFC; ru)";
			//"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36";
			"com.vk.vkclient/12 (unknown, iPhone OS 9.3.5, iPhone, Scale/2.000000)";
			
	private boolean success;
	private byte[] data;
	
	public Request(String method, String URL, String reqbody, String mimetype) throws Exception
	{
		this.method = method;
		this.URL = URL;
		this.reqbody = reqbody;
		this.mimetype = mimetype;
		
		if (!method.equals(HttpConnection.GET)  &&
			!method.equals(HttpConnection.POST) &&
			!method.equals(HttpConnection.HEAD))
			throw new Exception("Wrong or unsupported method name");
	}
	
	public boolean send()
	{
		HttpConnection c = null;
        InputStream is = null;
        OutputStream os = null;
        
        try 
        {
            c = (HttpConnection)Connector.open(URL);
            c.setRequestMethod(method);
            c.setRequestProperty("User-Agent", USER_AGENT);
            c.setRequestProperty("Content-Type", mimetype);

            if (reqbody != null)
            {
            	c.setRequestProperty("Content-Length", ""+reqbody.getBytes().length);
            	os = c.openOutputStream();
                os.write(reqbody.getBytes());
                os.flush();
            }
            else
            	c.setRequestProperty("Content-Length", "0");
            
            is = c.openInputStream();

            int len = (int)c.getLength();
            if (len > 0) 
            {
                 int actual = 0;
                 int bytesread = 0 ;
                 data = new byte[len];
                 while ((bytesread != len) && (actual != -1)) 
                 {
                    actual = is.read(data, bytesread, len - bytesread);
                    bytesread += actual;
                 }
                 respbody = new String(data);
            }
            /* when len is < 0 ?
            else 
            {
                int ch;
                System.out.print("ch: ");
                while ((ch = is.read()) != -1) 
                {
                	System.out.print((byte)ch);
                }
            }
            */
            success = true;
            return true;
        }
        catch (Exception e)
        {
        	// TODO: handle exception better
        	return false;
        }
        finally 
        {
        	try
        	{
        		if (is != null)
                    is.close();
                if (os != null)
                    os.close();
                if (c != null)
                    c.close();
        	}
        	catch (IOException e)
        	{
        		System.gc();
        		return false;
        	}
            
        }
	}
	
	public String getResponse()
	{
		return new String(respbody);
	}
	
	public byte[] getData()
	{
		return data;
	}
	
	public boolean getSuccess()
	{
		return success;
	}
}

package dumbvk;

import java.io.UnsupportedEncodingException;

import javax.microedition.lcdui.*;

public class Utils 
{
	public static Image resizeImage(Image image, int resizedWidth, int resizedHeight) 
	{
	    int width = image.getWidth();
	    int height = image.getHeight();

	    int[] in = new int[width];
	    int[] out = new int[resizedWidth * resizedHeight];

	    int dy, dx;
	    for (int y = 0; y < resizedHeight; y++) 
	    {
	        dy = y * height / resizedHeight;
	        image.getRGB(in, 0, width, 0, dy, width, 1);
	        for (int x = 0; x < resizedWidth; x++) {
	            dx = x * width / resizedWidth;
	            out[(resizedWidth * y) + x] = in[dx];
	        }
	    }
	    return Image.createRGBImage(out, resizedWidth, resizedHeight, true);
	}
	
	public static String replaceHttp(String url)
	{
		if (url.startsWith("https://"))
			url = "http://"+url.substring(8);
		return url;
	}
	
	public static Image fromUrl(String url, boolean toHttp)
	{
		if (toHttp)
			url = replaceHttp(url);
		Request r;
		try 
		{
			r = new Request("POST", url, null, Request.APPLICATION_X_WWW_FORM_URLENCODED);
			r.send();
			if (r.getSuccess())
				return Image.createImage(r.getData(), 0, r.getData().length);
			else 
				return null;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static String toUtf8(String s)
	{
		try 
		{
			return new String(s.getBytes(), "UTF-8");
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
			return s;
		}
	}
}

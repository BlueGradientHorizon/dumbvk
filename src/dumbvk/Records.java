package dumbvk;

import javax.microedition.rms.*;

public final class Records 
{
	public static boolean setRecord(String key, String value)
	{
		try
		{
			RecordStore r = RecordStore.openRecordStore(key, true);
			if (r.getNumRecords() > 0)
			{
				r.closeRecordStore();
				RecordStore.deleteRecordStore(key);
				r = RecordStore.openRecordStore(key, true);
			}
			
			if (value != null)
				r.addRecord(value.getBytes(), 0, value.length());
			else
				r.addRecord(null, 0, 0);
			
			r.closeRecordStore();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public static String getRecordString(String key)
	{
		try
		{
			RecordStore r = RecordStore.openRecordStore(key, false);
			RecordEnumeration e = r.enumerateRecords(null, null, true);
			if (!e.hasNextElement())
			{
				r.closeRecordStore();
				return null;
			}
			int id = e.nextRecordId();
			byte[] data = new byte[r.getRecordSize(id)];
			String s = new String(data, 0, r.getRecord(id, data, 0));
			r.closeRecordStore();
			return s;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean deleteRecord(String key)
	{
		try
		{
			RecordStore.deleteRecordStore(key);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean isRecordExist(String key)
	{
		try
		{
			RecordStore r = RecordStore.openRecordStore(key, false);
			boolean b = false;
			if (r.getNumRecords() == 1)
				b = true;
			r.closeRecordStore();
			return b;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean cleanRecords()
	{
		try
		{
			String s[] = RecordStore.listRecordStores();
			if (s == null)
				return true;
			int len = s.length;
			for (int i = 0; i < len; i++)
			{
				try
				{ 
					RecordStore.deleteRecordStore(s[i]); 
				}
				catch (Exception e) {e.printStackTrace();}
			}
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public static String[] listRecordStores()
	{
		try
		{
			return RecordStore.listRecordStores();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static int getNumRecords()
	{
		if (RecordStore.listRecordStores() == null)
			return 0;
		else
			return RecordStore.listRecordStores().length;
   	}
}



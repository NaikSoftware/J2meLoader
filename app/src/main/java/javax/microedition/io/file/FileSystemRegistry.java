/*
 * Copyright 2012 Kulikov Dmitriy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.microedition.io.file;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.ConnectionProvider;
import javax.microedition.io.Connector;

import android.os.Environment;

public class FileSystemRegistry implements ConnectionProvider
{
	public static final String INTERNAL_DISK = "c:/";
	public static final String EXTERNAL_DISK = "e:/";
	
	private static Vector roots = new Vector();
	
	public static Enumeration listRoots()
	{
		synchronized(roots)
		{
			roots.removeAllElements();
			roots.addElement(INTERNAL_DISK);
			
			String state = Environment.getExternalStorageState();
			
			if(Environment.MEDIA_MOUNTED.equals(state) ||
			   Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
			{
				roots.addElement(EXTERNAL_DISK);
			}
			
			return roots.elements();
		}
	}
	
	public static String getExternalStoragePath()
	{
		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		if(sdcard.startsWith("/"))
		{
			sdcard = sdcard.substring(1);
		}
		
		if(!sdcard.endsWith("/"))
		{
			sdcard += "/";
		}
		
		return sdcard;
	}
	
	public static String midpFileToAndroid(String name)
	{
		if(name.startsWith("/"))
		{
			name = name.substring(1);
		}
		
		if(name.startsWith(INTERNAL_DISK))
		{
			name = name.substring(INTERNAL_DISK.length());
		}
		else if(name.startsWith(EXTERNAL_DISK))
		{
			name = getExternalStoragePath() + name.substring(EXTERNAL_DISK.length());
		}
		
		return "/" + name;
	}
	
	public static String androidFileToMIDP(String name)
	{
		if(name.startsWith("/"))
		{
			name = name.substring(1);
		}
		
		String sdcard = getExternalStoragePath();
		
		if(name.startsWith(sdcard))
		{
			name = EXTERNAL_DISK + name.substring(sdcard.length());
		}
		else
		{
			name = INTERNAL_DISK + name;
		}
		
		return "/" + name;
	}
	
	public Connection open(String name, int mode)
	{
		return new FileConnection(new File(midpFileToAndroid(name)), (mode & Connector.WRITE) != 0 ? FileConnection.READ_WRITE : FileConnection.READ);
	}
}
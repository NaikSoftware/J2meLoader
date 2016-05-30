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

package javax.microedition.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.bluetooth.BTL2CAPConnectionProvider;
import javax.microedition.io.file.FileSystemRegistry;

public class Connector
{
	public static final int READ = 1;
	public static final int READ_WRITE = 3;
	public static final int WRITE = 2;
	
	private static HashMap<String, ConnectionProvider> providers;
	
	static
	{
		registerConnectionProvider("file", new FileSystemRegistry());
		registerConnectionProvider("http", new HttpConnectionProvider());
		registerConnectionProvider("btl2cap", new BTL2CAPConnectionProvider());
	}
	
	public static void registerConnectionProvider(String protocol, ConnectionProvider provider)
	{
		if(providers == null)
		{
			providers = new HashMap();
		}
		
		providers.put(protocol.toLowerCase(), provider);
	}
	
	public static Connection open(String name) throws IOException
	{
		return open(name, READ_WRITE);
	}
	
	public static Connection open(String name, int mode) throws IOException
	{
		if(providers == null || providers.isEmpty())
		{
			throw new ConnectionNotFoundException("no registered connection providers");
		}
		
		int index = name.indexOf("://");
		
		if(index >= 0)
		{
			String protocol = name.substring(0, index).toLowerCase();
			ConnectionProvider provider = providers.get(protocol);
			
			if(provider != null)
			{
				return provider.open(name.substring(index + 3), mode);
			}
			else
			{
				throw new ConnectionNotFoundException("'" + protocol + "' connections are not supported");
			}
		}
		else
		{
			throw new IllegalArgumentException("malformed URL: " + name);
		}
	}
	
	public static InputStream openInputStream(String name) throws IOException
	{
		Connection conn = open(name, READ);
		
		if(conn instanceof StreamConnection)
		{
			return new ImmediateInputStream((StreamConnection)conn);
		}
		else
		{
			throw new IOException("cannot open stream on a non-stream connection");
		}
	}
	
	public static OutputStream openOutputStream(String name) throws IOException
	{
		Connection conn = open(name, WRITE);
		
		if(conn instanceof StreamConnection)
		{
			return new ImmediateOutputStream((StreamConnection)conn);
		}
		else
		{
			throw new IOException("cannot open stream on a non-stream connection");
		}
	}
}
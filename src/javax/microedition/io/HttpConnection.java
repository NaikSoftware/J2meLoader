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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

public class HttpConnection implements StreamConnection
{
	protected HttpURLConnection conn;
	
	protected HttpConnection(HttpURLConnection conn)
	{
		this.conn = conn;
	}
	
	public void close()
	{
		conn.disconnect();
	}
	
	public InputStream openInputStream() throws IOException
	{
		return conn.getInputStream();
	}
	
	public DataInputStream openDataInputStream() throws IOException
	{
		return new DataInputStream(openInputStream());
	}
	
	public OutputStream openOutputStream() throws IOException
	{
		return conn.getOutputStream();
	}
	
	public DataOutputStream openDataOutputStream() throws IOException
	{
		return new DataOutputStream(openOutputStream());
	}

	public void setRequestMethod(String method) throws ProtocolException
	{
		conn.setRequestMethod(method);
	}

	public void setRequestProperty(String field, String newValue)
	{
		conn.setRequestProperty(field, newValue);
	}

	public int getResponseCode() throws IOException
	{
		return conn.getResponseCode();
	}
}
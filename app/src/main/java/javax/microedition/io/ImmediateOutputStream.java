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
import java.io.OutputStream;

public class ImmediateOutputStream extends OutputStream
{
	protected StreamConnection base;
	protected OutputStream out;
	
	public ImmediateOutputStream(StreamConnection conn) throws IOException
	{
		base = conn;
		out = conn.openOutputStream();
	}
	
	public void close() throws IOException
	{
		out.close();
		base.close();
	}
	
	public void flush() throws IOException
	{
		out.flush();
	}
	
	public void write(int b) throws IOException
	{
		out.write(b);
	}
	
	public void write(byte[] b) throws IOException
	{
		out.write(b, 0, b.length);
	}
	
	public void write(byte[] b, int off, int len) throws IOException
	{
		out.write(b, off, len);
	}
}

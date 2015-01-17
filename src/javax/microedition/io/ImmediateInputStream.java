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

public class ImmediateInputStream extends InputStream
{
	protected StreamConnection base;
	protected InputStream in;
	
	public ImmediateInputStream(StreamConnection conn) throws IOException
	{
		base = conn;
		in = conn.openInputStream();
	}
	
	public int available() throws IOException
	{
		return in.available();
	}
	
	public void close() throws IOException
	{
		in.close();
		base.close();
	}
	
	public void mark(int readLimit)
	{
		in.mark(readLimit);
	}
	
	public boolean markSupported()
	{
		return in.markSupported();
	}
	
	public int read() throws IOException
	{
		return in.read();
	}
	
	public int read(byte[] b) throws IOException
	{
		return in.read(b, 0, b.length);
	}
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		return in.read(b, off, len);
	}
	
	public void reset() throws IOException
	{
		in.reset();
	}
	
	public long skip(long n) throws IOException
	{
		return in.skip(n);
	}
}

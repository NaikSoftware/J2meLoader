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

package javax.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import android.bluetooth.BluetoothSocket;

public class L2CAPConnection implements StreamConnection
{
	public static final int DEFAULT_MTU = 672;
	public static final int MINIMUM_MTU = 48;
	
	protected BluetoothSocket socket;
	
	protected InputStream in;
	protected OutputStream out;
	
//	protected int receiveMTU;
//	protected int transmitMTU;
	
	public L2CAPConnection(BluetoothSocket socket) throws IOException
	{
		this.socket = socket;
		
		in = socket.getInputStream();
		out = socket.getOutputStream();
	}
	
	public void close() throws IOException
	{
		in.close();
		out.close();
		
		socket.close();
	}
	
	public int getReceiveMTU()
	{
		return DEFAULT_MTU; // receiveMTU;
	}
	
	public int getTransmitMTU()
	{
		return DEFAULT_MTU; // transmitMTU;
	}
	
	public boolean ready() throws IOException
	{
		return in.available() > 0;
	}
	
	public int receive(byte[] inBuf) throws IOException
	{
		return in.read(inBuf, 0, Math.min(inBuf.length, in.available()));
	}
	
	public void send(byte[] data) throws IOException
	{
		out.write(data);
	}

	public InputStream openInputStream() throws IOException
	{
		return in;
	}

	public OutputStream openOutputStream() throws IOException
	{
		return out;
	}
}
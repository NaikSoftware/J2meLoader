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

import javax.microedition.io.Connection;

import android.bluetooth.BluetoothServerSocket;

public class L2CAPConnectionNotifier implements Connection
{
	protected BluetoothServerSocket socket;
	
	public L2CAPConnectionNotifier(BluetoothServerSocket socket)
	{
		this.socket = socket;
	}
	
	public L2CAPConnection acceptAndOpen() throws IOException
	{
		return new L2CAPConnection(socket.accept());
	}

	public void close() throws IOException
	{
		socket.close();
	}
}
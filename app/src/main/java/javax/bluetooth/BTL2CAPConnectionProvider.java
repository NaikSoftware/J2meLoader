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
import javax.microedition.io.ConnectionProvider;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BTL2CAPConnectionProvider implements ConnectionProvider
{
	public Connection open(String name, int mode) throws IOException
	{
		if(name.startsWith("localhost:"))
		{
			String[] params = name.substring(10).split(";");
			name = null;
			
			for(int i = 1; i < params.length; i++)
			{
				if(params[i].startsWith("name="))
				{
					name = params[i].substring(5);
					break;
				}
			}
			
			if(name == null)
			{
				name = "MicroBTL2CAP";
			}
			
			return new L2CAPConnectionNotifier(BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(name, BTUtils.parseUUID(params[0])));
		}
		else
		{
			String[] params = name.split(";")[0].split(":");
			
			BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(BTUtils.validateBluetoothAddress(params[0]));
			BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BTUtils.parseUUID(params[1]));
			
			return new L2CAPConnection(socket);
		}
	}
}
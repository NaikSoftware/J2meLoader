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

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;

public class BTUtils
{
	public static UUID parseUUID(String uuidValue)
	{
		try
		{
			return UUID.fromString(uuidValue);
		}
		catch(IllegalArgumentException x)
		{
			StringBuilder buf = new StringBuilder(36);
			buf.append(uuidValue);
			
			while(buf.length() < 32)
			{
				buf.insert(0, '0');
			}
			
			buf.insert(8,  '-');
			buf.insert(13, '-');
			buf.insert(18, '-');
			buf.insert(23, '-');
			
			return UUID.fromString(buf.toString());
		}
	}
	
	public static String validateBluetoothAddress(String address)
	{
		if(BluetoothAdapter.checkBluetoothAddress(address))
		{
			return address;
		}
		else
		{
			StringBuilder buf = new StringBuilder(17);
			buf.append(address.toUpperCase());
			
			buf.insert(2, ':');
			buf.insert(5, ':');
			buf.insert(8, ':');
			buf.insert(11, ':');
			buf.insert(14, ':');
			
			return buf.toString();
		}
	}
	
	public static String formatBluetoothAddress(String address)
	{
		return address.replace(":", "");
	}
}
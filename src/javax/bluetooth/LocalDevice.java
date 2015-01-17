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

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import javax.microedition.util.ActivityResultListener;
import javax.microedition.util.ContextHolder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class LocalDevice implements DiscoveryAgent, ActivityResultListener
{
	protected static final int REQUEST_CODE_DISCOVERABLE = ContextHolder.getRequestCode("SetDiscoverable");
	
	protected static LocalDevice instance;
	protected static HashMap<String, String> properties;
	
	protected static final Object waiter = new Object();
	protected static final Random random = new Random();
	
	protected BluetoothAdapter adapter;
	
	static
	{
		properties = new HashMap();
		
		properties.put("bluetooth.api.version", "1.1");
		properties.put("bluetooth.master.switch", "true");
		properties.put("bluetooth.sd.attr.retrievable.max", "256");
		properties.put("bluetooth.connected.devices.max", "7");
		properties.put("bluetooth.l2cap.receiveMTU.max", "672");
		properties.put("bluetooth.sd.trans.max", "1");
		properties.put("bluetooth.connected.inquiry.scan", "true");
		properties.put("bluetooth.connected.page.scan", "true");
		properties.put("bluetooth.connected.inquiry", "true");
		properties.put("bluetooth.connected.page", "true");
	}
	
	public static String getProperty(String property)
	{
		return properties.get(property);
	}
	
	public static LocalDevice getLocalDevice() throws BluetoothStateException
	{
		if(instance == null)
		{
			instance = new LocalDevice();
			instance.adapter = BluetoothAdapter.getDefaultAdapter();
			
			if(instance.adapter == null)
			{
				throw new BluetoothStateException("this system does not have a bluetooth");
			}
		}
		
		return instance;
	}
	
	public String getBluetoothAddress()
	{
		return BTUtils.formatBluetoothAddress(adapter.getAddress());
	}
	
	public DiscoveryAgent getDiscoveryAgent()
	{
		return this;
	}
	
	public boolean setDiscoverable(int mode)
	{
		if(mode != NOT_DISCOVERABLE && getDiscoverable() == NOT_DISCOVERABLE)
		{
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			
			if(ContextHolder.hasCurrentActivity())
			{
				ContextHolder.addActivityResultListener(this);
				
				synchronized(waiter)
				{
					ContextHolder.getCurrentActivity().startActivityForResult(discoverableIntent, REQUEST_CODE_DISCOVERABLE);
					
					try
					{
						waiter.wait();
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == REQUEST_CODE_DISCOVERABLE)
		{
			synchronized(waiter)
			{
				waiter.notifyAll();
			}
		}
	}
	
	public int getDiscoverable()
	{
		int mode = adapter.getScanMode();
		
		switch(mode)
		{
			default:
			case BluetoothAdapter.SCAN_MODE_NONE:
				return NOT_DISCOVERABLE;
				
			case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
				return LIAC;
				
			case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
				return GIAC;
		}
	}
	
	public boolean startInquiry(int accessCode, final DiscoveryListener listener)
	{
		if(adapter.isDiscovering())
		{
			return false;
		}
		
		BroadcastReceiver receiver = new BroadcastReceiver()
		{
			public void onReceive(Context context, Intent intent)
			{
				String action = intent.getAction();
				
				if(BluetoothDevice.ACTION_FOUND.equals(action))
				{
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					listener.deviceDiscovered(new RemoteDevice(device), new DeviceClass(device.getBluetoothClass()));
				}
				else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
				{
					listener.inquiryCompleted(DiscoveryListener.INQUIRY_COMPLETED);
					ContextHolder.getContext().unregisterReceiver(this);
				}
			}
		};
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		
		ContextHolder.getContext().registerReceiver(receiver, filter);
		
		return adapter.startDiscovery();
	}
	
	public boolean cancelInquiry(DiscoveryListener listener)
	{
		return adapter.cancelDiscovery();
	}

	public int searchServices(final int[] attrSet, final UUID[] uuidSet, final RemoteDevice btDev, final DiscoveryListener discListener)
	{
		final int transID = random.nextInt() & 0x7FFFFFFF;
		
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				ServiceRecord[] services = new ServiceRecord[uuidSet.length];
				
				for(int i = 0; i < uuidSet.length; i++)
				{
					services[i] = new ServiceRecord(btDev, uuidSet[i]);
				}
				
				discListener.servicesDiscovered(transID, services);
				discListener.serviceSearchCompleted(transID, DiscoveryListener.SERVICE_SEARCH_COMPLETED);
			}
		};
		
		(new Thread(runnable)).start();
		
		return transID;
	}

	public boolean cancelServiceSearch(int transID)
	{
		return true;
	}
}
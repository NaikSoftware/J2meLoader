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

public interface DiscoveryListener
{
	public static final int INQUIRY_COMPLETED = 0;
	public static final int SERVICE_SEARCH_COMPLETED = 1;
	public static final int SERVICE_SEARCH_TERMINATED = 2;
	public static final int SERVICE_SEARCH_ERROR = 3;
	public static final int SERVICE_SEARCH_NO_RECORDS = 4;
	public static final int INQUIRY_TERMINATED = 5;
	public static final int SERVICE_SEARCH_DEVICE_NOT_REACHABLE = 6;
	public static final int INQUIRY_ERROR = 7;

	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod);
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord);
	public void serviceSearchCompleted(int transID, int respCode);
	public void inquiryCompleted(int discType);
}
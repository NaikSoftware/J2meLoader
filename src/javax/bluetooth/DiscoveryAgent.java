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

public interface DiscoveryAgent
{
	public static final int CACHED = 0;
	public static final int PREKNOWN = 1;
	
	public static final int NOT_DISCOVERABLE = 0;
	public static final int LIAC = 10390272;
	public static final int GIAC = 10390323;
	
	public boolean startInquiry(int accessCode, DiscoveryListener listener);
	public boolean cancelInquiry(DiscoveryListener listener);
	public int searchServices(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev, DiscoveryListener discListener);
	public boolean cancelServiceSearch(int transID);
}
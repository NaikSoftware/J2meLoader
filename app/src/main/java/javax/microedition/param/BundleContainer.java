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

package javax.microedition.param;

import android.os.Bundle;

public class BundleContainer implements DataContainer, DataEditor
{
	protected Bundle bundle;
	protected Bundle editor;
	
	public BundleContainer()
	{
		bundle = new Bundle();
	}
	
	public BundleContainer(Bundle bundle)
	{
		this.bundle = bundle;
	}
	
	public Bundle getBundle()
	{
		return bundle;
	}
	
	public DataEditor edit()
	{
		if(editor == null)
		{
			editor = new Bundle(bundle);
		}
		
		return this;
	}

	public boolean contains(String key)
	{
		return bundle.containsKey(key);
	}

	public boolean getBoolean(String key, boolean defValue)
	{
		return bundle.getBoolean(key, defValue);
	}

	public float getFloat(String key, float defValue)
	{
		return bundle.getFloat(key, defValue);
	}

	public int getInt(String key, int defValue)
	{
		return bundle.getInt(key, defValue);
	}

	public long getLong(String key, long defValue)
	{
		return bundle.getLong(key, defValue);
	}

	public String getString(String key, String defValue)
	{
		key = bundle.getString(key);
		return key != null ? key : defValue;
	}

	public boolean getBoolean(String key)
	{
		return bundle.getBoolean(key);
	}

	public float getFloat(String key)
	{
		return bundle.getFloat(key);
	}

	public int getInt(String key)
	{
		return bundle.getInt(key);
	}

	public long getLong(String key)
	{
		return bundle.getLong(key);
	}

	public String getString(String key)
	{
		return bundle.getString(key);
	}
	
	public DataEditor clear()
	{
		editor.clear();
		return this;
	}

	public DataEditor remove(String key)
	{
		editor.remove(key);
		return this;
	}

	public DataEditor putBoolean(String key, boolean value)
	{
		editor.putBoolean(key, value);
		return this;
	}

	public DataEditor putFloat(String key, float value)
	{
		editor.putFloat(key, value);
		return this;
	}

	public DataEditor putInt(String key, int value)
	{
		editor.putInt(key, value);
		return this;
	}

	public DataEditor putLong(String key, long value)
	{
		editor.putLong(key, value);
		return this;
	}

	public DataEditor putString(String key, String value)
	{
		editor.putString(key, value);
		return this;
	}

	public void apply()
	{
		commit();
	}

	public boolean commit()
	{
		bundle.clear();
		bundle.putAll(editor);
		
		return true;
	}
	
	public void close()
	{
		editor = null;
	}
}
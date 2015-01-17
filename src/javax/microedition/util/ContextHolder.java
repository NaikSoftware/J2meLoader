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

package javax.microedition.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.microedition.lcdui.MicroActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Process;
import android.view.Display;
import android.view.WindowManager;
import filelog.Log;

public class ContextHolder
{
	private static final String tag = "ContextHolder";
	
	private static Context context;
	private static Display display;
	
	private static MicroActivity currentActivity;
	private static ArrayList<WeakReference<MicroActivity>> activityPool = new ArrayList();
	private static ArrayList<ActivityResultListener> resultListeners = new ArrayList();
	
	public static void setContext(Context cx)
	{
		Log.d("ContextHolder", "setContext old=" + context);
		Log.d("ContextHolder", "setContext new=" + cx);
		context = cx;
	}
	
	public static Context getContext()
	{
		if(context == null)
		{
			throw new IllegalStateException("call setContext() before calling getContext()");
		}
		Log.d("ContextHolder", "getContext=" + context);
		return context;
	}
	
	public static Display getDisplay()
	{
		if(display == null)
		{
			display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		}
		
		return display;
	}
	
	public static int getDisplayWidth()
	{
		return getDisplay().getWidth();
	}
	
	public static int getDisplayHeight()
	{
		return getDisplay().getHeight();
	}
	
	public static WeakReference<MicroActivity> compactActivityPool(MicroActivity activity)
	{
		WeakReference<MicroActivity> reference = null;
		MicroActivity referent;
		
		for(int index = 0; index < activityPool.size();)
		{
			referent = activityPool.get(index).get();
			
			if(referent == null)
			{
				activityPool.remove(index);
			}
			else if(referent == activity)
			{
				reference = activityPool.remove(index);
			}
			else
			{
				index++;
			}
		}
		
		return reference;
	}
	
	public static void addActivityToPool(MicroActivity activity)
	{
		WeakReference<MicroActivity> reference = compactActivityPool(activity);
		
		if(reference == null)
		{
			reference = new WeakReference(activity);
		}
		
		activityPool.add(reference);
	}
	
	public static void setCurrentActivity(MicroActivity activity)
	{
		currentActivity = activity;
	}
	
	public static MicroActivity getCurrentActivity()
	{
		return currentActivity;
	}
	
	public static boolean hasCurrentActivity()
	{
		return currentActivity != null;
	}
	
	public static void addActivityResultListener(ActivityResultListener listener)
	{
		if(!resultListeners.contains(listener))
		{
			resultListeners.add(listener);
		}
	}
	
	public static void removeActivityResultListener(ActivityResultListener listener)
	{
		resultListeners.remove(listener);
	}
	
	public static void notifyOnActivityResult(int requestCode, int resultCode, Intent data)
	{
		for(ActivityResultListener listener : resultListeners)
		{
			listener.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	public static InputStream getResourceAsStream(String filename)
	{
		if(filename.startsWith("/"))
		{
			filename = filename.substring(1);
		}
		
		try {
			return getContext().getAssets().open(filename);
		} catch (IOException e) {
			Log.d(tag, "getResourceAsStream err: " + e.getMessage());
			return null;
		}
	}
	
	public static File getCacheDir()
	{
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			return getContext().getExternalCacheDir();
		}
		else
		{
			return getContext().getCacheDir();
		}
	}
	
	public static int getRequestCode(String requestString)
	{
		return requestString.hashCode() & 0x7FFFFFFF;
	}
	
	/**
	 * Свернуться в фоновый режим.
	 */
	public static void notifyPaused()
	{
		if(currentActivity != null)
		{
			currentActivity.moveTaskToBack(true);
		}
	}
	
	/**
	 * Закрыть все Activity и завершить процесс, в котором они выполнялись.
	 */
	public static void notifyDestroyed()
	{
		MicroActivity activity;
		int index;
		
		while(true)
		{
			index = activityPool.size() - 1;
			
			if(index < 0)
			{
				break;
			}
			
			activity = activityPool.remove(index).get();
			
			if(activity != null && activity != currentActivity)
			{
				activity.finish();
			}
		}
		
		if(currentActivity != null)
		{
			currentActivity.finish();
		}
		
		// Закоментировано чтобы не закрывалось все приложение вместе с j2me-приложением
		//Process.killProcess(Process.myPid());
	}
}

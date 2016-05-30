/*
 * Copyright 2012 Kulikov Dmitriy, Naik
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
import java.util.ArrayList;

import ua.naiksoftware.j2meloader.MainActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.LayoutInflater;
import filelog.Log;

public class ContextHolder {

	private static final String tag = "ContextHolder";

	private static MainActivity context;
	private static ArrayList<ActivityResultListener> resultListeners = new ArrayList<ActivityResultListener>();
	private static LayoutInflater inflater;

	public static void setContext(MainActivity cx) {
		context = cx;
		inflater = LayoutInflater.from(cx);
	}

	public static Context getContext() {
		if (context == null) {
			throw new IllegalStateException(
					"call setContext() before calling getContext()");
		}
		return context;
	}

	public static LayoutInflater getInflater() {
		return inflater;
	}

	public static InputStream getResourceAsStream(String filename) {
		if (filename.startsWith("/")) {
			filename = filename.substring(1);
		}

		try {
			return getContext().getAssets().open(filename);
		} catch (IOException e) {
			Log.d(tag, "getResourceAsStream err: " + e.getMessage());
			return null;
		}
	}

	public static File getCacheDir() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			return getContext().getExternalCacheDir();
		} else {
			return getContext().getCacheDir();
		}
	}

	public static int getRequestCode(String requestString) {
		return requestString.hashCode() & 0x7FFFFFFF;
	}

	public static MainActivity getActivity() {
		if (context == null) {
			throw new IllegalStateException(
					"call setContext() before calling getActivity()");
		}
		return context;
	}

	public static void addActivityResultListener(ActivityResultListener listener) {
		if (!resultListeners.contains(listener)) {
			resultListeners.add(listener);
		}
	}

	public static void removeActivityResultListener(
			ActivityResultListener listener) {
		resultListeners.remove(listener);
	}

	public static void notifyOnActivityResult(int requestCode, int resultCode,
			Intent data) {
		for (ActivityResultListener listener : resultListeners) {
			listener.onActivityResult(requestCode, resultCode, data);
		}
	}
}

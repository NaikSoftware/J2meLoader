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

package javax.microedition.lcdui;

import javax.microedition.lcdui.event.SimpleEvent;
import javax.microedition.midlet.MIDlet;

import ua.naiksoftware.j2meloader.R;

import javax.microedition.util.ContextHolder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MicroActivity extends Activity implements Handler.Callback
{
	private static final int SET_TITLE = 0;
	private static final int SET_CONTENT_VIEW = 1;
	
	private Displayable current;
	private boolean visible;
	
	private Handler handler;
	
	private SimpleEvent msgSetCurent = new SimpleEvent()
	{
		public void process()
		{
			current.setParentActivity(MicroActivity.this);
			
			setTitle(current.getTitle());
			setContentView(current.getDisplayableView());
		}
	};
	
	public void setCurrent(Displayable disp)
	{
		if(current != null)
		{
			current.setParentActivity(null);
		}
		
		current = disp;
		
		if(disp != null)
		{
			runOnUiThread(msgSetCurent);
		}
		else
		{
			ContextHolder.notifyPaused();
		}
	}
	
	public Displayable getCurrent()
	{
		return current;
	}
	
	public void setFullScreenMode()
	{
		Window wnd = getWindow();
		wnd.requestFeature(Window.FEATURE_NO_TITLE);
		wnd.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	public boolean isVisible()
	{
		return visible;
	}
	
	public void startActivity(Class cls)
	{
		startActivity(cls, null);
	}
	
	public void startActivity(Class cls, Bundle bundle)
	{
		Intent intent = new Intent(this, cls);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		
		if(bundle != null)
		{
			intent.putExtras(bundle);
		}
		
		startActivity(intent);
	}
	
	public void restart()
	{
		onPause();
		onStop();
		
		Bundle bundle = new Bundle();
		onSaveInstanceState(bundle);
		
		onDestroy();
		
		onCreate(bundle);
		onStart();
		onResume();
	}
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ContextHolder.addActivityToPool(this);
	}
	
	public void onResume()
	{
		super.onResume();
		
		ContextHolder.setCurrentActivity(this);
		visible = true;
	}
	
	public void onPause()
	{
		ContextHolder.setCurrentActivity(null);
		visible = false;
		
		super.onPause();
	}
	
	public void onDestroy()
	{
		if(current != null)
		{
			current.setParentActivity(null);
		}
		
		ContextHolder.compactActivityPool(this);
		
		super.onDestroy();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			event.startTracking();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled())
		{
			Runnable r = new Runnable()
			{
				public void run()
				{
					// При нажатии хард-кнопки "назад" сворачиваем приложение
					moveTaskToBack(true);
				}
			};
			
			(new Thread(r)).start();
			
			return true;
		}
		
		return super.onKeyUp(keyCode, event);
	}
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			CommandListener cl = new CommandListener()
			{
				public void commandAction(Command c, Displayable dp)
				{
					if(c.getCommandType() == Command.OK)
					{
						Runnable r = new Runnable()
						{
							public void run()
							{
								try
								{
									MIDlet.callDestroyApp(true);
								}
								catch(Throwable ex)
								{
									ex.printStackTrace();
								}
								
								ContextHolder.notifyDestroyed();
							}
						};
						
						(new Thread(r)).start();
					}
				}
			};
			
			Alert alert = new Alert(getString(R.string.CONFIRMATION_REQUIRED),
									getString(R.string.FORCE_CLOSE_CONFIRMATION),
									null, AlertType.CONFIRMATION);
			
			alert.addCommand(new Command(getString(R.string.YES_CMD), Command.OK, 1));
			alert.addCommand(new Command(getString(R.string.NO_CMD), Command.CANCEL, 2));
			alert.setCommandListener(cl);
			
			Display dsp = Display.getDisplay(null);
			dsp.setCurrent(alert, dsp.getCurrent());
			
			return true;
		}
		
		return super.onKeyLongPress(keyCode, event);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		ContextHolder.notifyOnActivityResult(requestCode, resultCode, data);
	}
	
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if(current != null)
		{
			current.populateMenu(menu);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(current != null)
		{
			current.menuItemSelected(item);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public boolean onContextItemSelected(MenuItem item)
	{
		if(current instanceof Form)
		{
			((Form)current).contextMenuItemSelected(item);
		}
		
		return super.onContextItemSelected(item);
	}

	public boolean handleMessage(Message msg)
	{
		switch(msg.what)
		{
			case SET_TITLE:
				super.setTitle((CharSequence)msg.obj);
				return true;
				
			case SET_CONTENT_VIEW:
				super.setContentView((View)msg.obj);
				return true;
		}
		
		return false;
	}
	
	public Handler getHandler()
	{
		if(handler == null)
		{
			handler = new Handler();
		}
		
		return handler;
	}
	
//	public void setTitle(CharSequence title)
//	{
//		getHandler().obtainMessage(SET_TITLE, title).sendToTarget();
//	}
//	
//	public void setContentView(View view)
//	{
//		getHandler().obtainMessage(SET_CONTENT_VIEW, view).sendToTarget();
//	}
	
//	private void out(String text)
//	{
//		System.out.println("[" + getClass().getName() + "] " + text);
//	}
}

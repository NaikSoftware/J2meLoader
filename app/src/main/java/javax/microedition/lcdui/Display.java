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

import java.util.HashMap;

import javax.microedition.lcdui.event.RunnableEvent;
import javax.microedition.midlet.MIDlet;
import javax.microedition.shell.ScreenFragment;
import javax.microedition.util.ContextHolder;

import ua.naiksoftware.j2meloader.MainActivity;
import android.content.Context;
import android.graphics.Rect;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;

public class Display {

	public static final int LIST_ELEMENT = 1;
	public static final int CHOICE_GROUP_ELEMENT = 2;
	public static final int ALERT = 3;

	public static final int COLOR_BACKGROUND = 0;
	public static final int COLOR_FOREGROUND = 1;
	public static final int COLOR_HIGHLIGHTED_BACKGROUND = 2;
	public static final int COLOR_HIGHLIGHTED_FOREGROUND = 3;
	public static final int COLOR_BORDER = 4;
	public static final int COLOR_HIGHLIGHTED_BORDER = 5;

	private static final int[] COLORS = { 0xFFD0D0D0, 0xFF000080, 0xFF000080,
			0xFFFFFFFF, 0xFFFFFFFF, 0xFF000080 };

	private static final int WIDTH, HEIGHT;

	private static FragmentManager fragmentManager;
	private static HashMap<MIDlet, Display> instances = new HashMap<MIDlet, Display>();

	private static PowerManager powermanager;
	private static PowerManager.WakeLock wakelock;
	private static Vibrator vibrator;

	static {
		Rect r = new Rect();
		MainActivity a = ContextHolder.getActivity();
		a.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
		WIDTH = r.right;
		HEIGHT = r.bottom;
	}

	/**
	 * Добавляем новый экран в множество при запуска нового мидлета. Также
	 * настраиваем параметры этого экрана.
	 * 
	 * @param midlet
	 */
	public static void addDisplay(MIDlet midlet) {
		instances.put(midlet, new Display(midlet));
	}

	/**
	 * Удаляем экран из множества при закрытии мидлета
	 * 
	 * @param midlet
	 */
	public static void removeDisplay(MIDlet midlet) {
		instances.remove(midlet);
	}

	public static Display getDisplay(MIDlet midlet) {
		Display d = instances.get(midlet);
		if (d == null) {
			throw new IllegalArgumentException("Display for midlet " + midlet
					+ " not found. Add display previously.");
		} else {
			return d;
		}
	}

	// ---------------------------------------------

	private MIDlet dispMidlet;
	private Displayable current;
	private Overlay overlay;

	private Display(MIDlet midlet) {
		dispMidlet = midlet;
		if (fragmentManager == null) {
			MainActivity a = ContextHolder.getActivity();
			fragmentManager = a.getSupportFragmentManager();
		}
	}

	public void setCurrent(Displayable disp) {
		changeCurrent(disp);
		showCurrent();
	}

	public void setCurrent(Alert alert, Displayable disp) {
		changeCurrent(disp);

		alert.showDialog(ContextHolder.getContext());

		if (alert.finiteTimeout()) {
			alert.setCommandListener(new CommandListener() {

				@Override
				public void commandAction(Command c, Displayable d) {
					showCurrent();
				}
			});
			(new Thread(alert)).start();
		}
	}

	public void setOverlay(Overlay overlay) {
		this.overlay = overlay;
	}

	/**
	 * Изменить текущее представление.
	 * 
	 * @param disp
	 */
	private void changeCurrent(Displayable disp) {
		if (current instanceof Canvas) {
			((Canvas) current).setOverlay(null);
		}

		if (disp instanceof Canvas) {
			((Canvas) disp).setOverlay(overlay);
		}

		current = disp;
	}

	/**
	 * Применить изменение представления.
	 */
	public void showCurrent() {

	}

	/**
	 * Вызывается когда текущий экран переходит в режим паузы.
	 */
	public void pause() {
		/*
		 * ...а спрятали-то наш текущий ScreenFragment! Нужно сообщить об этом
		 * мидлету...
		 */

		dispMidlet.pauseApp();
	}

	public Displayable getCurrent() {
		return current;
	}

	public void callSerially(Runnable r) {
		if (current != null) {
			current.getEventQueue().postEvent(RunnableEvent.getInstance(r));
		} else {
			r.run();
		}
	}

	public static boolean flashBacklight(int duration) {
		try {
			if (powermanager == null) {
				powermanager = (PowerManager) ContextHolder.getContext()
						.getSystemService(Context.POWER_SERVICE);
				wakelock = powermanager.newWakeLock(
						PowerManager.SCREEN_BRIGHT_WAKE_LOCK
								| PowerManager.ACQUIRE_CAUSES_WAKEUP,
						"Display.flashBacklight");
			}

			if (wakelock.isHeld()) {
				wakelock.release();
			}

			if (duration > 0) {
				wakelock.acquire(duration);
			} else if (duration < 0) {
				wakelock.acquire();
			}

			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	public static boolean vibrate(int duration) {
		try {
			if (vibrator == null) {
				vibrator = (Vibrator) ContextHolder.getContext()
						.getSystemService(Context.VIBRATOR_SERVICE);
			}

			vibrator.vibrate(duration);

			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * Проскролить до item, если он есть в текущем displayable.
	 * 
	 * @param item
	 */
	public void setCurrentItem(Item item) {
		if (item.hasOwnerForm()) {
			setCurrent(item.getOwnerForm());
		}
	}

	// --------------------------------------------

	public static int getWidth() {
		return WIDTH;
	}

	public static int getHeight() {
		return HEIGHT;
	}

	public static int getBestImageHeight(int imageType) {
		return 0;
	}

	public static int getBestImageWidth(int imageType) {
		return 0;
	}

	public static int getBorderStyle(boolean highlighted) {
		return highlighted ? Graphics.SOLID : Graphics.DOTTED;
	}

	public static int getColor(int colorSpecifier) {
		return COLORS[colorSpecifier];
	}

	public static void setColor(int colorSpecifier, int color) {
		COLORS[colorSpecifier] = color;
	}

	public static boolean isColor() {
		return true;
	}

	public static int numAlphaLevels() {
		return 255;
	}

	public static int numColors() {
		return Integer.MAX_VALUE;
	}

}

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
package javax.microedition.midlet;

import java.util.Locale;
import java.util.TreeMap;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.util.ContextHolder;

import ua.naiksoftware.j2meloader.AppItem;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

public class MIDlet {

	private static TreeMap<String, String> tempProps;
	private TreeMap<String, String> props;
	private AppItem app;

	/**
	 * Для первоначальной инициализации мидлета, чтобы в конструкторе оболочка
	 * уже была готова работать с этим мидлетом.
	 */
	{
		Display.addDisplay(this);
		Display.getDisplay(this).setCurrent(defaultDisplayable);
	}

	/**
	 * Запуск мидлета
	 * 
	 * @param app
	 */
	public void start(AppItem app) {
		this.app = app;
		ContextHolder.getActivity().setTitle(
				app.getTitle() + " " + app.getVersion());
		startApp();
	}

	/**
	 * Установка временных параметров мидлета из его манифеста. Нужно для их
	 * доступности в конструкторе по умолчанию, который вызывается через
	 * рефлексию. После инициализации мидлета нужно установить эти же параметры
	 * с помощью {@link #setProps(TreeMap)}
	 * 
	 * @param p
	 */
	public static void initTempProps(TreeMap<String, String> p) {
		tempProps = p;
	}

	/**
	 * @see {@link #initTempProps(TreeMap)}
	 * @param p
	 */
	public void setProps(TreeMap<String, String> p) {
		this.props = p;
	}

	public String getAppProperty(String key) {
		if (props == null) {
			return tempProps.get(key);
		} else {
			return props.get(key);
		}
	}

	/**
	 * Вызывается каждый раз, когда мидлет становится активным: при запуске, при
	 * восстановлении из свернутого состояния, ... Не вызывайте напрямую этот
	 * метод! Для запуска мидлета вызывайте {@link #start(AppItem)}
	 */
	public void startApp() {
	}

	/**
	 * Сообщить оболочке, что мидлет готов перейти в состояние паузы. При этом
	 * он будет свернут в фон.
	 *
	 * Вызовы этого метода из pauseApp() игнорируются.
	 */
	public final void notifyPaused() {
		// if (!pauseAppCalled) {
		// ContextHolder.notifyPaused();
		// }
	}

	/**
	 * Сообщить оболочке, что мидлет завершил работу. При этом экран мидлета
	 * будет удален.
	 */
	public final void notifyDestroyed() {
		Display.removeDisplay(this);
	}

	/**
	 * Вызывается каждый раз, когда мидлет становится на паузу: при сворачивании
	 * в фоновый режим, ...
	 */
	public void pauseApp() {
	}

	/**
	 * Вызывается при завершении работы приложения.
	 *
	 * @param unconditional
	 *            флаг безусловного завершения, для Android не имеет особого
	 *            смысла
	 */
	public static void destroyApp(boolean unconditional) {
	}

	public static String getDefaultLocale() {
		return Locale.getDefault().getCountry();
	}

	public boolean platformRequest(String url)
			throws ConnectionNotFoundException {
		try {
			ContextHolder.getContext().startActivity(
					new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		} catch (ActivityNotFoundException e) {
			throw new ConnectionNotFoundException();
		}

		return true;
	}

	public AppItem getAppItem() {
		return app;
	}

	private static Displayable defaultDisplayable = new Displayable() {
		// default implementation
	};

}

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

package javax.microedition.shell;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.pointer.VirtualKeyboard;
import javax.microedition.midlet.MIDlet;
import javax.microedition.param.DataContainer;
import javax.microedition.param.DataEditor;
import javax.microedition.param.SharedPreferencesContainer;
import javax.microedition.util.ContextHolder;

import ua.naiksoftware.j2meloader.AppItem;
import ua.naiksoftware.j2meloader.R;
import ua.naiksoftware.util.FileUtils;
import yuku.ambilwarna.AmbilWarnaDialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import filelog.Log;

public class ConfScreen extends Displayable implements OnClickListener,
		OnKeyListener {

	private Activity context;
	private View view;

	protected EditText tfScreenWidth;
	protected EditText tfScreenHeight;
	protected EditText tfScreenBack;
	protected CheckBox cxScaleToFit;
	protected CheckBox cxKeepAspectRatio;
	protected CheckBox cxFilter;

	protected EditText tfFontSizeSmall;
	protected EditText tfFontSizeMedium;
	protected EditText tfFontSizeLarge;
	protected CheckBox cxFontSizeInSP;

	protected SeekBar sbVKAlpha;
	protected EditText tfVKHideDelay;
	protected EditText tfVKLayoutKeyCode;
	protected EditText tfVKFore;
	protected EditText tfVKBack;
	protected EditText tfVKSelFore;
	protected EditText tfVKSelBack;
	protected EditText tfVKOutline;

	protected ArrayList<Integer> screenWidths;
	protected ArrayList<Integer> screenHeights;
	protected ArrayList<String> screenAdapter;

	protected Handler handler;
	protected Runnable resetMessage;

	private MIDlet runMidlet;
	private MIDlet myMidlet;
	private AppItem app;
	public static final String MIDLET_RES_DIR = "/res/";
	public static final String MIDLET_DEX_FILE = "/converted.dex";
	public static final String MIDLET_CONF_FILE = MIDLET_DEX_FILE + ".conf";

	public ConfScreen(MIDlet m, AppItem app) {
		context = ContextHolder.getActivity();
		this.myMidlet = m;
		this.app = app;
		create();
	}

	public void create() {

		DataContainer params = new SharedPreferencesContainer(
				"RunConfiguration", Context.MODE_WORLD_READABLE, context);

		view = getDisplayableView();

		addCommand(new Command(getString(R.string.START_CMD), Command.OK, 1));
		addCommand(new Command(getString(R.string.RESET_CMD), Command.ITEM, 2));
		addCommand(new Command(getString(R.string.CANCEL_CMD), Command.EXIT, 3));

		tfScreenWidth = (EditText) view.findViewById(R.id.tfScreenWidth);
		tfScreenHeight = (EditText) view.findViewById(R.id.tfScreenHeight);
		tfScreenBack = (EditText) view.findViewById(R.id.tfScreenBack);
		cxScaleToFit = (CheckBox) view.findViewById(R.id.cxScaleToFit);
		cxKeepAspectRatio = (CheckBox) view
				.findViewById(R.id.cxKeepAspectRatio);
		cxFilter = (CheckBox) view.findViewById(R.id.cxFilter);

		tfFontSizeSmall = (EditText) view.findViewById(R.id.tfFontSizeSmall);
		tfFontSizeMedium = (EditText) view.findViewById(R.id.tfFontSizeMedium);
		tfFontSizeLarge = (EditText) view.findViewById(R.id.tfFontSizeLarge);
		cxFontSizeInSP = (CheckBox) view.findViewById(R.id.cxFontSizeInSP);

		sbVKAlpha = (SeekBar) view.findViewById(R.id.sbVKAlpha);
		tfVKHideDelay = (EditText) view.findViewById(R.id.tfVKHideDelay);
		tfVKLayoutKeyCode = (EditText) view
				.findViewById(R.id.tfVKLayoutKeyCode);
		tfVKFore = (EditText) view.findViewById(R.id.tfVKFore);
		tfVKBack = (EditText) view.findViewById(R.id.tfVKBack);
		tfVKSelFore = (EditText) view.findViewById(R.id.tfVKSelFore);
		tfVKSelBack = (EditText) view.findViewById(R.id.tfVKSelBack);
		tfVKOutline = (EditText) view.findViewById(R.id.tfVKOutline);

		screenWidths = new ArrayList<Integer>();
		screenHeights = new ArrayList<Integer>();
		screenAdapter = new ArrayList<String>();

		fillScreenSizePresets(Display.getWidth(), Display.getHeight());

		tfVKLayoutKeyCode.setOnKeyListener(this);

		view.findViewById(R.id.cmdScreenSizePresets).setOnClickListener(this);
		view.findViewById(R.id.cmdScreenBack).setOnClickListener(this);
		view.findViewById(R.id.cmdVKBack).setOnClickListener(this);
		view.findViewById(R.id.cmdVKFore).setOnClickListener(this);
		view.findViewById(R.id.cmdVKSelBack).setOnClickListener(this);
		view.findViewById(R.id.cmdVKSelFore).setOnClickListener(this);
		view.findViewById(R.id.cmdVKOutline).setOnClickListener(this);
		view.findViewById(R.id.btnRun).setOnClickListener(this);
		view.findViewById(R.id.btnReset).setOnClickListener(this);
		view.findViewById(R.id.btnCancel).setOnClickListener(this);

		handler = new Handler();

		resetMessage = new Runnable() {
			public void run() {
				SharedPreferencesContainer params = new SharedPreferencesContainer(
						"RunConfiguration", Context.MODE_WORLD_READABLE,
						context);
				params.edit().clear().commit();
				params.close();

				loadParams(params);
			}
		};

		loadParams(params);

		applyConfiguration();
	}

	private String getString(int resId) {
		return context.getString(resId);
	}

	@Override
	public View getDisplayableView() {
		View v;
		if (view == null) {
			v = ContextHolder.getInflater().inflate(R.layout.config_all, null);
		} else {
			v = view;
		}
		return v;
	}

	public void fillScreenSizePresets(int w, int h) {
		screenWidths.clear();
		screenHeights.clear();
		screenAdapter.clear();

		addScreenSizePreset(128, 128);
		addScreenSizePreset(128, 160);
		addScreenSizePreset(132, 176);
		addScreenSizePreset(176, 220);
		addScreenSizePreset(240, 320);

		int w2 = w / 2;
		int h2 = h / 2;

		if (w > h) {
			addScreenSizePreset(h2 * 3 / 4, h2);
			addScreenSizePreset(h2 * 4 / 3, h2);

			addScreenSizePreset(h * 3 / 4, h);
			addScreenSizePreset(h * 4 / 3, h);
		} else {
			addScreenSizePreset(w2, w2 * 4 / 3);
			addScreenSizePreset(w2, w2 * 3 / 4);

			addScreenSizePreset(w, w * 4 / 3);
			addScreenSizePreset(w, w * 3 / 4);
		}

		addScreenSizePreset(w, h);
	}

	public void addScreenSizePreset(int width, int height) {
		screenWidths.add(width);
		screenHeights.add(height);
		screenAdapter.add(Integer.toString(width) + " x "
				+ Integer.toString(height));
	}

	public void loadParams(DataContainer params) {
		tfScreenWidth.setText(Integer.toString(params
				.getInt("ScreenWidth", 240)));
		tfScreenHeight.setText(Integer.toString(params.getInt("ScreenHeight",
				320)));
		tfScreenBack
				.setText(Integer.toHexString(
						params.getInt("ScreenBackgroundColor", 0xD0D0D0))
						.toUpperCase());
		cxScaleToFit.setChecked(params.getBoolean("ScreenScaleToFit", true));
		cxKeepAspectRatio.setChecked(params.getBoolean("ScreenKeepAspectRatio",
				true));
		cxFilter.setChecked(params.getBoolean("ScreenFilter", true));

		tfFontSizeSmall.setText(Integer.toString(params.getInt("FontSizeSmall",
				18)));
		tfFontSizeMedium.setText(Integer.toString(params.getInt(
				"FontSizeMedium", 22)));
		tfFontSizeLarge.setText(Integer.toString(params.getInt("FontSizeLarge",
				26)));
		cxFontSizeInSP.setChecked(params.getBoolean("FontApplyDimensions",
				false));

		sbVKAlpha.setProgress(params.getInt("VirtualKeyboardAlpha", 64));
		tfVKHideDelay.setText(Integer.toString(params.getInt(
				"VirtualKeyboardDelay", -1)));
		tfVKLayoutKeyCode.setText(Integer.toString(params.getInt(
				"VirtualKeyboardLayoutKeyCode",
				Canvas.convertAndroidKeyCode(KeyEvent.KEYCODE_MENU))));
		tfVKBack.setText(Integer.toHexString(
				params.getInt("VirtualKeyboardColorBackground", 0xD0D0D0))
				.toUpperCase());
		tfVKFore.setText(Integer.toHexString(
				params.getInt("VirtualKeyboardColorForeground", 0x000080))
				.toUpperCase());
		tfVKSelBack.setText(Integer.toHexString(
				params.getInt("VirtualKeyboardColorBackgroundSelected",
						0x000080)).toUpperCase());
		tfVKSelFore.setText(Integer.toHexString(
				params.getInt("VirtualKeyboardColorForegroundSelected",
						0xFFFFFF)).toUpperCase());
		tfVKOutline.setText(Integer.toHexString(
				params.getInt("VirtualKeyboardColorOutline", 0xFFFFFF))
				.toUpperCase());
	}

	public void saveParams() {
		SharedPreferencesContainer params = new SharedPreferencesContainer(
				"RunConfiguration", Context.MODE_WORLD_READABLE, context);

		DataEditor editor = params.edit();

		try {
			editor.putInt("ScreenWidth",
					Integer.parseInt(tfScreenWidth.getText().toString()));
			editor.putInt("ScreenHeight",
					Integer.parseInt(tfScreenHeight.getText().toString()));
			editor.putInt("ScreenBackgroundColor",
					Integer.parseInt(tfScreenBack.getText().toString(), 16));
			editor.putBoolean("ScreenScaleToFit", cxScaleToFit.isChecked());
			editor.putBoolean("ScreenKeepAspectRatio",
					cxKeepAspectRatio.isChecked());
			editor.putBoolean("ScreenFilter", cxFilter.isChecked());

			editor.putInt("FontSizeSmall",
					Integer.parseInt(tfFontSizeSmall.getText().toString()));
			editor.putInt("FontSizeMedium",
					Integer.parseInt(tfFontSizeMedium.getText().toString()));
			editor.putInt("FontSizeLarge",
					Integer.parseInt(tfFontSizeLarge.getText().toString()));
			editor.putBoolean("FontApplyDimensions", cxFontSizeInSP.isChecked());

			editor.putInt("VirtualKeyboardAlpha", sbVKAlpha.getProgress());
			editor.putInt("VirtualKeyboardDelay",
					Integer.parseInt(tfVKHideDelay.getText().toString()));
			editor.putInt("VirtualKeyboardLayoutKeyCode",
					Integer.parseInt(tfVKLayoutKeyCode.getText().toString()));
			editor.putInt("VirtualKeyboardColorBackground",
					Integer.parseInt(tfVKBack.getText().toString(), 16));
			editor.putInt("VirtualKeyboardColorForeground",
					Integer.parseInt(tfVKFore.getText().toString(), 16));
			editor.putInt("VirtualKeyboardColorBackgroundSelected",
					Integer.parseInt(tfVKSelBack.getText().toString(), 16));
			editor.putInt("VirtualKeyboardColorForegroundSelected",
					Integer.parseInt(tfVKSelFore.getText().toString(), 16));
			editor.putInt("VirtualKeyboardColorOutline",
					Integer.parseInt(tfVKOutline.getText().toString(), 16));

			editor.apply();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		params.close();
	}

	/**
	 * Настройка конфигурации перед запуском мидлета. Нужно вызывать каждый раз
	 * при запуске (перезапуске) мидлета, т.к. конфигурация статична (одна для
	 * всех одновременно запущеных мидлетов). Индивидуальные настройки каждого
	 * мидлета хранятся прямо в этом классе. Поэтому нужно хранить ссылку на
	 * него, и при событии start/resume вызывать этот метод.
	 */
	public void applyConfiguration() {
		try {
			int fontSizeSmall = Integer.parseInt(tfFontSizeSmall.getText()
					.toString());
			int fontSizeMedium = Integer.parseInt(tfFontSizeMedium.getText()
					.toString());
			int fontSizeLarge = Integer.parseInt(tfFontSizeLarge.getText()
					.toString());
			boolean fontApplyDimensions = cxFontSizeInSP.isChecked();

			int screenWidth = Integer.parseInt(tfScreenWidth.getText()
					.toString());
			int screenHeight = Integer.parseInt(tfScreenHeight.getText()
					.toString());
			int screenBackgroundColor = Integer.parseInt(tfScreenBack.getText()
					.toString(), 16);
			boolean screenScaleToFit = cxScaleToFit.isChecked();
			boolean screenKeepAspectRatio = cxKeepAspectRatio.isChecked();
			boolean screenFilter = cxFilter.isChecked();

			Font.setSize(Font.SIZE_SMALL, fontSizeSmall);
			Font.setSize(Font.SIZE_MEDIUM, fontSizeMedium);
			Font.setSize(Font.SIZE_LARGE, fontSizeLarge);
			Font.setApplyDimensions(fontApplyDimensions);

			Canvas.setVirtualSize(screenWidth, screenHeight, screenScaleToFit,
					screenKeepAspectRatio);
			Canvas.setFilterBitmap(screenFilter);
			Canvas.setBackgroundColor(screenBackgroundColor);

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Установку виртуальной клавиатуры можно выполнить всего однин раз для
	 * каждого мидлета (т.к. создается отдельный Display).
	 * 
	 * @param midlet
	 */
	private void setVirtualKeyboard(MIDlet midlet) {
		int vkAlpha = sbVKAlpha.getProgress();
		int vkDelay = Integer.parseInt(tfVKHideDelay.getText().toString());
		int vkLayoutKeyCode = Integer.parseInt(tfVKLayoutKeyCode.getText()
				.toString());
		int vkColorBackground = Integer.parseInt(tfVKBack.getText().toString(),
				16);
		int vkColorForeground = Integer.parseInt(tfVKFore.getText().toString(),
				16);
		int vkColorBackgroundSelected = Integer.parseInt(tfVKSelBack.getText()
				.toString(), 16);
		int vkColorForegroundSelected = Integer.parseInt(tfVKSelFore.getText()
				.toString(), 16);
		int vkColorOutline = Integer.parseInt(tfVKOutline.getText().toString(),
				16);

		VirtualKeyboard vk = new VirtualKeyboard();

		vk.setOverlayAlpha(vkAlpha);
		vk.setHideDelay(vkDelay);
		vk.setLayoutEditKey(vkLayoutKeyCode);

		try {
			DataInputStream dis = new DataInputStream(ContextHolder
					.getContext().openFileInput("VirtualKeyboardLayout"));
			vk.readLayout(dis);
			dis.close();
		} catch (FileNotFoundException fnfe) {
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		vk.setColor(VirtualKeyboard.BACKGROUND, vkColorBackground);
		vk.setColor(VirtualKeyboard.FOREGROUND, vkColorForeground);
		vk.setColor(VirtualKeyboard.BACKGROUND_SELECTED,
				vkColorBackgroundSelected);
		vk.setColor(VirtualKeyboard.FOREGROUND_SELECTED,
				vkColorForegroundSelected);
		vk.setColor(VirtualKeyboard.OUTLINE, vkColorOutline);

		VirtualKeyboard.LayoutListener listener = new VirtualKeyboard.LayoutListener() {
			public void layoutChanged(VirtualKeyboard vk) {
				try {
					DataOutputStream dos = new DataOutputStream(ContextHolder
							.getContext().openFileOutput(
									"VirtualKeyboardLayout",
									Context.MODE_WORLD_READABLE));
					vk.writeLayout(dos);
					dos.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		};
		vk.setLayoutListener(listener);
		Display.getDisplay(midlet).setOverlay(vk);
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (v == tfVKLayoutKeyCode
				&& (event.getFlags() & KeyEvent.FLAG_SOFT_KEYBOARD) == 0) {
			tfVKLayoutKeyCode.setText(Integer.toString(Canvas
					.convertAndroidKeyCode(keyCode)));
			return true;
		}

		return false;
	}

	@Override
	public void onClick(View v) {
		String[] presets = null;
		DialogInterface.OnClickListener presetListener = null;

		int color = 0;
		AmbilWarnaDialog.OnAmbilWarnaListener colorListener = null;

		int id = v.getId();

		if (id == R.id.cmdScreenSizePresets) {
			presets = screenAdapter.toArray(new String[0]);

			presetListener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					tfScreenWidth.setText(Integer.toString(screenWidths
							.get(which)));
					tfScreenHeight.setText(Integer.toString(screenHeights
							.get(which)));
				}
			};
		} else if (id == R.id.cmdScreenBack) {
			color = Integer.parseInt(tfScreenBack.getText().toString(), 16);

			colorListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
				public void onOk(AmbilWarnaDialog dialog, int color) {
					tfScreenBack.setText(Integer.toHexString(color & 0xFFFFFF)
							.toUpperCase());
				}

				public void onCancel(AmbilWarnaDialog dialog) {
				}
			};
		} else if (id == R.id.cmdVKBack) {
			color = Integer.parseInt(tfVKBack.getText().toString(), 16);

			colorListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
				public void onOk(AmbilWarnaDialog dialog, int color) {
					tfVKBack.setText(Integer.toHexString(color & 0xFFFFFF)
							.toUpperCase());
				}

				public void onCancel(AmbilWarnaDialog dialog) {
				}
			};
		} else if (id == R.id.cmdVKFore) {
			color = Integer.parseInt(tfVKFore.getText().toString(), 16);

			colorListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
				public void onOk(AmbilWarnaDialog dialog, int color) {
					tfVKFore.setText(Integer.toHexString(color & 0xFFFFFF)
							.toUpperCase());
				}

				public void onCancel(AmbilWarnaDialog dialog) {
				}
			};
		} else if (id == R.id.cmdVKSelFore) {
			color = Integer.parseInt(tfVKSelFore.getText().toString(), 16);

			colorListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
				public void onOk(AmbilWarnaDialog dialog, int color) {
					tfVKSelFore.setText(Integer.toHexString(color & 0xFFFFFF)
							.toUpperCase());
				}

				public void onCancel(AmbilWarnaDialog dialog) {
				}
			};
		} else if (id == R.id.cmdVKSelBack) {
			color = Integer.parseInt(tfVKSelBack.getText().toString(), 16);

			colorListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
				public void onOk(AmbilWarnaDialog dialog, int color) {
					tfVKSelBack.setText(Integer.toHexString(color & 0xFFFFFF)
							.toUpperCase());
				}

				public void onCancel(AmbilWarnaDialog dialog) {
				}
			};
		} else if (id == R.id.cmdVKOutline) {
			color = Integer.parseInt(tfVKOutline.getText().toString(), 16);

			colorListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
				public void onOk(AmbilWarnaDialog dialog, int color) {
					tfVKOutline.setText(Integer.toHexString(color & 0xFFFFFF)
							.toUpperCase());
				}

				public void onCancel(AmbilWarnaDialog dialog) {
				}
			};
		} else if (id == R.id.btnRun) {// Запуск мидлета
			try {
				runMidlet = loadMIDlet();
				// Теперь применяем конфигурацию к запускаемому мидлету.
				applyConfiguration();
				setVirtualKeyboard(runMidlet);

				runMidlet.start(app);

			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else if (id == R.id.btnReset) {// Сброс параметров запуска
			handler.post(resetMessage);
		} else if (id == R.id.btnCancel) {// Закрытие мидлета настройки
			myMidlet.notifyDestroyed();
		} else {
			return;
		}

		if (presetListener != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(getString(R.string.SIZE_PRESETS));
			builder.setItems(presets, presetListener);

			AlertDialog alert = builder.create();
			alert.show();
		} else if (colorListener != null) {
			AmbilWarnaDialog dialog = new AmbilWarnaDialog(context,
					color | 0xFF000000, colorListener);
			dialog.show();
		}
	}

	private MIDlet loadMIDlet() {

		MIDlet midlet = null;
		String appDir = app.getPath();
		TreeMap<String, String> p = FileUtils.loadManifest(new File(appDir
				+ MIDLET_CONF_FILE));
		MIDlet.initTempProps(p); // для доступности в конструкторе
		String dex = appDir + ConfScreen.MIDLET_DEX_FILE;
		ClassLoader loader = new MyClassLoader(dex,
				context.getApplicationInfo().dataDir, null,
				context.getClassLoader(), appDir + MIDLET_RES_DIR);
		try {
			String mainClassParam = p.get("MIDlet-1");
			String mainClass = mainClassParam.substring(
					mainClassParam.lastIndexOf(',') + 1).trim();
			Log.d("inf", "load main: " + mainClass + " from dex:" + dex);
			// Тут вызывается конструктор по умолчанию.
			midlet = (MIDlet) loader.loadClass(mainClass).newInstance();
			midlet.setProps(p);
		} catch (ClassNotFoundException ex) {
			Log.d("err", ex.toString() + "/n" + ex.getMessage());
		} catch (InstantiationException ex) {
			Log.d("err", ex.toString() + "/n" + ex.getMessage());
		} catch (IllegalAccessException ex) {
			Log.d("err", ex.toString() + "/n" + ex.getMessage());
		}
		return midlet;
	}

	@Override
	public void clearDisplayableView() {
		view = null;
	}
	
	public String getAppDir() {
		return app.getPath();
	}

}

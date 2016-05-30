package ua.naiksoftware.j2meloader;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.shell.ConfScreen;
import javax.microedition.util.ContextHolder;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

/**
 * 
 * @author Naik
 */
public class AppsListFragment extends ListFragment {

	private static ArrayList<ConfScreen> confScreens = new ArrayList<ConfScreen>();
	private static ConfScreen currConfScreen;

	public static ConfScreen getCurrConf() {
		return currConfScreen;
	}

	private List<AppItem> apps;

	public AppsListFragment() {
	}

	public AppsListFragment(List<AppItem> apps) {
		this.apps = apps;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ContextHolder.setContext((MainActivity) getActivity());
		MIDlet confMidlet = new ConfMidlet();
		confMidlet.start(apps.get(position));
	}

	private static class ConfMidlet extends MIDlet {

		private ConfScreen confScreen;
		private boolean paused;

		@Override
		public void startApp() {
			Display d = Display.getDisplay(this);
			if (paused) {
				d.setCurrent(confScreen);
			} else {
				confScreen = new ConfScreen(this, getAppItem());
				confScreens.add(confScreen);
				currConfScreen = confScreen;
				d.setCurrent(confScreen);
			}
		}

		@Override
		public void pauseApp() {
			paused = true;
			confScreen.saveParams();
		}
	}

}

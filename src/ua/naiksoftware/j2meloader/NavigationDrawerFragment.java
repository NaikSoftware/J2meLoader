package ua.naiksoftware.j2meloader;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import android.widget.TextView;
import ua.naiksoftware.util.ProcessUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import ua.naiksoftware.util.MathUtils;
import filelog.Log;

/**
 * Fragment used for managing interactions for and presentation of a navigation
 * drawer. See the <a href=
 * "https://developer.android.com/design/patterns/navigation-drawer.html#Interaction"
 * > design guidelines</a> for a complete explanation of the behaviors
 * implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

	private static final String tag = NavigationDrawerFragment.class.getName();

	/**
	 * Remember the current path.
	 */
	private static final String STATE_SELECTED_PATH = "selected_navigation_drawer_path";

	/**
	 * Per the design guidelines, you should show the drawer on launch until the
	 * user manually expands it. This shared preference tracks this.
	 */
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

	/**
	 * Helper component that ties the action bar to the navigation drawer.
	 */
	private ActionBarDrawerToggle mDrawerToggle;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private View mFragmentContainerView;

	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;

	public String selectExts;

	private TextView fullPath;
	ArrayList<FSItem> items;
	private String currPath, prevPath;
	private static final Comparator<SortItem> comparator = new AlphabeticComparator<SortItem>();
	private static final Map<String, Integer> mapExt = new HashMap<String, Integer>() {
		{
			put(".zip", R.drawable.icon_zip);
			put(".jar", R.drawable.icon_jar);
			put(".java", R.drawable.icon_java);
		}
	};

	public NavigationDrawerFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Read in the flag indicating whether or not the user has demonstrated
		// awareness of the
		// drawer. See PREF_USER_LEARNED_DRAWER for details.
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

		if (savedInstanceState != null) {
			currPath = savedInstanceState.getString(STATE_SELECTED_PATH);
			mFromSavedInstanceState = true;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of
		// actions in the action bar.
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.file_browser_layout, container,
				false);
		fullPath = (TextView) v.findViewById(R.id.full_path);
		mDrawerListView = (ListView) v.findViewById(R.id.file_list);
		mDrawerListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						prevPath = currPath;
						FSItem it = items.get(position);
						switch (it.getType()) {
						case Folder:
							currPath = currPath + "/" + it.getName();// build
																		// URL
							readFolder(currPath);
							break;
						case Back:
							currPath = calcBackPath();
							readFolder(currPath);
							break;
						case File:
							selectFile(currPath + '/' + it.getName());// build
																		// URL
							break;
						}
					}
				});
		return v;
	}

	public boolean isDrawerOpen() {
		return mDrawerLayout != null
				&& mDrawerLayout.isDrawerOpen(mFragmentContainerView);
	}

	/**
	 * Users of this fragment must call this method to set up the navigation
	 * drawer interactions.
	 *
	 * @param fragmentId
	 *            The android:id of this fragment in its activity's layout.
	 * @param drawerLayout
	 *            The DrawerLayout containing this fragment's UI.
	 */
	public void setUp(int fragmentId, DrawerLayout drawerLayout,
			String startPath, String exts) {
		if (currPath == null) {
			this.currPath = startPath;
		}
		this.selectExts = exts;
		prevPath = calcBackPath();
		readFolder(currPath);

		mFragmentContainerView = getActivity().findViewById(fragmentId);
		mDrawerLayout = drawerLayout;

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the navigation drawer and the action bar app icon.
		mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.navigation_drawer_open, /*
										 * "open drawer" description for
										 * accessibility
										 */
		R.string.navigation_drawer_close /*
										 * "close drawer" description for
										 * accessibility
										 */
		) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) {
					return;
				}

				getActivity().supportInvalidateOptionsMenu(); // calls
				// onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}

				if (!mUserLearnedDrawer) {
					// The user manually opened the drawer; store this flag to
					// prevent auto-showing
					// the navigation drawer automatically in the future.
					mUserLearnedDrawer = true;
					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true)
							.apply();
				}

				getActivity().supportInvalidateOptionsMenu(); // calls
				// onPrepareOptionsMenu()
			}
		};

		// If the user hasn't 'learned' about the drawer, open it to introduce
		// them to the drawer,
		// per the navigation drawer design guidelines.
		if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
			mDrawerLayout.openDrawer(mFragmentContainerView);
		}

		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_SELECTED_PATH, currPath);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// If the drawer is open, show the global app actions in the action bar.
		// See also
		// showGlobalContextActionBar, which controls the top-left area of the
		// action bar.
		if (mDrawerLayout != null && isDrawerOpen()) {
			inflater.inflate(R.menu.global, menu);
			showGlobalContextActionBar();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		if (item.getItemId() == R.id.action_exit_app) {
			Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT)
					.show();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Per the navigation drawer design guidelines, updates the action bar to
	 * show the global app 'context', rather than just what's in the current
	 * screen.
	 */
	private void showGlobalContextActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setTitle(R.string.open_jar);
	}

	private ActionBar getActionBar() {
		return ((ActionBarActivity) getActivity()).getSupportActionBar();
	}

	/**
	 * Callbacks interface that all activities using this fragment must
	 * implement.
	 */
	public static interface SelectedCallback {
		/**
		 * Called when clicked on file
		 */
		void onSelected(String path);
	}

	private void readFolder(String folderStr) {
		Log.d(tag, "read : " + folderStr);
		String[] lsOutputDet;// Детальная информация
		String[] names;
		String error;
		try {
			java.lang.Process proc = new ProcessBuilder().command("ls", "-l",
					"-a", folderStr + "/").start();
			lsOutputDet = ProcessUtils.readFromProcess(proc, false).split("\n");
			error = ProcessUtils.readFromProcess(proc, true);
			names = ProcessUtils.readFromProcess(
					new ProcessBuilder().command("ls", "-a", folderStr + "/")
							.start(), false).split("\n");
			if (!error.isEmpty()) {
				/*
				 * Комманда ls ничего не вернула. Папка не существует
				 */
				currPath = prevPath;// ?? fix
				Log.d(tag, "Command ls return error " + error);
				return;
			}
		} catch (IOException e) {
			Log.d(tag, "read ls" + e.getLocalizedMessage());
			return;
		}
		items = new ArrayList<FSItem>();
		ArrayList<FSItem> listFolder = new ArrayList<FSItem>();
		ArrayList<FSItem> listFile = new ArrayList<FSItem>();
		StringBuilder subheader = new StringBuilder();
		if (!currPath.equals("")) {
			items.add(new FSItem(R.drawable.folder_in, "..", "Parent folder",
					FSItem.Type.Back));
		}
		if (names[0].equals("")) {// если папка пустая
			mDrawerListView.setAdapter(new FileListAdapter(getActionBar()
					.getThemedContext(), items));
			fullPath.setText(currPath);
			return;
		}
		int j = 0;// счетчик для names
		for (String str : lsOutputDet) {
			String arr[] = str.split("\\s+");
			char id = arr[0].charAt(0);
			if (id != '-' && id != 'd' && id != 'l') {
				/*
				 * Если не файл, не папка, не ссылка,а какая-то фигня, то от
				 * греха подальше, пропускаем
				 */
				// L.write(tag, id + " not known");
				continue;
			}
			subheader.delete(0, subheader.length()).append(' ');// cls subheader
			subheader.append(arr[0].substring(1)).append(' ');// add permissions
			// to subheader
			if (id == 'd' || id == 'l') {// если папка или ссылка
				subheader.append(arr[3]).append(' ').append(arr[4]);// date
				// folder
				listFolder.add(new FSItem(R.drawable.folder, names[j],
						subheader.toString(), FSItem.Type.Folder));
			} else {// если файл
				subheader.append(arr[4]).append(' ').append(arr[5]);// date file
				subheader.append(' ').append(calcSize(Long.parseLong(arr[3])));
				String ext = getExtension(names[j]);// get extension from name
				int iconId = R.drawable.file;
				if (mapExt.containsKey(ext)) {
					iconId = mapExt.get(ext);
				}
				listFile.add(new FSItem(iconId, names[j], subheader.toString(),
						FSItem.Type.File));
			}
			j++;
		}
		Collections.sort(listFolder, comparator);
		Collections.sort(listFile, comparator);
		items.addAll(listFolder.subList(0, listFolder.size()));
		items.addAll(listFile.subList(0, listFile.size()));
		mDrawerListView.setAdapter(new FileListAdapter(getActionBar()
				.getThemedContext(), items));
		fullPath.setText(currPath);
	}

	/*
	 * calc file size in b, Kb or Mb
	 */
	private String calcSize(long length) {
		if (length < 1024) {
			return String.valueOf(length).concat(" b");
		} else if (length < 1048576) {
			return String.valueOf(MathUtils.round((float) length / 1024f))
					.concat(" Kb");
		} else {
			return String.valueOf(MathUtils.round((float) length / 1048576f))
					.concat(" Mb");
		}
	}

	private String calcBackPath() {
		try {
			return currPath.substring(0, currPath.lastIndexOf('/'));
		} catch (IndexOutOfBoundsException ex) {
			return "";
		}
	}

	private void selectFile(String path) {
		String ext = getExtension(path);
		if (ext != null && ext.equals(".jar")) {
			if (mDrawerLayout != null) {
				mDrawerLayout.closeDrawer(mFragmentContainerView);
				getActionBar().setTitle(R.string.app_name);
			}
			((SelectedCallback) getActivity()).onSelected(path);
		}
	}

	private static String getExtension(String path) {
		if (path.contains(".")) {
			return path.substring(path.lastIndexOf(".")).toLowerCase();
		}
		return null;
	}
}

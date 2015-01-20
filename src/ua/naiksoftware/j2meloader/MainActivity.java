package ua.naiksoftware.j2meloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.microedition.shell.ConfigActivity;

import ua.naiksoftware.util.FileUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.os.Environment;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.SelectedCallback {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	private String startPath;
	private static final String EXTS = ".jar.zip.java";
	private AppsListFragment appsListFragment;
	private final List<AppItem> apps = new ArrayList<AppItem>();

	/** путь к папке со сконвертированными приложениями */
	private String pathConverted;

	private JarConverter converter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		startPath = Environment.getExternalStorageDirectory().getAbsolutePath();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout), startPath,
				EXTS);
		pathConverted = getApplicationInfo().dataDir + "/converted/";
		appsListFragment = new AppsListFragment(apps);
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container, appsListFragment).commit();
		updateApps();
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSelected(String path) {
		converter = new JarConverter(this);
		converter.execute(path, pathConverted);
	}

	public void updateApps() {
		apps.clear();
		AppItem item;
		String author = getString(R.string.author);
		String version = getString(R.string.version);
		String[] appFolders = new File(pathConverted).list();
		if (!(appFolders == null)) {
			for (String appFolder : appFolders) {
				TreeMap<String, String> params = FileUtils
						.loadManifest(new File(pathConverted + appFolder
								+ ConfigActivity.MIDLET_CONF_FILE));
				item = new AppItem(R.drawable.app_default_icon,
						params.get("MIDlet-Name"), 
						author + params.get("MIDlet-Vendor"),
						version + params.get("MIDlet-Version"));
				item.setPath(pathConverted + appFolder);
				apps.add(item);
			}
		}
		AppsListAdapter adapter = new AppsListAdapter(this, apps);
		appsListFragment.setListAdapter(adapter);
	}

}

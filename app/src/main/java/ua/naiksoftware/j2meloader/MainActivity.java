package ua.naiksoftware.j2meloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.microedition.shell.ConfScreen;
import javax.microedition.util.ContextHolder;

import ua.naiksoftware.util.FileUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.os.Environment;
import ua.naiksoftware.j2meloader.converter.*;
import android.support.v7.app.*;

public class MainActivity extends AppCompatActivity {

	private String startPath;
	private static final String EXTS = ".jar.zip.java";
	private AppsListFragment appsListFragment;
	private final List<AppItem> apps = new ArrayList<AppItem>();

	/** Путь к папке со сконвертированными приложениями */
	private String pathConverted;

	private JarConverter converter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		startPath = Environment.getExternalStorageDirectory().getAbsolutePath();

		pathConverted = getApplicationInfo().dataDir + "/converted/";
		appsListFragment = new AppsListFragment(apps);
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container, appsListFragment).commit();
		updateApps();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
								+ ConfScreen.MIDLET_CONF_FILE));
				item = new AppItem(R.drawable.app_default_icon,
						params.get("MIDlet-Name"), author
								+ params.get("MIDlet-Vendor"), version
								+ params.get("MIDlet-Version"));
				item.setPath(pathConverted + appFolder);
				apps.add(item);
			}
		}
		AppsListAdapter adapter = new AppsListAdapter(this, apps);
		appsListFragment.setListAdapter(adapter);
	}
}

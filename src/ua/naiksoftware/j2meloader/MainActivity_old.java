package ua.naiksoftware.j2meloader;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import dalvik.system.*;
import filelog.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.microedition.shell.*;
import ua.naiksoftware.util.*;

import java.lang.reflect.Proxy;

/**
 * @author Naik
 */
public class MainActivity_old extends Activity implements ListView.OnItemClickListener {

    private static final String tag = "MainActivity";

    private ListView listView;
	private ArrayList<AppItem> jars = new ArrayList<AppItem>();
    private String pathConverted;
    private TreeMap<String, String> params = new TreeMap<String, String>();
    
    //public static final String KEY_CONF = "conf";

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        listView = (ListView) findViewById(R.id.appsListView);
        pathConverted = getApplicationInfo().dataDir + "/converted/";
        new File(pathConverted).mkdir();
		updateApps();
		listView.setOnItemClickListener(this);
    }
	
    @Override
	public void onItemClick(AdapterView<?> p1, View p2, int sel, long p4) {
		AppItem item = jars.get(sel);
		Log.d(tag, "onItemClick: load=" + item.getPath());
		Intent i = new Intent(Intent.ACTION_DEFAULT, Uri.parse(item.getPath()), this, ConfigActivity.class);
		startActivity(i);
	}
	
	public void updateApps() {
		jars.clear();
		AppItem item;
		String[] appFolders = new File(pathConverted).list();
		for (String appFolder: appFolders) {
			params = FileUtils.loadManifest(new File(pathConverted + appFolder + ConfigActivity.MIDLET_DEX_FILE + ".conf"));
			item = new AppItem(R.drawable.app_default_icon, params.get("MIDlet-Name"));
			item.setPath(pathConverted + appFolder);
			jars.add(item);
		}
		listView.setAdapter(new AppsListAdapter(this, jars));
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String pathToJar = data.getDataString();
			Log.d(tag, "onActivityResult: " + pathToJar);
            //new JarConverter(this).execute(pathToJar, pathConverted);
        }
    }

    public void openJar(View v) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        //i.setClass(this, FileChooser.class);
        //i.putExtra(FileChooser.KEY_START_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
        //i.putExtra(FileChooser.KEY_SELECT_EXTENSIONS, ".jar");
        //startActivityForResult(i, 0);
    }

    // Пока не используется.
    public Object callMethod(String pathToApk, String absClassName, String methodName, Object[] params) {
        DexClassLoader dLoader = new DexClassLoader(pathToApk, getApplicationContext().getApplicationInfo().dataDir, null, ClassLoader.getSystemClassLoader().getParent());
        Class c = null;
        //try {
        //    c = dLoader.loadClass(absClassName);
        //} catch (ClassNotFoundException e) {
        //    e.printStackTrace();
        //}
        try {
            int len = params.length;
            Class[] cc = new Class[len];
            for (int i = 0; i < len; i++) {
                cc[i] = params[i].getClass();
            }
            Method method = c.getDeclaredMethod(methodName, cc);
            if (len < 1) {
                return method.invoke(c.newInstance());
            } else {
                return method.invoke(c.newInstance(), params);
            }
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (InstantiationException e) {
        }
        return null;
    }
}

package ua.naiksoftware.j2meloader.converter;

import java.io.*;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.android.dx.command.Main;
import filelog.Log;
import javax.microedition.shell.ConfScreen;
import ua.naiksoftware.util.FileUtils;
import rx.*;

public class JarConverter {

	private static final String TAG = "JarConverter";

	private final File dirTmp, dirForJAssist;

	private static final String classRepl = "java.lang.Class";
	private static final String methodRepl = "getResourceAsStream";

	public JarConverter(Context context) {
		dirTmp = new File(context.getApplicationInfo().dataDir + "/tmp");
		dirForJAssist = new File(dirTmp, "/_converted_classes");
	}

	public Observable<Void> convert(final String pathToJar, final String pathConverted) {
		Log.d(TAG, "doInBackground$ pathToJar=" + pathToJar + " pathConverted=" + pathConverted);
		return Observable.<Void>create(new Observable.OnSubscribe<Void>() {

				@Override
				public void call(Subscriber<? super Void> subscriber) {
					FileUtils.deleteDirectory(dirTmp);
					dirTmp.mkdir();
					try {
						if (!FileUtils.unzip(new FileInputStream(new File(pathToJar)), dirTmp)) {
							subscriber.onError(new ConvertException("Brocken jar"));
							return;
						}
					} catch (FileNotFoundException e) {
						subscriber.onError(e);
						return;
					}

					String appDir = FileUtils.loadManifest(
						new File(dirTmp, "/META-INF/MANIFEST.MF")).get("MIDlet-Name");
					File appConverted = new File(pathConverted + appDir);
					FileUtils.deleteDirectory(appConverted);
					appConverted.mkdirs();
					// dirForJAssist.mkdir();
					// workModifyClass(dirTmp.getPath()); // MODIFY
					// Log.d(tag, "-------\n\nreplace OK");
					// Convert to dex
					Log.d(TAG, "appConverted=" + appConverted.getPath());
					Main.main(new String[] {
								  "--dex",
								  "--output=" + appConverted.getPath()
								  + ConfScreen.MIDLET_DEX_FILE,
								  /* dirForJAssist.getPath() */pathToJar });
					File conf = new File(dirTmp, "/META-INF/MANIFEST.MF");
					if (!conf.exists()) {
						subscriber.onError(new ConvertException("Manifest not exists: " + conf.getPath()));
						return;
					}
					conf.renameTo(new File(appConverted, ConfScreen.MIDLET_CONF_FILE));
					// Extract other resources from jar.
					FileUtils.moveFiles(dirTmp.getPath(), pathConverted + appDir
						+ ConfScreen.MIDLET_RES_DIR, new FilenameFilter() {
							public boolean accept(File dir, String fname) {
								if (fname.equalsIgnoreCase("MANIFEST.MF")
									|| fname.endsWith(".class")) {
									return false;
								} else {
									return true;
								}
							}
						});
					FileUtils.deleteDirectory(dirTmp);
					subscriber.onCompleted();
				}
		});
	}
}

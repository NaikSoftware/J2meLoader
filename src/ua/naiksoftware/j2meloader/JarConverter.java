package ua.naiksoftware.j2meloader;

import java.io.*;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.android.dx.command.Main;
import filelog.Log;
import javax.microedition.shell.ConfigActivity;
import ua.naiksoftware.util.FileUtils;

public class JarConverter extends AsyncTask<String, String, Boolean> {

	private static final String tag = "JarConverter";

	private final Context context;
	private String err = "Void error";
	private ProgressDialog dialog;

	private String pathToJar, appDir, pathConverted;
	private final File dirTmp, dirForJAssist;

	private static final String classRepl = "java.lang.Class";
	private static final String methodRepl = "getResourceAsStream";

	public JarConverter(MainActivity context) {
		this.context = context;
		dirTmp = new File(context.getApplicationInfo().dataDir + "/tmp");
		dirForJAssist = new File(dirTmp, "/_converted_classes");
	}

	@Override
	protected Boolean doInBackground(String... p1) {
		pathToJar = p1[0];
		pathConverted = p1[1];
		Log.d(tag, "doInBackground$ pathToJar=" + pathToJar + " pathConverted="
				+ pathConverted);
		FileUtils.deleteDirectory(dirTmp);
		dirTmp.mkdir();
		try {
			if (!FileUtils.unzip(new FileInputStream(new File(pathToJar)),
					dirTmp)) {
				err = "Brocken jar";
				return false;
			}
		} catch (FileNotFoundException e) {
			err = e.getMessage();
			return false;
		}

		appDir = FileUtils.loadManifest(
				new File(dirTmp, "/META-INF/MANIFEST.MF")).get("MIDlet-Name");
		File appConverted = new File(pathConverted + appDir);
		FileUtils.deleteDirectory(appConverted);
		appConverted.mkdirs();
		dirForJAssist.mkdir();
		//workJavassist(dirTmp.getPath());
		Log.d(tag, "replace OK");
		// Convert to dex
		Log.d(tag, "appConverted=" + appConverted.getPath());
		Main.main(new String[] {
				"--dex",
				"--output=" + appConverted.getPath()
						+ ConfigActivity.MIDLET_DEX_FILE,
				/*dirForJAssist.getPath()*/ pathToJar});
		File conf = new File(dirTmp, "/META-INF/MANIFEST.MF");
		if (!conf.exists()) {
			err = "Manifest not exists: " + conf.getPath();
			return false;
		}
		conf.renameTo(new File(appConverted, ConfigActivity.MIDLET_DEX_FILE
				+ ".conf"));
		// Extract other resources from jar.
		FileUtils.moveFiles(dirTmp.getPath(), pathConverted + appDir
				+ ConfigActivity.MIDLET_RES_DIR, new FilenameFilter() {
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
		return true;
	}

	private void workJavassist(final String path) {
		Log.d(tag, "WORK_JAVAASSIST");
		Log.d(tag, "workJavassist(" + path + ")");
		File dir = new File(path);
		dir.mkdir();
		File[] classes = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String fname) {
				// Log.d(tag, "accept " + dir + "/" + fname);
				if (fname.endsWith(".class")) {
					return true;
				} else if (new File(dir, fname).isDirectory()) {
					// Log.d(tag, "accept recursive call");
					if (!dir.getPath().contains("_converted_classes")) {
						workJavassist(path + "/" + fname);
					}
					return false;
				} else {
					return false;
				}
			}
		});
		// Javassist works
		/*
		 * ClassPool cp = ClassPool.getDefault();
		 * cp.makeClass("java.lang.String");
		 * cp.makeClass("java.io.InputStream"); try { cp.appendSystemPath();
		 * cp.importPackage("javax.microedition.util.ContextHolder");
		 * cp.makeClass(new FileInputStream(context.getApplicationInfo().dataDir
		 * + "/api/ContextHolder.class")); } catch (RuntimeException e) {
		 * Log.d(tag, e.toString()); } catch (IOException e) { Log.d(tag,
		 * e.toString()); }
		 */
		for (File classFile : classes) {
			String cl = classFile.getPath();
			String clName = cl.replace(dirTmp.getPath() + "/", "")
					.replace(".class", "").replace('/', '.');
			Log.d(tag, "--------\n cl=" + cl);
			Log.d(tag, "clName=" + clName);
			try {
				byte[] dataClass;
				dataClass = ModifyClass.modifyClass(new FileInputStream(
						classFile), "javax/microedition/util/ContextHolder");
				String newPath = classFile.getPath().replace(dirTmp.getPath(),
						dirForJAssist.getPath());
				File newFile = new File(newPath);
				Log.d(tag, "newPath=" + newPath);
				newFile.getParentFile().mkdirs();
				if (dataClass != null) {
					Log.d(tag, "modify ok");
					FileOutputStream fos = new FileOutputStream(newFile);
					fos.write(dataClass, 0, dataClass.length);
					fos.flush();
					fos.close();
				} else {
					Log.d(tag, "modify not detected");
					classFile.renameTo(newFile);
				}
			} catch (FileNotFoundException e) {
				Log.d(tag, e.getMessage());
			} catch (IOException e) {
				Log.d(tag, e.getMessage());
			}
			/*
			 * try { //Log.d(tag, "cls: " + classFile.getPath()); CtClass
			 * ctClass = cp.makeClass(new FileInputStream(classFile));
			 * Log.d(tag, "class: " + ctClass.getName()); CtMethod[] methods =
			 * ctClass.getDeclaredMethods(); for (CtMethod ctMethod: methods) {
			 * //Log.d(tag, "    method: " + ctMethod.getLongName());
			 * ctMethod.instrument(new ExprEditor() {
			 * 
			 * @Override public void edit(MethodCall mcall) { //Log.d(tag,
			 * "        call: " + mcall.getClassName() + "->" +
			 * mcall.getMethodName()); if
			 * (mcall.getClassName().equals(classRepl) &&
			 * mcall.getMethodName().equals(methodRepl)) { Log.d(tag,
			 * "replace call"); try {
			 * mcall.replace("{$_ = ContextHolder.getResourceAsStream($$);}"); }
			 * catch (CannotCompileException e) { Log.d(tag, e.toString());
			 * e.printStackTrace(); } } } }); } } catch (CannotCompileException
			 * e) { Log.d(tag, "1 " + e.getMessage()); } catch (RuntimeException
			 * e) { Log.d(tag, "2 " + e.getMessage()); } catch (IOException e) {
			 * Log.d(tag, "3 " + e.getMessage()); e.printStackTrace(System.err);
			 * }
			 */
		}
	}

	@Override
	public void onProgressUpdate(String... s) {

	}

	@Override
	public void onPreExecute() {
		// Log.i(tag, "onPreExecute");
		dialog = new ProgressDialog(context);
		dialog.setIndeterminate(true);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.setTitle(R.string.converting_wait);
		dialog.show();
	}

	@Override
	public void onPostExecute(Boolean result) {
		// Log.i(tag, "onPostExecute with: " + result);
		Toast t;
		if (result) {
			t = Toast.makeText(context,
					context.getResources().getString(R.string.convert_complete)
							+ " " + appDir, Toast.LENGTH_LONG);
			((MainActivity)context).updateApps();
		} else {
			t = Toast.makeText(context, err, Toast.LENGTH_LONG);

		}
		dialog.dismiss();
		t.show();
	}
}

/*
 * Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sb.firefds.firefdskit.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.CursorLoader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import eu.chainfire.libsuperuser.Shell.SU;

public class Utils {

	private static Boolean mIsExynosDevice = null;

	public static int cmdId = 1982;
	// GB Context
	private static Context mGbContext;

	public static void closeStatusBar(Context context) throws Throwable {
		Object sbservice = context.getSystemService("statusbar");
		Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
		Method showsb = statusbarManager.getMethod("collapsePanels");
		showsb.invoke(sbservice);
	}

	public static void resetPermissions(Context context) {
		executeScript(context, "reset_permissions");
	}

	public static void createCSCFiles(Context context) {
		executeScript(context, "create_csc_files");
	}

	public static void applyCSCFeatues(Context context) {
		executeScript(context, "apply_csc_features");
	}

	public static void reboot(Context context) {
		try {
			rebootSystem(context, null);
			return;

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static synchronized Context getGbContext(Context context) throws Throwable {
		if (mGbContext == null) {
			mGbContext = context.createPackageContext(Packages.XTOUCHWIZ, Context.CONTEXT_IGNORE_SECURITY);
		}
		return mGbContext;
	}

	public static void rebootEPM(Context context, String rebootType) {
		try {
			rebootSystem(context, rebootType);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void rebootSystem(final Context context, final String rebootType) {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				try {
					final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
					pm.reboot(rebootType);

				} catch (Throwable e) {
					XposedBridge.log(e);
				}
			}
		}, 500);

	}

	public static void savefile(Context context, Uri sourceuri, String path) {
		String sourceFilename = getUriPath(context, sourceuri);
		String dir = android.os.Environment.getExternalStorageDirectory().getPath() + File.separatorChar
				+ Constants.BACKUP_DIR;

		String destinationFilename = dir + File.separatorChar + path;

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		try {

			new File(dir).mkdirs();

			bis = new BufferedInputStream(new FileInputStream(sourceFilename));
			bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
			byte[] buf = new byte[1024];
			bis.read(buf);
			do {
				bos.write(buf);
			} while (bis.read(buf) != -1);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				if (bis != null)
					bis.close();
				if (bos != null)
					bos.close();
			} catch (Throwable e) {

			}
		}
	}

	public static String getUriPath(Context context, Uri uri) {
		String[] data = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(context, uri, data, null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public static void enableBootSounds(Context context) {
		if (new File("/system/media/audio/ui/PowerOn.ogg.bak").isFile()
				|| new File("/system/etc/PowerOn.ogg.bak").isFile()) {
			executeScript(context, "enable_boot_sounds");
		}
	}

	public static void disableBootSounds(Context context) {
		if (new File("/system/media/audio/ui/PowerOn.ogg").isFile() || new File("/system/etc/PowerOn.ogg").isFile()) {
			executeScript(context, "disable_boot_sounds");
		}
	}

	public static void disableVolumeControlSounds(Context context) {
		if (new File("/system/media/audio/ui/TW_Volume_control.ogg").isFile()) {
			executeScript(context, "disable_volume_sounds");
		}
	}

	public static void enableVolumeControlSounds(Context context) {
		if (new File("/system/media/audio/ui/TW_Volume_control.ogg.bak").isFile()) {
			executeScript(context, "enable_volume_sounds");
		}
	}

	public static void disableLowBatterySounds(Context context) {
		if (new File("/system/media/audio/ui/LowBattery.ogg").isFile()) {
			executeScript(context, "disable_low_battery_sounds");
		}
	}

	public static void enableLowBatterySounds(Context context) {
		if (new File("/system/media/audio/ui/LowBattery.ogg.bak").isFile()) {
			executeScript(context, "enable_low_battery_sounds");
		}
	}

	public static void hideViewBGContent(ViewGroup vg) {
		try {

			vg.setBackgroundColor(Color.TRANSPARENT);
			int childCount = vg.getChildCount();
			if (childCount > 0) {
				for (int i = 0; i < childCount; i++) {
					try {
						View v = vg.getChildAt(i);
						v.setBackgroundColor(Color.TRANSPARENT);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static void executeScript(Context context, String name) {
		File scriptFile = writeAssetToCacheFile(context, name);
		if (scriptFile == null)
			return;

		scriptFile.setReadable(true, false);
		scriptFile.setExecutable(true, false);

		new SuTask().execute(new String[] { "cd " + context.getCacheDir(), "./" + scriptFile.getName(),
				"rm " + scriptFile.getName() });

	}

	public static class SuTask extends AsyncTask<String, Void, Void> {

		protected Void doInBackground(String... params) {
			try {
				SU.run(params);
			} catch (Throwable e) {
				e.printStackTrace();
			}

			return null;
		}

	}

	private static File writeAssetToCacheFile(Context context, String name) {
		return writeAssetToCacheFile(context, name, name);
	}

	private static File writeAssetToCacheFile(Context context, String assetName, String fileName) {
		File file = null;
		try {
			InputStream in = context.getAssets().open(assetName);
			file = new File(context.getCacheDir(), fileName);
			FileOutputStream out = new FileOutputStream(file);

			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			in.close();
			out.close();

			return file;
		} catch (Throwable e) {
			e.printStackTrace();
			if (file != null)
				file.delete();

			return null;
		}
	}

	public static TextView setTypeface(XSharedPreferences prefs, TextView tv) {

		int typeStyle = Typeface.NORMAL;
		if (!prefs.getString("statusbarTextStyle", "Normal").equalsIgnoreCase("Normal")) {
			if (prefs.getString("statusbarTextStyle", "Normal").equalsIgnoreCase("Italic")) {
				typeStyle = Typeface.ITALIC;
			} else if (prefs.getString("statusbarTextStyle", "Normal").equalsIgnoreCase("Bold")) {
				typeStyle = Typeface.BOLD;
			}
		}

		if (!prefs.getString("statusbarTextFace", "Regular").equalsIgnoreCase("Regular")) {
			String typeFace = "sans-serif";
			if (prefs.getString("statusbarTextFace", "Regular").equalsIgnoreCase("Light")) {
				typeFace = "sans-serif-light";
			}
			if (prefs.getString("statusbarTextFace", "Regular").equalsIgnoreCase("Condensed")) {
				typeFace = "sans-serif-condensed";
			}
			if (prefs.getString("statusbarTextFace", "Regular").equalsIgnoreCase("Thin")) {
				typeFace = "sans-serif-thin";
			}
			tv.setTypeface(Typeface.create(typeFace, typeStyle));
		} else {
			tv.setTypeface(tv.getTypeface(), typeStyle);
		}

		return tv;

	}

	public static boolean isSamsungRom() {
		if (new File("/system/framework/twframework-res.apk").isFile()) {
			return true;
		}

		return false;
	}

	public static boolean isPackageExisted(Context context, String targetPackage) {
		List<ApplicationInfo> packages;
		PackageManager pm;
		pm = context.getPackageManager();
		packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo packageInfo : packages) {
			if (packageInfo.packageName.equals(targetPackage))
				return true;
		}
		return false;
	}

	public static void setEnglishLocale(Context context) {

		Locale.setDefault(Locale.ENGLISH);
		Configuration config = new Configuration();
		config.locale = Locale.ENGLISH;
		context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
	}

	public static boolean isExynosDevice() {
		if (mIsExynosDevice != null)
			return mIsExynosDevice;

		mIsExynosDevice = Build.HARDWARE.toLowerCase(Locale.UK).contains("smdk");
		return mIsExynosDevice;
	}

	public static boolean contains(final int[] array, final int v) {
		for (final int e : array)
			if (e == v)
				return true;

		return false;
	}
}

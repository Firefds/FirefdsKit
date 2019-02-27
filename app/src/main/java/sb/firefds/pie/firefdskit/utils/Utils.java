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
package sb.firefds.pie.firefdskit.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
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
import android.os.IBinder;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import eu.chainfire.libsuperuser.Shell.SU;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class Utils {

    private static final String OMC_PATH = "persist.sys.omc_path";
    private static final String OMC_SUPPORT = "persist.sys.omc_support";

    //private static final String OMC_ENABLE = "persist.sys.omc.enable";
    public enum CscType {
        CSC, OMC_CSC, OMC_OMC
    }

    private static Boolean mIsExynosDevice = null;
    private static CscType mCscType = null;

    public static int cmdId = 1982;
    // GB Context
    private static Context mGbContext;

    public static void closeStatusBar(Context context) throws Throwable {
        Object sbservice = context.getSystemService("statusbar");
        @SuppressLint("PrivateApi") Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
        Method showsb = statusbarManager.getMethod("collapsePanels");
        showsb.invoke(sbservice);
    }

    public static void resetPermissions(Context context) {
        executeScript(context, "reset_permissions");
    }

    public static void createCSCFiles(Context context) {
        switch (getCSCType()) {
            case CSC:
                executeScript(context, "create_csc_files");
                break;
            case OMC_CSC:
                executeScript(context, "create_omc_csc_files");
                break;
            case OMC_OMC:
                executeScript(context, "create_omc_omc_files");
                break;
        }
    }

    public static void applyCSCFeatues(Context context) {
        switch (getCSCType()) {
            case CSC:
                executeScript(context, "apply_csc_features");
                break;
            case OMC_CSC:
                executeScript(context, "apply_omc_csc_features");
                break;
            case OMC_OMC:
                executeScript(context, "apply_omc_omc_features");
                break;
        }
    }

    public static void reboot() {
        try {
            rebootSystem(null);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static synchronized Context getGbContext(Context context) throws Throwable {
        if (mGbContext == null) {
            mGbContext = context.createPackageContext(Packages.FIREFDSKIT, Context.CONTEXT_IGNORE_SECURITY);
        }
        return mGbContext;
    }

    public static synchronized Context getGbContext(Context context, Configuration config) throws Throwable {
        if (mGbContext == null) {
            mGbContext = context.createPackageContext(Packages.FIREFDSKIT,
                    Context.CONTEXT_IGNORE_SECURITY);
        }
        return (config == null ? mGbContext : mGbContext.createConfigurationContext(config));
    }

    public static void rebootEPM(String rebootType) {
        try {
            rebootSystem(rebootType);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void rebootSystem(final String rebootType) {
        new Handler().postDelayed(() -> new SuTask()
                .execute("reboot " + rebootType), 1000);
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
            } catch (Throwable ignored) {

            }
        }
    }

    private static String getUriPath(Context context, Uri uri) {
        String[] data = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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

    @SuppressLint("SetWorldReadable")
    private static void executeScript(Context context, String name) {
        File scriptFile = writeAssetToCacheFile(context, name);
        if (scriptFile == null)
            return;

        scriptFile.setReadable(true, false);
        scriptFile.setExecutable(true, false);

        new SuTask().execute("cd " + context.getCacheDir(), "./" + scriptFile.getName(),
                "rm " + scriptFile.getName());
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
            if (file != null) {
                file.delete();
            }
            return null;
        }
    }

    public static void setTypeface(SharedPreferences prefs, TextView tv) {

        int typeStyle = Typeface.NORMAL;
        if (!Objects.requireNonNull(prefs.getString("statusbarTextStyle", "Normal"))
                .equalsIgnoreCase("Normal")) {
            if (Objects.requireNonNull(prefs.getString("statusbarTextStyle", "Normal"))
                    .equalsIgnoreCase("Italic")) {
                typeStyle = Typeface.ITALIC;
            } else if (Objects.requireNonNull(prefs.getString("statusbarTextStyle", "Normal"))
                    .equalsIgnoreCase("Bold")) {
                typeStyle = Typeface.BOLD;
            }
        }

        if (!Objects.requireNonNull(prefs.getString("statusbarTextFace", "Regular"))
                .equalsIgnoreCase("Regular")) {
            String typeFace = "sans-serif";
            if (Objects.requireNonNull(prefs.getString("statusbarTextFace", "Regular"))
                    .equalsIgnoreCase("Light")) {
                typeFace = "sans-serif-light";
            }
            if (Objects.requireNonNull(prefs.getString("statusbarTextFace", "Regular"))
                    .equalsIgnoreCase("Condensed")) {
                typeFace = "sans-serif-condensed";
            }
            if (Objects.requireNonNull(prefs.getString("statusbarTextFace", "Regular"))
                    .equalsIgnoreCase("Thin")) {
                typeFace = "sans-serif-thin";
            }
            tv.setTypeface(Typeface.create(typeFace, typeStyle));
        } else {
            tv.setTypeface(tv.getTypeface(), typeStyle);
        }

    }

    public static boolean isSamsungRom() {
        return new File("/system/framework/com.samsung.device.jar").isFile();
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

    public static boolean isExynosDevice() {
        if (mIsExynosDevice != null)
            return mIsExynosDevice;

        mIsExynosDevice = Build.HARDWARE.toLowerCase(Locale.UK).contains("smdk");
        return mIsExynosDevice;
    }

    public static CscType getCSCType() {
        if (mCscType != null)
            return mCscType;

        mCscType = SystemProperties.getBoolean(OMC_SUPPORT, false) ? CscType.OMC_OMC : CscType.OMC_CSC;

        //mCscType = SystemProperties.getBoolean(OMC_ENABLE, false) ?
        //		SystemProperties.getBoolean(OMC_SUPPORT, false) ? CscType.OMC_OMC : CscType.OMC_CSC :
        //			CscType.CSC;
		/*				
		if (SystemProperties.getBoolean(OMC_ENABLE, false)) {
			if (SystemProperties.getBoolean(OMC_SUPPORT, false))
				mCscType = CscType.OMC_OMC;
			else
				mCscType = CscType.OMC_CSC;
		}
		else 
			mCscType = CscType.CSC;*/
        return mCscType;
    }

    static String getOMCPath() {
        return SystemProperties.get(OMC_PATH);
    }

    public static boolean contains(final int[] array, final int v) {
        for (final int e : array)
            if (e == v)
                return true;

        return false;
    }

    public static void performSoftReboot() {
        try {
            Class<?> classSm = XposedHelpers.findClass("android.os.ServiceManager", null);
            Class<?> classIpm = XposedHelpers.findClass("android.os.IPowerManager.Stub", null);
            IBinder b = (IBinder) XposedHelpers.callStaticMethod(
                    classSm, "getService", Context.POWER_SERVICE);
            Object ipm = XposedHelpers.callStaticMethod(classIpm, "asInterface", b);
            XposedHelpers.callMethod(ipm, "crash", "Hot reboot");
        } catch (Throwable t) {
            try {
                SystemProp.set("ctl.restart", "surfaceflinger");
                SystemProp.set("ctl.restart", "zygote");
            } catch (Throwable t2) {
                XposedBridge.log(t);
                XposedBridge.log(t2);
            }
        }
    }

    static class SystemProp extends Utils {

        private SystemProp() {

        }

        // Get the value for the given key
        // @param key: key to lookup
        // @return null if the key isn't found
        public static String get(String key) {
            String ret;

            try {
                Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                ret = (String) callStaticMethod(classSystemProperties, "get", key);
            } catch (Throwable t) {
                ret = null;
            }
            return ret;
        }

        // Get the value for the given key
        // @param key: key to lookup
        // @param def: default value to return
        // @return if the key isn't found, return def if it isn't null, or an empty string otherwise
        public static String get(String key, String def) {
            String ret = def;

            try {
                Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                ret = (String) callStaticMethod(classSystemProperties, "get", key, def);
            } catch (Throwable t) {
                ret = def;
            }
            return ret;
        }

        // Get the value for the given key, and return as an integer
        // @param key: key to lookup
        // @param def: default value to return
        // @return the key parsed as an integer, or def if the key isn't found or cannot be parsed
        public static Integer getInt(String key, Integer def) {
            Integer ret = def;

            try {
                Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                ret = (Integer) callStaticMethod(classSystemProperties, "getInt", key, def);
            } catch (Throwable t) {
                ret = def;
            }
            return ret;
        }

        // Get the value for the given key, and return as a long
        // @param key: key to lookup
        // @param def: default value to return
        // @return the key parsed as a long, or def if the key isn't found or cannot be parsed
        public static Long getLong(String key, Long def) {
            Long ret = def;

            try {
                Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                ret = (Long) callStaticMethod(classSystemProperties, "getLong", key, def);
            } catch (Throwable t) {
                ret = def;
            }
            return ret;
        }

        // Get the value (case insensitive) for the given key, returned as a boolean
        // Values 'n', 'no', '0', 'false' or 'off' are considered false
        // Values 'y', 'yes', '1', 'true' or 'on' are considered true
        // If the key does not exist, or has any other value, then the default result is returned
        // @param key: key to lookup
        // @param def: default value to return
        // @return the key parsed as a boolean, or def if the key isn't found or cannot be parsed
        public static Boolean getBoolean(String key, boolean def) {
            Boolean ret = def;

            try {
                Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                ret = (Boolean) callStaticMethod(classSystemProperties, "getBoolean", key, def);
            } catch (Throwable t) {
                ret = def;
            }
            return ret;
        }

        // Set the value for the given key
        public static void set(String key, String val) {
            try {
                Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                callStaticMethod(classSystemProperties, "set", key, val);
            } catch (Throwable t) {
            }
        }
    }
}

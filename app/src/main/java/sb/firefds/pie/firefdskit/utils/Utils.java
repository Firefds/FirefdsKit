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
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Objects;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import com.topjohnwu.superuser.Shell;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class Utils {

    private static final String OMC_PATH = "persist.sys.omc_path";
    private static final String OMC_SUPPORT = "persist.sys.omc_support";
    private static final String STATUSBAR_SERVICE = "statusbar";

    public enum CscType {
        CSC, OMC_CSC, OMC_OMC
    }

    private static CscType mCscType = null;
    private static boolean omcEncryptedFlag;

    public static void closeStatusBar(Context context) throws Throwable {
        @SuppressLint("WrongConstant") Object sbservice =
                context.getSystemService(STATUSBAR_SERVICE);
        @SuppressLint("PrivateApi") Class<?> statusbarManager =
                Class.forName("android.app.StatusBarManager");
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

    private static void executeScript(Context context, String name) {
        try {
            InputStream inputStream = context.getAssets().open(name);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            inputStream.close();
            new SuTask().execute(result.toString("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class SuTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {
            try {
                Shell.su(params).exec();
            } catch (Throwable e) {
                e.printStackTrace();
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

    public static CscType getCSCType() {
        if (mCscType != null)
            return mCscType;

        mCscType = SystemProperties.getBoolean(OMC_SUPPORT, false) ? CscType.OMC_OMC : CscType.OMC_CSC;

        return mCscType;
    }

    public static boolean isOmcEncryptedFlag() {
        return omcEncryptedFlag;
    }

    public static void setOmcEncryptedFlag() {
        Utils.omcEncryptedFlag = !SystemProperties.get("ro.omc.img_mount").isEmpty()
                && !SystemProperties.get("persist.sys.omc_install").isEmpty();
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
        } catch (Throwable e1) {
            try {
                SystemProp.set("ctl.restart", "surfaceflinger");
                SystemProp.set("ctl.restart", "zygote");
            } catch (Throwable e2) {
                XposedBridge.log(e1);
                XposedBridge.log(e2);
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

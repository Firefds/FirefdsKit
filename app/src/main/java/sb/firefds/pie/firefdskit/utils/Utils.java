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
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Objects;

import androidx.core.content.ContextCompat;
import de.robv.android.xposed.XposedBridge;
import sb.firefds.pie.firefdskit.R;

import com.google.android.material.snackbar.Snackbar;
import com.topjohnwu.superuser.Shell;

public class Utils {

    private static final String OMC_PATH = "persist.sys.omc_path";
    private static final String OMC_SUPPORT = "persist.sys.omc_support";
    private static final String STATUSBAR_SERVICE = "statusbar";

    public enum CscType {
        CSC, OMC_CSC, OMC_OMC
    }

    private static CscType mCscType = null;
    @SuppressLint("StaticFieldLeak")
    private static Context mGbContext;

    public static void closeStatusBar(Context context) throws Throwable {
        @SuppressLint("WrongConstant") Object sbservice =
                context.getSystemService(STATUSBAR_SERVICE);
        @SuppressLint("PrivateApi") Class<?> statusbarManager =
                Class.forName("android.app.StatusBarManager");
        Method showsb = statusbarManager.getMethod("collapsePanels");
        showsb.invoke(sbservice);
    }

    public static void resetPermissions(Context context) {
        executeScript(context, R.raw.reset_permissions);
    }

    public static void createCSCFiles(Context context) {
        switch (getCSCType()) {
            case CSC:
                executeScript(context, R.raw.create_csc_files);
                break;
            case OMC_CSC:
                executeScript(context, R.raw.create_omc_csc_files);
                break;
            case OMC_OMC:
                executeScript(context, R.raw.create_omc_omc_files);
                break;
        }
    }

    public static void applyCSCFeatues(Context context) {
        switch (getCSCType()) {
            case CSC:
                executeScript(context, R.raw.apply_csc_features);
                break;
            case OMC_CSC:
                executeScript(context, R.raw.apply_omc_csc_features);
                break;
            case OMC_OMC:
                executeScript(context, R.raw.apply_omc_omc_features);
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
        new Handler().postDelayed(() -> Shell.su("reboot " + rebootType).submit(), 1000);
    }

    public static void disableVolumeControlSounds(Context context) {
        if (new File("/system/media/audio/ui/TW_Volume_control.ogg").isFile()) {
            executeScript(context, R.raw.disable_volume_sounds);
        }
    }

    public static void enableVolumeControlSounds(Context context) {
        if (new File("/system/media/audio/ui/TW_Volume_control.ogg.bak").isFile()) {
            executeScript(context, R.raw.enable_volume_sounds);
        }
    }

    public static void disableLowBatterySounds(Context context) {
        if (new File("/system/media/audio/ui/LowBattery.ogg").isFile()) {
            executeScript(context, R.raw.disable_low_battery_sounds);
        }
    }

    public static void enableLowBatterySounds(Context context) {
        if (new File("/system/media/audio/ui/LowBattery.ogg.bak").isFile()) {
            executeScript(context, R.raw.enable_low_battery_sounds);
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

    private static void executeScript(Context context, int scriptId) {
        Shell.su(context.getResources().openRawResource(scriptId)).submit();
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

    public static boolean isNotSamsungRom() {
        return !new File("/system/framework/com.samsung.device.jar").isFile();
    }

    public static CscType getCSCType() {
        if (mCscType != null)
            return mCscType;

        mCscType = SystemProperties.getBoolean(OMC_SUPPORT, false) ?
                CscType.OMC_OMC : CscType.OMC_CSC;

        return mCscType;
    }

    public static boolean isDeviceEncrypted() {
        return SystemProperties.get("ro.crypto.state").equals("encrypted");
    }

    public static boolean isOmcEncryptedFlag() {
        return SystemProperties.get("ro.omc.img_mount").equals("0");
    }

    static String getOMCPath() {
        return SystemProperties.get(OMC_PATH);
    }

    public static void performQuickReboot() {
        try {
            new Handler().postDelayed(() -> Shell.su("setprop ctl.restart zygote").submit(), 1000);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    public static Snackbar createSnackbar(View view, int stringId, Context context) {
        Snackbar snackbar = Snackbar
                .make(view, stringId, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(context, android.R.color.white));
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.primaryColor));
        return snackbar;
    }
}

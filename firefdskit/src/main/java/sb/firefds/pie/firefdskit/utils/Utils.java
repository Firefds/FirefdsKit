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
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.LocaleList;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Objects;

import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.R;

import static sb.firefds.pie.firefdskit.FirefdsKitActivity.getAppContext;
import static sb.firefds.pie.firefdskit.utils.Packages.FIREFDSKIT;

public class Utils {

    private static final String STATUSBAR_SERVICE = "statusbar";

    @SuppressLint("StaticFieldLeak")
    private static Context mGbContext;

    public static void closeStatusBar(Context context) throws Throwable {
        @SuppressLint("WrongConstant") Object sbservice = context.getSystemService(STATUSBAR_SERVICE);
        @SuppressLint("PrivateApi") Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
        Method showsb = statusbarManager.getMethod("collapsePanels");
        showsb.invoke(sbservice);
    }

    public static void reboot() {
        try {
            rebootSystem(null);
        } catch (Throwable e) {
            Utils.log(e);
        }
    }

    public static void rebootEPM(String rebootType) {
        try {
            rebootSystem(rebootType);
        } catch (Throwable e) {
            Utils.log(e);
        }
    }

    private static void rebootSystem(final String rebootType) {
        try {
            ((PowerManager) getAppContext().getSystemService(Context.POWER_SERVICE)).reboot(rebootType);
        } catch (Exception e) {
            Utils.log(e);
        }
    }

    public static synchronized Context getGbContext(Context context) throws Throwable {
        if (mGbContext == null) {
            mGbContext = context.createPackageContext(FIREFDSKIT, Context.CONTEXT_IGNORE_SECURITY);
        }
        return mGbContext;
    }

    public static synchronized Context getGbContext(Context context, Configuration config) throws Throwable {
        if (mGbContext == null) {
            mGbContext = context.createPackageContext(FIREFDSKIT,
                    Context.CONTEXT_IGNORE_SECURITY);
        }
        return (config == null ? mGbContext : mGbContext.createConfigurationContext(config));
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
        return !(new File("/system/framework/com.samsung.device.jar").isFile() ||
                new File("/system/framework/com.samsung.device.lite.jar").isFile());
    }

    public static boolean isDeviceEncrypted() {
        return SystemProperties.get("ro.crypto.state").equals("encrypted");
    }

    public static void performQuickReboot() {
        try {
            set("ctl.restart", "surfaceflinger");
            set("ctl.restart", "zygote");
        } catch (Throwable e) {
            log(e);
        }
    }

    public static Snackbar createSnackbar(View view, int stringId, Context context) {
        Snackbar snackbar = Snackbar
                .make(view, stringId, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(context, android.R.color.white));
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.primaryColor));
        return snackbar;
    }

    public static void log(Throwable e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        Log.e("FFK", errors.toString());
    }

    public static ContextWrapper checkForceEnglish(Context context, SharedPreferences prefs) {
        LocaleList localeList;
        Configuration config = context.getResources().getConfiguration();
        if (prefs.getBoolean("forceEnglish", false)) {
            config.setLocale(Locale.ENGLISH);
            localeList = new LocaleList(Locale.ENGLISH);
        } else {
            config.setLocale(Locale.getDefault());
            localeList = new LocaleList(Locale.getDefault());
        }
        LocaleList.setDefault(localeList);
        config.setLocales(localeList);
        context = context.createConfigurationContext(config);
        return new ContextWrapper(context);
    }

    private static void set(String key, String val) {
        try {
            Class<?> classSystemProperties = XposedHelpers.findClass("android.os.SystemProperties", null);
            XposedHelpers.callStaticMethod(classSystemProperties, "set", key, val);
        } catch (Throwable e) {
            log(e);
        }
    }
}

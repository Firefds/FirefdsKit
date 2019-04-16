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
package sb.firefds.oreo.firefdskit;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.PowerManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.oreo.firefdskit.utils.Packages;

public class XAndroidPackage {

    private static XSharedPreferences prefs;
    private static ClassLoader classLoader;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext = null;

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        XAndroidPackage.prefs = prefs;
        XAndroidPackage.classLoader = classLoader;

        if (prefs.getBoolean("disableTIMA", true))
            try {
                disableTIMA();
            } catch (Throwable e) {
                e.printStackTrace();
            }

        if (prefs.getBoolean("disableDVFS", true)) {
            try {
                disableTwDvfs();
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        } else if (prefs.getString("disableDVFSWhiteList", "").length() > 0) {
            try {
                disableDVFSWhiteList();
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }

    private static void disableTIMA() {
        final Class<?> mTimaService = XposedHelpers.findClass("com.android.server.TimaService", classLoader);

        XposedHelpers.findAndHookMethod(mTimaService, "checkEvent", int.class, int.class,
                XC_MethodReplacement.returnConstant(null));
    }

    private static void disableTwDvfs() {

        final Class<?> mCustomFrequencyManager = XposedHelpers.findClass(Packages.ANDROID
                + ".os.CustomFrequencyManager", classLoader);

        try {
            XposedHelpers.findAndHookMethod(mCustomFrequencyManager, "newFrequencyRequest", int.class, int.class,
                    long.class, String.class, Context.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if (mContext == null) {
                                mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                            }
                            if (mContext != null) {
                                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                                if (!pm.isPowerSaveMode()) {
                                    param.setResult(null);
                                }
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void disableDVFSWhiteList() {

        final Class<?> mCustomFrequencyManager = XposedHelpers.findClass(Packages.ANDROID
                + ".os.CustomFrequencyManager", classLoader);

        try {
            XposedHelpers.findAndHookMethod(mCustomFrequencyManager, "newFrequencyRequest", int.class, int.class,
                    long.class, String.class, Context.class, new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {

                                String pkg = null;
                                if (mContext == null) {
                                    mContext = (Context) param.args[4];
                                }

                                if (mContext != null) {
                                    PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                                    if (!pm.isPowerSaveMode()) {
                                        @SuppressWarnings("deprecation")
                                        List<RunningTaskInfo> list = ((ActivityManager) mContext
                                                .getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
                                        if (list != null && list.size() > 0) {
                                            pkg = list.get(0).topActivity.getPackageName();
                                        }

                                        if (pkg != null && prefs.getString("enableDVFSBlackList", "").contains(pkg)) {
                                            param.setResult(null);
                                        }
                                    }
                                }
                            } catch (Throwable e) {
                                XposedBridge.log(e);
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

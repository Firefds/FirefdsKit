/*
 * Copyright (C) 2020 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.pie.firefdskit;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import de.robv.android.xposed.*;

import java.util.List;
import java.util.Optional;

import static sb.firefds.pie.firefdskit.utils.Preferences.*;

public class XAndroidPackage {

    private static final String WINDOW_STATE = "com.android.server.wm.WindowState";
    private static final String WINDOW_MANAGER_SERVICE = "com.android.server.wm.WindowManagerService";
    private static final String PACKAGE_MANAGER_SERVICE_UTILS = "com.android.server.pm.PackageManagerServiceUtils";
    private static final String PACKAGE_MANAGER_SERVICE = "com.android.server.pm.PackageManagerService";
    private static final String INSTALLER = "com.android.server.pm.Installer";
    private static final String STATUS_BAR_MANAGER_SERVICE = "com.android.server.statusbar.StatusBarManagerService";
    private static final String USB_HANDLER = "com.android.server.usb.UsbDeviceManager.UsbHandler";
    private static final String SHUTDOWN_THREAD = "com.android.server.power.ShutdownThread";
    private static final String WIFI_NATIVE_CLASS = "com.android.server.wifi.WifiNative";
    private static final String AP_CONFIG_UTIL_CLASS = "com.android.server.wifi.util.ApConfigUtil";
    private static final String ACTIVITY_MANAGER_SERVICE = "com.android.server.am.ActivityManagerService";
    private static final String PERSONA_SERVICE_HELPER = "com.android.server.pm.PersonaServiceHelper";
    private static final String STORAGE_MANAGER_SERVICE = "com.android.server.StorageManagerService";
    @SuppressLint("StaticFieldLeak")
    private static Context mPackageManagerServiceContext;
    private static boolean isFB;

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        try {

            XposedHelpers.findAndHookMethod(ACTIVITY_MANAGER_SERVICE,
                    classLoader,
                    "appRestrictedInBackgroundLocked",
                    int.class,
                    String.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if (param.args[1].equals("org.meowcat.edxposed.manager")) {
                                param.setResult(0);
                            }
                        }
                    });

            if (prefs.getBoolean(PREF_ENABLE_DUAL_SIM_SD_CARD, false)) {
                XposedHelpers.findAndHookMethod(STORAGE_MANAGER_SERVICE,
                        classLoader,
                        "isSimSdBlock",
                        XC_MethodReplacement.returnConstant(Boolean.FALSE));
            }

            if (prefs.getBoolean(PREF_ENABLE_SECURE_FOLDER, false)) {
                XposedHelpers.findAndHookMethod(PERSONA_SERVICE_HELPER,
                        classLoader,
                        "shouldBlockUserStart",
                        Context.class,
                        int.class,
                        XC_MethodReplacement.returnConstant(false));
            }

            if (prefs.getBoolean(PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS, false)) {
                Class<?> wifiNativeClass = XposedHelpers.findClass(WIFI_NATIVE_CLASS, classLoader);
                XposedHelpers.findAndHookMethod(AP_CONFIG_UTIL_CLASS,
                        classLoader,
                        "isWifiApSupport5G",
                        wifiNativeClass,
                        String.class,
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            }

            if (prefs.getBoolean(PREF_DEFAULT_REBOOT_BEHAVIOR, false)) {
                Class<?> shutdownThreadClass = XposedHelpers.findClass(SHUTDOWN_THREAD, classLoader);
                XposedHelpers.findAndHookMethod(shutdownThreadClass,
                        "shutdownInner",
                        Context.class,
                        boolean.class,
                        boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                String mRebootReason = (String) XposedHelpers.getStaticObjectField(shutdownThreadClass, "mRebootReason");
                                boolean mReboot = XposedHelpers.getStaticBooleanField(shutdownThreadClass, "mReboot");
                                if (mReboot && mRebootReason.equals("userrequested")) {
                                    XposedHelpers.setStaticObjectField(shutdownThreadClass, "mRebootReason", "recovery");
                                }
                            }
                        });
            }

            if (prefs.getBoolean(PREF_DISABLE_SECURE_FLAG, false)) {
                Class<?> windowStateClass = XposedHelpers.findClass(WINDOW_STATE, classLoader);
                XposedHelpers.findAndHookMethod(WINDOW_MANAGER_SERVICE,
                        classLoader,
                        "isSecureLocked",
                        windowStateClass,
                        XC_MethodReplacement.returnConstant(Boolean.FALSE));
            }

            if (prefs.getBoolean(PREF_DISABLE_SIGNATURE_CHECK, false)) {
                if (mPackageManagerServiceContext == null) {
                    Class<?> installer = XposedHelpers.findClass(INSTALLER, classLoader);
                    XposedHelpers.findAndHookConstructor(PACKAGE_MANAGER_SERVICE,
                            classLoader,
                            Context.class,
                            installer,
                            boolean.class,
                            boolean.class,
                            new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) {
                                    mPackageManagerServiceContext = (Context) param.args[0];
                                }
                            });
                }

                XposedHelpers.findAndHookMethod(PACKAGE_MANAGER_SERVICE_UTILS,
                        classLoader,
                        "compareSignatures",
                        Signature[].class,
                        Signature[].class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                new Handler(Looper.getMainLooper()).post(new DLX());
                                if (!isFB) {
                                    param.setResult(0);
                                }
                            }
                        });
            }

            if (prefs.getBoolean(PREF_SUPPORTS_MULTIPLE_USERS, false)) {
                XposedHelpers.findAndHookMethod(UserManager.class,
                        "supportsMultipleUsers",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                param.setResult(true);
                            }
                        });

                XposedHelpers.findAndHookMethod(UserManager.class,
                        "getMaxSupportedUsers",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                param.setResult(prefs.getInt(PREF_MAX_SUPPORTED_USERS, 3));
                            }
                        });
            }

            if (prefs.getBoolean(PREF_HIDE_VOLTE_ICON, false)) {
                XposedHelpers.findAndHookMethod(STATUS_BAR_MANAGER_SERVICE,
                        classLoader,
                        "setIconVisibility",
                        String.class,
                        boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (param.args[0].equals("ims_volte") ||
                                        param.args[0].equals("ims_volte2")) {
                                    param.args[1] = false;
                                }
                            }
                        });
            }

            if (prefs.getBoolean(PREF_HIDE_USB_NOTIFICATION, false)) {
                XposedHelpers.findAndHookMethod(USB_HANDLER,
                        classLoader,
                        "updateUsbNotification",
                        boolean.class,
                        XC_MethodReplacement.returnConstant(null));
            }

        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }

    private static class DLX implements Runnable {
        public void run() {
            try {
                Optional<ActivityManager> activityManager =
                        Optional.ofNullable((ActivityManager) mPackageManagerServiceContext
                                .getSystemService(Context.ACTIVITY_SERVICE));

                List<ActivityManager.AppTask> runningTasks = activityManager
                        .map(ActivityManager::getAppTasks)
                        .get();

                runningTasks.forEach(appTask ->
                        Optional.ofNullable(appTask.getTaskInfo().topActivity)
                                .ifPresent(componentName -> isFB = componentName.getPackageName()
                                        .equals("com.facebook.katana")));

            } catch (NullPointerException e) {
                XposedBridge.log(e);
            }
        }
    }
}

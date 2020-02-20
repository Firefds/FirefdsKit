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
package sb.firefds.q.firefdskit;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.q.firefdskit.utils.Packages.SAMSUNG_SETTINGS;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DISABLE_BLUETOOTH_DIALOG;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DISABLE_SYNC_DIALOG;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_MAKE_OFFICIAL;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_MAX_SUPPORTED_USERS;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_SHOW_NETWORK_SPEED_MENU;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_SUPPORTS_MULTIPLE_USERS;

public class XSecSettingsPackage {

    private static final String BLUETOOTH_SCAN_DIALOG = SAMSUNG_SETTINGS + ".bluetooth.BluetoothScanDialog";
    private static final String SEC_ACCOUNT_TILES = SAMSUNG_SETTINGS + ".qstile.SecAccountTiles";
    private static final String SEC_DEVICE_INFO_UTILS = SAMSUNG_SETTINGS + ".deviceinfo.SecDeviceInfoUtils";
    private static final String STATUS_BAR = SAMSUNG_SETTINGS + ".notification.StatusBar";
    private static final String SYSCOPE_STATUS_PREFERENCE_CONTROLLER =
            SAMSUNG_SETTINGS + ".deviceinfo.status.SysScopeStatusPreferenceController";
    private static final String ICDVERIFICATION = "com.sec.icdverification.ICDVerification";
    private static final String SETTINGS_UTILS = "com.android.settings.Utils";


    private static ClassLoader classLoader;

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        XSecSettingsPackage.classLoader = classLoader;

        if (prefs.getBoolean(PREF_MAKE_OFFICIAL, true)) {
            makeOfficial();
        }

        if (prefs.getBoolean(PREF_SHOW_NETWORK_SPEED_MENU, false)) {
            showNetworkSpeedMenu();
        }

        try {
            XposedHelpers.findAndHookMethod(
                    BLUETOOTH_SCAN_DIALOG,
                    classLoader,
                    "onCreate",
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            prefs.reload();
                            if (prefs.getBoolean(PREF_DISABLE_BLUETOOTH_DIALOG, false))
                                ((android.app.Activity) param.thisObject).finish();
                        }
                    });

        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(SEC_ACCOUNT_TILES,
                    classLoader,
                    "showConfirmPopup",
                    boolean.class,
                    new XC_MethodHook() {
                        @SuppressLint("MissingPermission")
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if (prefs.getBoolean(PREF_DISABLE_SYNC_DIALOG, false)) {
                                ContentResolver.setMasterSyncAutomatically((Boolean) param.args[0]);
                                param.setResult(null);
                            }
                        }
                    });

            if (prefs.getBoolean(PREF_SUPPORTS_MULTIPLE_USERS, false)) {
                XposedHelpers.findAndHookMethod(UserManager.class, "supportsMultipleUsers",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                param.setResult(true);
                            }
                        });

                XposedHelpers.findAndHookMethod(UserManager.class, "getMaxSupportedUsers",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                param.setResult(prefs.getInt(PREF_MAX_SUPPORTED_USERS, 3));
                            }
                        });
            }

            if (prefs.getBoolean(PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS, false)) {
                XposedHelpers.findAndHookMethod(SETTINGS_UTILS,
                        classLoader,
                        "initMHSFeature",
                        Context.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Class<?> settingsUtils = XposedHelpers.findClass(SETTINGS_UTILS, classLoader);
                                XposedHelpers.setStaticBooleanField(settingsUtils, "SUPPORT_MOBILEAP_MAXCLIENT_MENU", true);
                                XposedHelpers.setStaticBooleanField(settingsUtils, "SUPPORT_MOBILEAP_5G", true);
                                XposedHelpers.setStaticBooleanField(settingsUtils, "SUPPORT_MOBILEAP_5G_BASED_ON_COUNTRY", true);
                                XposedHelpers.setStaticObjectField(settingsUtils, "SUPPORT_MOBILEAP_REGION", "NA");
                            }
                        });
            }

        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void makeOfficial() {
        try {
            XposedHelpers.findAndHookMethod(ICDVERIFICATION,
                    classLoader,
                    "check",
                    XC_MethodReplacement.returnConstant(1));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        try {
            XposedHelpers.findAndHookMethod(SYSCOPE_STATUS_PREFERENCE_CONTROLLER,
                    classLoader,
                    "getICDVerification",
                    XC_MethodReplacement.returnConstant(1));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(SEC_DEVICE_INFO_UTILS,
                    classLoader,
                    "checkRootingCondition",
                    XC_MethodReplacement.returnConstant(Boolean.FALSE));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(SEC_DEVICE_INFO_UTILS,
                    classLoader,
                    "isSupportRootBadge",
                    Context.class,
                    XC_MethodReplacement.returnConstant(Boolean.FALSE));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void showNetworkSpeedMenu() {
        try {
            XposedHelpers.findAndHookMethod(STATUS_BAR,
                    classLoader,
                    "onCreate",
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedHelpers.setStaticBooleanField(param.thisObject.getClass(),
                                    "isSupportNetworkSpeedFeature",
                                    true);
                        }
                    });

            XposedHelpers.findAndHookMethod(STATUS_BAR,
                    classLoader,
                    "onResume",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedHelpers.setStaticBooleanField(param.thisObject.getClass(),
                                    "isSupportNetworkSpeedFeature",
                                    true);
                        }
                    });

        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

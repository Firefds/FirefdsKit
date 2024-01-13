/*
 * Copyright (C) 2023 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.u.firefdskit;

import static sb.firefds.u.firefdskit.utils.Packages.SAMSUNG_SETTINGS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_BLUETOOTH_DIALOG;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_MAKE_OFFICIAL;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_MAX_SUPPORTED_USERS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SUPPORTS_MULTIPLE_USERS;

import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSecSettingsPackage {

    private static final String BLUETOOTH_SCAN_DIALOG = SAMSUNG_SETTINGS + ".bluetooth.BluetoothScanDialog";
    private static final String SEC_DEVICE_INFO_UTILS = SAMSUNG_SETTINGS + ".deviceinfo.SecDeviceInfoUtils";
    private static final String SETTINGS_UTILS = "com.android.settings.Utils";


    private static ClassLoader classLoader;

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        XSecSettingsPackage.classLoader = classLoader;

        if (prefs.getBoolean(PREF_MAKE_OFFICIAL, true)) {
            makeOfficial();
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
                                prefs.reload();
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
                                Class<?> settingsUtils = XposedHelpers.findClass(SETTINGS_UTILS,
                                        classLoader);
                                XposedHelpers.setStaticBooleanField(settingsUtils,
                                        "SUPPORT_MOBILEAP_MAXCLIENT_MENU",
                                        true);
                                XposedHelpers.setStaticBooleanField(settingsUtils,
                                        "SUPPORT_MOBILEAP_5G", true);
                                XposedHelpers.setStaticBooleanField(settingsUtils,
                                        "SUPPORT_MOBILEAP_5G_BASED_ON_COUNTRY",
                                        true);
                                XposedHelpers.setStaticObjectField(settingsUtils,
                                        "SUPPORT_MOBILEAP_REGION", "NA");
                            }
                        });
            }

        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void makeOfficial() {
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
                    "isAlterModel",
                    XC_MethodReplacement.returnConstant(Boolean.FALSE));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(SEC_DEVICE_INFO_UTILS,
                    classLoader,
                    "isPhoneStatusUnlocked",
                    XC_MethodReplacement.returnConstant(Boolean.FALSE));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

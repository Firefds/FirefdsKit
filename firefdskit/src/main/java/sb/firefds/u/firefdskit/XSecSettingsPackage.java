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

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setStaticBooleanField;
import static de.robv.android.xposed.XposedHelpers.setStaticIntField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetBooleanPref;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetIntPref;
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

public class XSecSettingsPackage {

    private static final String BLUETOOTH_SCAN_DIALOG = SAMSUNG_SETTINGS + ".bluetooth.BluetoothScanDialog";
    private static final String SEC_DEVICE_INFO_UTILS = SAMSUNG_SETTINGS + ".deviceinfo.SecDeviceInfoUtils";
    private static final String SETTINGS_UTILS = "com.android.settings.Utils";


    private static ClassLoader classLoader;

    public static void doHook(ClassLoader classLoader) {

        XSecSettingsPackage.classLoader = classLoader;

        makeOfficial();

        try {
            findAndHookMethod(BLUETOOTH_SCAN_DIALOG, classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DISABLE_BLUETOOTH_DIALOG, false))
                        ((android.app.Activity) param.thisObject).finish();
                }
            });

        } catch (Throwable e) {
            log(e);
        }

        try {
            findAndHookMethod(UserManager.class, "supportsMultipleUsers", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_SUPPORTS_MULTIPLE_USERS, false)) {
                        param.setResult(true);
                    }
                }
            });

            findAndHookMethod(UserManager.class, "getMaxSupportedUsers", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_SUPPORTS_MULTIPLE_USERS, false)) {
                        param.setResult(reloadAndGetIntPref(PREF_MAX_SUPPORTED_USERS, 3));
                    }
                }
            });

            findAndHookMethod(SETTINGS_UTILS, classLoader, "initMHSFeature", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS, false)) {
                        Class<?> settingsUtils = findClass(SETTINGS_UTILS, classLoader);
                        setStaticIntField(settingsUtils, "MAX_CLIENT_4_MOBILEAP", 10);
                        setStaticBooleanField(settingsUtils, "SUPPORT_MOBILEAP_5G_BASED_ON_COUNTRY", true);
                        setStaticBooleanField(settingsUtils, "SUPPORT_MOBILEAP_6G_BASED_ON_COUNTRY", true);
                        setStaticBooleanField(settingsUtils, "SUPPORT_MOBILEAP_WIFISHARING", true);
                        setStaticBooleanField(settingsUtils, "SUPPORT_MOBILEAP_WIFISHARINGLITE", true);
                        setStaticObjectField(settingsUtils, "SUPPORT_MOBILEAP_REGION", "NA");
                    }
                }
            });

        } catch (Throwable e) {
            log(e);
        }
    }

    private static void makeOfficial() {
        try {
            findAndHookMethod(SEC_DEVICE_INFO_UTILS,
                              classLoader,
                              "checkRootingCondition",
                              XC_MethodReplacement.returnConstant(!reloadAndGetBooleanPref(PREF_MAKE_OFFICIAL, true)));
        } catch (Throwable e) {
            log(e);
        }

        try {
            findAndHookMethod(SEC_DEVICE_INFO_UTILS,
                              classLoader,
                              "isAlterModel",
                              XC_MethodReplacement.returnConstant(!reloadAndGetBooleanPref(PREF_MAKE_OFFICIAL, true)));
        } catch (Throwable e) {
            log(e);
        }

        try {
            findAndHookMethod(SEC_DEVICE_INFO_UTILS,
                              classLoader,
                              "isPhoneStatusUnlocked",
                              XC_MethodReplacement.returnConstant(!reloadAndGetBooleanPref(PREF_MAKE_OFFICIAL, true)));
        } catch (Throwable e) {
            log(e);
        }
    }
}

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
package sb.firefds.pie.firefdskit;


import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Packages;

public class XSecSettingsPackage {

    private static ClassLoader classLoader;

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        XSecSettingsPackage.classLoader = classLoader;

        if (prefs.getBoolean("makeMeTooLegit", true)) {
            makeOfficial();
        }

        if (prefs.getBoolean("showNetworkSpeedMenu", false)) {
            showNetworkSpeedMenu();
        }

        try {
            XposedHelpers.findAndHookMethod(
                    Packages.SAMSUNG_SETTINGS + ".bluetooth.BluetoothScanDialog",
                    classLoader,
                    "onCreate",
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            if (prefs.getBoolean("disableBluetoothScanDialog", false))
                                ((android.app.Activity) param.thisObject).finish();
                        }
                    });

        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(Packages.SAMSUNG_SETTINGS + ".qstile.SecAccountTiles",
                    classLoader,
                    "showConfirmPopup",
                    boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (prefs.getBoolean("disableSyncDialog", false)) {
                                ContentResolver.setMasterSyncAutomatically((Boolean) param.args[0]);
                                param.setResult(null);
                            }
                        }
                    });

        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void makeOfficial() {
        String SYSCOPE_STATUS_PREFERENCE_CONTROLLER =
                Packages.SAMSUNG_SETTINGS + ".deviceinfo.status.SyscopeStatusPreferenceController";
        String SEC_DEVICE_INFO_UTILS =
                Packages.SAMSUNG_SETTINGS + ".deviceinfo.SecDeviceInfoUtils";

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
            XposedHelpers.findAndHookMethod(SEC_DEVICE_INFO_UTILS, classLoader,
                    "isSupportRootBadge",
                    Context.class,
                    XC_MethodReplacement.returnConstant(Boolean.FALSE));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void showNetworkSpeedMenu() {
        try {
            XposedHelpers.findAndHookMethod(Packages.SAMSUNG_SETTINGS + ".display.StatusBar",
                    classLoader,
                    "onCreate",
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedHelpers.setStaticBooleanField(param.thisObject.getClass(),
                                    "isSupportNetworkSpeedFeature",
                                    true);
                        }
                    });

            XposedHelpers.findAndHookMethod(Packages.SAMSUNG_SETTINGS + ".display.StatusBar",
                    classLoader,
                    "onResume",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
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

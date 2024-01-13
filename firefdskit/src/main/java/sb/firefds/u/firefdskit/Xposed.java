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

import static sb.firefds.u.firefdskit.utils.Packages.FIREFDSKIT;

import android.util.Log;

import androidx.annotation.Keep;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import sb.firefds.u.firefdskit.utils.Packages;
import sb.firefds.u.firefdskit.utils.Utils;

@Keep
public class Xposed implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private static XSharedPreferences prefs;

    private static XSharedPreferences getPref() {
        XSharedPreferences pref = new XSharedPreferences(FIREFDSKIT);
        return pref.getFile().canRead() ? pref : null;
    }

    @Override
    public void initZygote(StartupParam startupParam) {

        // Do not load if Not a Samsung Device
        if (Utils.isNotSamsungRom()) {
            Log.e("FFK", "com.samsung.device.jar or com.samsung.device.lite.jar not found!");
            XposedBridge.log("FFK: com.samsung.device.jar or com.samsung.device.lite.jar not found!");
        }

        XSharedPreferences pref = getPref();
        if (pref != null) {
            prefs = pref;
            XposedBridge.log("FFK: Firefds Kit preferences loaded correctly!");
        } else {
            Log.e("FFK", "Cannot load pref for zygote properly");
            XposedBridge.log("FFK: Cannot load pref for zygote properly");
        }
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {

        // Do not load if Not a Touchwiz Rom
        if (Utils.isNotSamsungRom()) {
            Log.e("FFK", "com.samsung.device.jar or com.samsung.device.lite.jar not found!");
            XposedBridge.log("FFK: com.samsung.device.jar or com.samsung.device.lite.jar not found!");
            return;
        }

        if (prefs == null) {
            Log.e("FFK", "Xposed cannot read Firefds Kit preferences!");
            XposedBridge.log("FFK: Xposed cannot read Firefds Kit preferences!");
            return;
        }

        if (lpparam.packageName.equals(FIREFDSKIT)) {
            try {
                XposedHelpers.findAndHookMethod(FIREFDSKIT + ".XposedChecker",
                        lpparam.classLoader,
                        "isActive",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        try {
            XSystemWide.doHook(prefs);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        if (lpparam.packageName.equals(Packages.ANDROID)) {

            try {
                XPM34.doHook(lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }

            try {
                XAndroidPackage.doHook(prefs, lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (lpparam.packageName.equals(Packages.NFC)) {
            try {
                XNfcPackage.doHook(prefs, lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (lpparam.packageName.equals(Packages.SYSTEM_UI)) {
            try {
                XSysUIPackage.doHook(prefs, lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (lpparam.packageName.equals(Packages.SETTINGS)) {
            try {
                XSecSettingsPackage.doHook(prefs, lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (lpparam.packageName.equals(Packages.EMAIL)) {
            try {
                XSecEmailPackage.doHook(prefs, lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (lpparam.packageName.equals(Packages.CAMERA)) {
            try {
                XSecCameraPackage.doHook(prefs, lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (lpparam.packageName.equals(Packages.MTP_APPLICATION)) {
            try {
                XMtpApplication.doHook(prefs, lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (lpparam.packageName.equals(Packages.FOTA_AGENT)) {
            try {
                XFotaAgentPackage.doHook(prefs, lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (lpparam.packageName.equals(Packages.SAMSUNG_MESSAGING)) {
            try {
                XMessagingPackage.doHook(prefs, lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (lpparam.packageName.equals(Packages.SAMSUNG_CONTACTS)) {
            try {
                XContactsPackage.doHook(prefs, lpparam.classLoader);
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (lpparam.packageName.equals(Packages.SMART_CAPTURE)) {
            try {
                XSmartCapturePackage.doHook(prefs, lpparam.classLoader);
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }
    }
}

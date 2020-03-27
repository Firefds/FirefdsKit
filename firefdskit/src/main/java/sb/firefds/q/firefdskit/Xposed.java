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

import androidx.annotation.Keep;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import sb.firefds.q.firefdskit.utils.Packages;
import sb.firefds.q.firefdskit.utils.Utils;

import static sb.firefds.q.firefdskit.utils.Packages.FIREFDSKIT;

@Keep
public class Xposed implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private static XSharedPreferences prefs;
    private static File securePrefFile = new File("/data/user_de/0/sb.firefds.q.firefdskit/shared_prefs/" + FIREFDSKIT + "_preferences.xml");

    @Override
    public void initZygote(StartupParam startupParam) {

        // Do not load if Not a Touchwiz Rom
        if (Utils.isNotSamsungRom()) {
            XposedBridge.log("FFK: com.samsung.device.jar or com.samsung.device.lite.jar not found!");
            return;
        }

        getModuleSharedPreferences();
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {

        // Do not load if Not a Touchwiz Rom
        if (Utils.isNotSamsungRom()) {
            XposedBridge.log("FFK: com.samsung.device.jar or com.samsung.device.lite.jar not found!");
            return;
        }

        if (lpparam.packageName.equals(FIREFDSKIT)) {
            if (prefs != null) {
                try {
                    XposedHelpers.findAndHookMethod(FIREFDSKIT + ".XposedChecker",
                            lpparam.classLoader,
                            "isActive",
                            XC_MethodReplacement.returnConstant(Boolean.TRUE));
                } catch (Throwable e) {
                    XposedBridge.log(e);
                }
            } else {
                XposedBridge.log("FFK: Xposed cannot read XTouchWiz preferences!");
            }
        }

        try {
            XSystemWide.doHook(prefs);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        if (lpparam.packageName.equals(Packages.ANDROID)) {

            try {
                XPM29.doHook(lpparam.classLoader);
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

    private static void getModuleSharedPreferences() {
        if (prefs == null) {
            prefs = Utils.isDeviceEncrypted() ?
                    new XSharedPreferences(securePrefFile) :
                    new XSharedPreferences(FIREFDSKIT);
            prefs.makeWorldReadable();
        } else {
            prefs.reload();
        }
    }
}

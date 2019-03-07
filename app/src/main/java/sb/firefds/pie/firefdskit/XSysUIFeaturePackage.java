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

import android.media.AudioManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Packages;

public class XSysUIFeaturePackage {

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {


        try {
            if (prefs.getBoolean("isStatusBarDoubleTapEnabled", false)) {
                XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".KnoxStateMonitor.CustomSdkMonitor",
                        classLoader,
                        "isStatusBarDoubleTapEnabled",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            }

            if (prefs.getBoolean("setEyeStrainDialogEnabled", false)) {
                XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".settings.ToggleSliderView",
                        classLoader,
                        "setEyeStrainDialogEnabled",
                        int.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                param.args[0] = 0;
                            }
                        });
            }

            if (prefs.getBoolean("disableLoudVolumeWarning", false)) {
                XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".volume.VolumeDialogControllerImpl",
                        classLoader,
                        "onShowSafetyWarningW",
                        int.class,
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                AudioManager mAudio =
                                        (AudioManager) XposedHelpers.getObjectField(param.thisObject,
                                                "mAudio");
                                XposedHelpers.callMethod(mAudio, "disableSafeMediaVolume");
                                return null;
                            }
                        });
            }
            if (prefs.getBoolean("enableFingerprintUnlock", false)) {
                XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor",
                        classLoader,
                        "isUnlockingWithFingerprintAllowed",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            }

            if (prefs.getBoolean("enableBiometricsUnlock", false)) {
                XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor",
                        classLoader,
                        "isUnlockCompleted",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));

                XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor",
                        classLoader,
                        "isUnlockingWithBiometricAllowed",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            }

        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}
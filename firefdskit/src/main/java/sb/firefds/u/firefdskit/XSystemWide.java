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
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetBooleanPref;
import static sb.firefds.u.firefdskit.utils.Constants.CONFIG_RECORDING;
import static sb.firefds.u.firefdskit.utils.Constants.CONFIG_SVC_PROVIDER_FOR_UNKNOWN_NUMBER;
import static sb.firefds.u.firefdskit.utils.Constants.SUPPORT_REAL_TIME_NETWORK_SPEED;
import static sb.firefds.u.firefdskit.utils.Constants.SUPPORT_Z_PROJECT_FUNCTION_IN_GLOBAL;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DEFAULT_REBOOT_BEHAVIOR;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_SECURE_FLAG;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_CALL_ADD;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_CALL_RECORDING;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_SPAM_PROTECTION;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SHOW_NETWORK_SPEED_MENU;

import android.os.PowerManager;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import com.samsung.android.wifi.SemWifiManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;

public class XSystemWide {

    public static void doHook() {

        try {
            findAndHookMethod(Window.class, "setFlags", int.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DISABLE_SECURE_FLAG, false)) {
                        Integer flags = (Integer) param.args[0];
                        flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                        param.args[0] = flags;
                    }
                }
            });

            findAndHookMethod(SurfaceView.class, "setSecure", boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DISABLE_SECURE_FLAG, false)) {
                        param.args[0] = false;
                    }
                }
            });

            findAndHookMethod(PowerManager.class, "reboot", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DEFAULT_REBOOT_BEHAVIOR, false)) {
                        if (param.args[0] == null) {
                            param.args[0] = "recovery";
                        }
                    }
                }
            });

            findAndHookMethod(SemWifiManager.class,
                              "supportWifiAp5GBasedOnCountry",
                              XC_MethodReplacement.returnConstant(reloadAndGetBooleanPref(
                                      PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS,
                                      false)));

            findAndHookMethod(SemWifiManager.class,
                              "supportWifiAp6GBasedOnCountry",
                              XC_MethodReplacement.returnConstant(reloadAndGetBooleanPref(
                                      PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS,
                                      false)));

            findAndHookMethod(SemFloatingFeature.class, "getString", String.class, cscFeatureGetStringHook());

            findAndHookMethod(SemFloatingFeature.class,
                              "getString",
                              String.class,
                              String.class,
                              cscFeatureGetStringHook());

            findAndHookMethod(SemCscFeature.class, "getString", String.class, cscFeatureGetStringHook());

            findAndHookMethod(SemCscFeature.class, "getString", String.class, String.class, cscFeatureGetStringHook());

            findAndHookMethod(SemFloatingFeature.class, "getBoolean", String.class, cscFeatureGetBooleanHook());

            findAndHookMethod(SemFloatingFeature.class,
                              "getBoolean",
                              String.class,
                              boolean.class,
                              cscFeatureGetBooleanHook());

            findAndHookMethod(SemCscFeature.class, "getBoolean", String.class, cscFeatureGetBooleanHook());

            findAndHookMethod(SemCscFeature.class,
                              "getBoolean",
                              String.class,
                              boolean.class,
                              cscFeatureGetBooleanHook());
        } catch (Throwable e) {
            log(e);
        }
    }

    @NonNull
    private static XC_MethodHook cscFeatureGetStringHook() {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0].equals(CONFIG_RECORDING)) {
                    if (reloadAndGetBooleanPref(PREF_ENABLE_CALL_RECORDING, false)) {
                        if (reloadAndGetBooleanPref(PREF_ENABLE_CALL_ADD, false)) {
                            param.setResult("RecordingAllowedByMenu");
                        } else {
                            param.setResult("RecordingAllowed");
                        }
                    } else {
                        param.setResult("");
                    }
                }
                if (param.args[0].equals(CONFIG_SVC_PROVIDER_FOR_UNKNOWN_NUMBER)) {
                    if (reloadAndGetBooleanPref(PREF_ENABLE_SPAM_PROTECTION, true)) {
                        param.setResult("whitepages,whitepages,whitepages");
                    } else {
                        param.setResult("");
                    }
                }
            }
        };
    }

    @NonNull
    private static XC_MethodHook cscFeatureGetBooleanHook() {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0].equals(SUPPORT_REAL_TIME_NETWORK_SPEED)) {
                    param.setResult(reloadAndGetBooleanPref(PREF_SHOW_NETWORK_SPEED_MENU, false));
                }
                if (param.args[0].equals(SUPPORT_Z_PROJECT_FUNCTION_IN_GLOBAL)) {
                    param.setResult(reloadAndGetBooleanPref(PREF_SHOW_NETWORK_SPEED_MENU, false));
                }
            }
        };
    }
}

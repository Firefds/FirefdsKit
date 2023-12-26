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

import static sb.firefds.u.firefdskit.utils.Constants.CONFIG_RECORDING;
import static sb.firefds.u.firefdskit.utils.Constants.CONFIG_SVC_PROVIDER_FOR_UNKNOWN_NUMBER;
import static sb.firefds.u.firefdskit.utils.Constants.SAMSUNG_BLUR;
import static sb.firefds.u.firefdskit.utils.Constants.SUPPORT_REAL_TIME_NETWORK_SPEED;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DEFAULT_REBOOT_BEHAVIOR;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_SECURE_FLAG;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_CALL_ADD;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_CALL_RECORDING;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_SAMSUNG_BLUR;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_SPAM_PROTECTION;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SHOW_NETWORK_SPEED_MENU;

import android.os.PowerManager;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import com.samsung.android.wifi.SemWifiManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSystemWide {

    public static void doHook(XSharedPreferences prefs) {

        try {
            if (prefs.getBoolean(PREF_DISABLE_SECURE_FLAG, false)) {
                XposedHelpers.findAndHookMethod(Window.class,
                        "setFlags",
                        int.class,
                        int.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                Integer flags = (Integer) param.args[0];
                                flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                                param.args[0] = flags;
                            }
                        });

                XposedHelpers.findAndHookMethod(SurfaceView.class,
                        "setSecure",
                        boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                param.args[0] = false;
                            }
                        });
            }

            if (prefs.getBoolean(PREF_DEFAULT_REBOOT_BEHAVIOR, false)) {
                XposedHelpers.findAndHookMethod(PowerManager.class,
                        "reboot",
                        String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (param.args[0] == null) {
                                    param.args[0] = "recovery";
                                }
                            }
                        });
            }

            if (prefs.getBoolean(PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS, false)) {
                XposedHelpers.findAndHookMethod(SemWifiManager.class,
                        "supportWifiAp5GBasedOnCountry",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));

                XposedHelpers.findAndHookMethod(SemWifiManager.class,
                        "supportWifiAp6GBasedOnCountry",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            }

            XposedHelpers.findAndHookMethod(SemFloatingFeature.class,
                    "getString",
                    String.class,
                    cscFeatureGetStringHook(prefs));

            XposedHelpers.findAndHookMethod(SemFloatingFeature.class,
                    "getString",
                    String.class,
                    String.class,
                    cscFeatureGetStringHook(prefs));

            XposedHelpers.findAndHookMethod(SemCscFeature.class,
                    "getString",
                    String.class,
                    cscFeatureGetStringHook(prefs));

            XposedHelpers.findAndHookMethod(SemCscFeature.class,
                    "getString",
                    String.class,
                    String.class,
                    cscFeatureGetStringHook(prefs));

            XposedHelpers.findAndHookMethod(SemFloatingFeature.class,
                    "getBoolean",
                    String.class,
                    cscFeatureGetBooleanHook(prefs));

            XposedHelpers.findAndHookMethod(SemFloatingFeature.class,
                    "getBoolean",
                    String.class,
                    boolean.class,
                    cscFeatureGetBooleanHook(prefs));

            XposedHelpers.findAndHookMethod(SemCscFeature.class,
                    "getBoolean",
                    String.class,
                    cscFeatureGetBooleanHook(prefs));

            XposedHelpers.findAndHookMethod(SemCscFeature.class,
                    "getBoolean",
                    String.class,
                    boolean.class,
                    cscFeatureGetBooleanHook(prefs));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static XC_MethodHook cscFeatureGetStringHook(XSharedPreferences prefs) {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0].equals(CONFIG_RECORDING)) {
                    prefs.reload();
                    if (prefs.getBoolean(PREF_ENABLE_CALL_RECORDING, false)) {
                        if (prefs.getBoolean(PREF_ENABLE_CALL_ADD, false)) {
                            param.setResult("RecordingAllowedByMenu");
                        } else {
                            param.setResult("RecordingAllowed");
                        }
                    } else {
                        param.setResult("");
                    }
                }
                if (param.args[0].equals(CONFIG_SVC_PROVIDER_FOR_UNKNOWN_NUMBER)) {
                    prefs.reload();
                    if (prefs.getBoolean(PREF_ENABLE_SPAM_PROTECTION, true)) {
                        param.setResult("whitepages,whitepages,whitepages");
                    } else {
                        param.setResult("");
                    }
                }
            }
        };
    }

    private static XC_MethodHook cscFeatureGetBooleanHook(XSharedPreferences prefs) {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0].equals(SUPPORT_REAL_TIME_NETWORK_SPEED)) {
                    param.setResult(prefs.getBoolean(PREF_SHOW_NETWORK_SPEED_MENU, false));
                }
                if (param.args[0].equals(SAMSUNG_BLUR)) {
                    param.setResult(prefs.getBoolean(PREF_ENABLE_SAMSUNG_BLUR, false));
                }
            }
        };
    }
}

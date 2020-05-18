/*
 * Copyright (C) 2020 Shauli Bracha for FirefdsKit Project (firefds@xda)
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

import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.UserManager;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import de.robv.android.xposed.*;

import static sb.firefds.pie.firefdskit.utils.Constants.*;
import static sb.firefds.pie.firefdskit.utils.Preferences.*;

public class XSystemWide {

    private final static String WIFI_AP_CUST_CLASS = "android.net.wifi.WifiApCust";

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
                Class<?> WifiApCustClass = XposedHelpers.findClass(WIFI_AP_CUST_CLASS, null);
                XposedHelpers.setStaticBooleanField(WifiApCustClass, "mSupportMaxClientMenu", true);
                XposedHelpers.setStaticBooleanField(WifiApCustClass, "mSupport5G", true);
                XposedHelpers.setStaticBooleanField(WifiApCustClass, "mSupport5GBasedOnCountry", true);
                XposedHelpers.setStaticObjectField(WifiApCustClass, "mRegion", "NA");

                XposedHelpers.findAndHookMethod(WifiManager.class,
                        "semSupportWifiAp5GBasedOnCountry",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));

                XposedHelpers.findAndHookMethod(WifiManager.class,
                        "semSupportWifiAp5G",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            }

            XposedHelpers.findAndHookMethod(SemCscFeature.class,
                    "getString",
                    String.class,
                    cscFeatureGetStringHook(prefs));

            XposedHelpers.findAndHookMethod(SemCscFeature.class,
                    "getString",
                    String.class,
                    String.class,
                    cscFeatureGetStringHook(prefs));

            if (prefs.getBoolean(PREF_ENABLE_SECURE_FOLDER, false)) {
                XposedBridge.hookAllMethods(SemFloatingFeature.class,
                        "getBoolean",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (param.args[0].equals(SUPPORT_UNIFIED_KEY_STORE)) {
                                    param.setResult(true);
                                }
                            }
                        });

                XposedHelpers.findAndHookMethod(UserManager.class,
                        "getUserInfo",
                        int.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Object userInfo = param.getResult();
                                if (userInfo != null) {
                                    String userInfoName = (String) XposedHelpers.getObjectField(userInfo, "name");
                                    int userInfoAttributes = XposedHelpers.getIntField(userInfo, "attributes");
                                    if (userInfoName.equals("Secure Folder") && userInfoAttributes != 0) {
                                        XposedHelpers.setIntField(userInfo, "attributes", 0);
                                        param.setResult(userInfo);
                                    }
                                }
                            }
                        });
            }
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
                        param.setResult(null);
                    }
                }
            }
        };
    }
}

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

import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.pie.firefdskit.utils.Packages.SYSTEM_UI;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CARRIER_SIZE;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DATA_ICON_BEHAVIOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DATA_USAGE_VIEW;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_HIDE_CARRIER_LABEL;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SHOW_NETWORK_SPEED_MENU;

public class XSysUINotificationPanelPackage {

    private static final String LTE_INSTEAD_OF_4G = "STATBAR_DISPLAY_LTE_INSTEAD_OF_4G_ICON";
    private static final String FOUR_G_PLUS_INSTEAD_OF_4G = "STATBAR_DISPLAY_4G_PLUS_INSTEAD_OF_4G_ICON";
    private static final String FOUR_G_INSTEAD_OF_4G_PLUS = "STATBAR_DISPLAY_4G_INSTEAD_OF_4G_PLUS_ICON";
    private static final String FOUR_HALF_G_INSTEAD_OF_4G_PLUS = "STATBAR_DISPLAY_4_HALF_G_INSTEAD_OF_4G_PLUS_ICON";
    private static final String RUNE = SYSTEM_UI + ".Rune";
    private static final String CARRIER_TEXT = "com.android.keyguard.CarrierText";
    private static final String DATA_USAGE_BAR = SYSTEM_UI + ".bar.DataUsageBar";
    private static final String NETSPEED_VIEW = SYSTEM_UI + ".statusbar.policy.NetspeedView";
    private static final String MOBILE_SIGNAL_CONTROLLER_CLASS = SYSTEM_UI + ".statusbar.policy.MobileSignalController";
    private static final Map<String, Integer> CARRIER_SIZES_MAP = new HashMap<>();
    private static final Map<String, String> DATA_ICONS_MAP = new HashMap<>();

    private static ClassLoader classLoader;

    static {
        CARRIER_SIZES_MAP.put("Small", 14);
        CARRIER_SIZES_MAP.put("Medium", 16);
        CARRIER_SIZES_MAP.put("Large", 18);
        CARRIER_SIZES_MAP.put("Larger", 19);
        CARRIER_SIZES_MAP.put("Largest", 20);
        DATA_ICONS_MAP.put("0", "DEFAULT");
        DATA_ICONS_MAP.put("1", LTE_INSTEAD_OF_4G);
        DATA_ICONS_MAP.put("2", FOUR_G_PLUS_INSTEAD_OF_4G);
        DATA_ICONS_MAP.put("3", FOUR_G_INSTEAD_OF_4G_PLUS);
        DATA_ICONS_MAP.put("4", FOUR_HALF_G_INSTEAD_OF_4G_PLUS);
    }

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        XSysUINotificationPanelPackage.classLoader = classLoader;
        final Class<?> systemUIRuneClass = XposedHelpers.findClass(RUNE, classLoader);

        try {
            XposedHelpers.findAndHookMethod(CARRIER_TEXT,
                    classLoader,
                    "updateCarrierText",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            TextView tvCarrier = (TextView) param.thisObject;

                            prefs.reload();
                            if (prefs.getBoolean(PREF_HIDE_CARRIER_LABEL, false)) {
                                tvCarrier.setText(" ");
                            }

                            int textSize = getCarrierSizeValue(prefs.getString(PREF_CARRIER_SIZE, "Small"));
                            tvCarrier.setTextSize(textSize);
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        String behaviorIndex = prefs.getString(PREF_DATA_ICON_BEHAVIOR, "0");
        String dataBehavior = getDataIconBehavior(behaviorIndex);
        if (!dataBehavior.equals("DEFAULT")) {
            changeDataIcon(systemUIRuneClass, dataBehavior);
        }

        try {
            XposedHelpers.findAndHookMethod(DATA_USAGE_BAR,
                    classLoader,
                    "isAvailable",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            prefs.reload();
                            return prefs.getBoolean(PREF_DATA_USAGE_VIEW, false);
                        }
                    });

        } catch (Exception e) {
            XposedBridge.log(e);
        }

        if (prefs.getBoolean(PREF_SHOW_NETWORK_SPEED_MENU, false)) {
            try {
                XposedHelpers.findAndHookMethod(NETSPEED_VIEW,
                        classLoader,
                        "onAttachedToWindow",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                XposedHelpers.setStaticBooleanField(systemUIRuneClass,
                                        "STATBAR_SUPPORT_REAL_TIME_NETWORK_SPEED",
                                        true);
                            }
                        });

            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }
    }

    private static void changeDataIcon(Class<?> aClass, String dataIconBehavior) {
        try {
            XC_MethodHook mobileSignalMethodHook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    XposedHelpers.setStaticBooleanField(aClass, dataIconBehavior, true);
                }
            };

            XposedHelpers.findAndHookMethod(MOBILE_SIGNAL_CONTROLLER_CLASS,
                    classLoader,
                    "updateMobileIconGroup",
                    mobileSignalMethodHook);
            XposedHelpers.findAndHookMethod(MOBILE_SIGNAL_CONTROLLER_CLASS,
                    classLoader,
                    "updateTelephony",
                    mobileSignalMethodHook);

        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    public static Integer getCarrierSizeValue(String sizeName) {
        return CARRIER_SIZES_MAP.get(sizeName);
    }

    private static String getDataIconBehavior(String behaviorIndex) {
        return DATA_ICONS_MAP.get(behaviorIndex);
    }
}

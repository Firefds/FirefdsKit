/*
 * Copyright (C) 2022 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.s.firefdskit;

import static sb.firefds.s.firefdskit.utils.Packages.SYSTEM_UI;
import static sb.firefds.s.firefdskit.utils.Preferences.PREF_CARRIER_SIZE;
import static sb.firefds.s.firefdskit.utils.Preferences.PREF_DATA_ICON_BEHAVIOR;
import static sb.firefds.s.firefdskit.utils.Preferences.PREF_DATA_USAGE_VIEW;
import static sb.firefds.s.firefdskit.utils.Preferences.PREF_ENABLE_SAMSUNG_BLUR;
import static sb.firefds.s.firefdskit.utils.Preferences.PREF_HIDE_CARRIER_LABEL;
import static sb.firefds.s.firefdskit.utils.Preferences.PREF_SHOW_NETWORK_SPEED_MENU;

import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSysUINotificationPanelPackage {

    private static final String LTE_INSTEAD_OF_4G = "useLteInsteadOf4G";
    private static final String FOUR_G_PLUS_INSTEAD_OF_4G = "use4GPlusInsteadOf4G";
    private static final String FOUR_G_INSTEAD_OF_4G_PLUS = "use4GInstead4GPlus";
    private static final String FOUR_HALF_G_INSTEAD_OF_4G_PLUS = "use4HalfGInsteadOf4GPlus";
    private static final String OPERATOR = SYSTEM_UI + ".Operator";
    private static final String CARRIER_TEXT_MANAGER = "com.android.keyguard.CarrierTextManager";
    private static final String CARRIER_TEXT_CALLBACK_INFO = CARRIER_TEXT_MANAGER + ".CarrierTextCallbackInfo";
    private static final String CARRIER_TEXT = "com.android.keyguard.CarrierText";
    private static final String QP_RUNE = SYSTEM_UI + ".QpRune";
    private static final String BASIC_RUNE = SYSTEM_UI + ".BasicRune";
    private static final String LS_RUNE = SYSTEM_UI + ".LsRune";
    private static final Map<String, Integer> CARRIER_SIZES_MAP = new HashMap<>();
    private static final Map<String, String> DATA_ICONS_MAP = new HashMap<>();

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

        final Class<?> operatorClass = XposedHelpers.findClass(OPERATOR, classLoader);

        try {
            XposedHelpers.findAndHookMethod(CARRIER_TEXT_MANAGER,
                    classLoader,
                    "postToCallback",
                    CARRIER_TEXT_CALLBACK_INFO,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            prefs.reload();
                            if (prefs.getBoolean(PREF_HIDE_CARRIER_LABEL, false)) {
                                XposedHelpers.setObjectField(param.args[0], "carrierText", " ");
                            }
                        }
                    });

            XposedHelpers.findAndHookMethod(CARRIER_TEXT,
                    classLoader,
                    "onFinishInflate",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            prefs.reload();
                            int textSize = getCarrierSizeValue(prefs.getString(PREF_CARRIER_SIZE, "Small"));
                            ((TextView) param.thisObject).setTextSize(textSize);
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        String behaviorIndex = prefs.getString(PREF_DATA_ICON_BEHAVIOR, "0");
        String dataBehavior = getDataIconBehavior(behaviorIndex);
        if (!dataBehavior.equals("DEFAULT")) {
            changeDataIcon(operatorClass, dataBehavior);
        }

        if (prefs.getBoolean(PREF_DATA_USAGE_VIEW, false)) {
            try {
                Class<?> qpRune = XposedHelpers.findClass(QP_RUNE, classLoader);
                XposedHelpers.setStaticBooleanField(qpRune, "PANEL_CARRIERINFO_DATAUSAGE", true);
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }

        if (prefs.getBoolean(PREF_SHOW_NETWORK_SPEED_MENU, false)) {
            try {
                Class<?> basicRune = XposedHelpers.findClass(BASIC_RUNE, classLoader);
                XposedHelpers.setStaticBooleanField(basicRune, "STATUS_REAL_TIME_NETWORK_SPEED", true);
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }

        if (prefs.getBoolean(PREF_ENABLE_SAMSUNG_BLUR, true)) {
            try {
                Class<?> lsRune = XposedHelpers.findClass(LS_RUNE, classLoader);
                XposedHelpers.setStaticBooleanField(lsRune, "SECURITY_BOUNCER_WINDOW", true);
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }
    }

    private static void changeDataIcon(Class<?> aClass, String dataIconBehavior) {
        try {
            XposedHelpers.findAndHookMethod(aClass,
                    dataIconBehavior,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(Boolean.TRUE);
                        }
                    });
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

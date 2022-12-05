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
package sb.firefds.t.firefdskit;

import static sb.firefds.t.firefdskit.utils.Packages.SYSTEM_UI;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_CARRIER_SIZE;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_DATA_ICON_BEHAVIOR;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_ENABLE_SAMSUNG_BLUR;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_HIDE_CARRIER_LABEL;

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
    private static final String NOTIFICATION_SHADE_WINDOW_CONTROLLER_IMPL = SYSTEM_UI + ".statusbar.phone" +
            ".NotificationShadeWindowControllerImpl";
    private static final String STATE = NOTIFICATION_SHADE_WINDOW_CONTROLLER_IMPL + ".State";
    private static final Map<String, Float> CARRIER_SIZES_MAP = new HashMap<>();
    private static final Map<String, Integer> CLOCK_SIZES_MAP = new HashMap<>();
    private static final Map<String, String> DATA_ICONS_MAP = new HashMap<>();

    static {
        CLOCK_SIZES_MAP.put("Tiny", 10);
        CLOCK_SIZES_MAP.put("Smaller", 12);
        CLOCK_SIZES_MAP.put("Small", 14);
        CLOCK_SIZES_MAP.put("Medium", 16);
        CLOCK_SIZES_MAP.put("Large", 18);
        CLOCK_SIZES_MAP.put("Larger", 19);
        CLOCK_SIZES_MAP.put("Largest", 20);
        CARRIER_SIZES_MAP.put("Tiny", 19f);
        CARRIER_SIZES_MAP.put("Smaller", 29f);
        CARRIER_SIZES_MAP.put("Small", 39f);
        CARRIER_SIZES_MAP.put("Medium", 49f);
        CARRIER_SIZES_MAP.put("Large", 59f);
        CARRIER_SIZES_MAP.put("Larger", 69f);
        CARRIER_SIZES_MAP.put("Largest", 79f);
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
                    "setTextSize",
                    int.class,
                    float.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.args[1] = getCarrierSizeValue(prefs.getString(PREF_CARRIER_SIZE, "Small"));
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

        if (prefs.getBoolean(PREF_ENABLE_SAMSUNG_BLUR, false)) {
            try {
                Class<?> stateClass = XposedHelpers.findClass(STATE, classLoader);

                XposedHelpers.findAndHookMethod(NOTIFICATION_SHADE_WINDOW_CONTROLLER_IMPL,
                        classLoader,
                        "apply",
                        stateClass,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                XposedHelpers.callMethod(param.thisObject, "applyBouncer", param.args[0]);
                            }
                        });
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

    public static Float getCarrierSizeValue(String sizeName) {
        return CARRIER_SIZES_MAP.get(sizeName);
    }

    public static Integer getClockSizeValue(String sizeName) {
        return CLOCK_SIZES_MAP.get(sizeName);
    }

    private static String getDataIconBehavior(String behaviorIndex) {
        return DATA_ICONS_MAP.get(behaviorIndex);
    }
}

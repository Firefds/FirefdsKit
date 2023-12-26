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

import static sb.firefds.u.firefdskit.utils.Packages.SYSTEM_UI;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_4G_DATA_ICON_BEHAVIOR;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_5G_DATA_ICON_BEHAVIOR;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_CARRIER_SIZE;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_HIDE_CARRIER_LABEL;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSysUINotificationPanelPackage {

    private static final String LTE_INSTEAD_OF_4G = "useLTEInsteadOf4G";
    private static final String FOUR_G_PLUS_INSTEAD_OF_4G = "use4GPlusInsteadOf4G";
    private static final String FOUR_HALF_G_INSTEAD_OF_4G_PLUS = "use4HalfGInsteadOf4GPlus";
    private static final String FIVE_G_ONE_SHAPED_ICON = "use5gOneShapedIcon";
    private static final String FIVE_G_PLUS_ICON = "use5gPlusIcon";
    private static final String MOBILE_DATA_UTIL = SYSTEM_UI + ".statusbar.pipeline.carrier.MobileDataUtil";
    private static final String CARRIER_TEXT_MANAGER = "com.android.keyguard.CarrierTextManager";
    private static final String CARRIER_TEXT_CALLBACK_INFO = CARRIER_TEXT_MANAGER + ".CarrierTextCallbackInfo";
    private static final String CARRIER_TEXT = "com.android.keyguard.CarrierText";
    private static final Map<String, Float> CARRIER_SIZES_MAP = new HashMap<>();
    private static final Map<String, Integer> CLOCK_SIZES_MAP = new HashMap<>();
    private static final Map<String, String> FOUR_G_DATA_ICONS_MAP = new HashMap<>();
    private static final Map<String, String> FIVE_G_DATA_ICONS_MAP = new HashMap<>();

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
        FOUR_G_DATA_ICONS_MAP.put("0", "DEFAULT");
        FOUR_G_DATA_ICONS_MAP.put("1", LTE_INSTEAD_OF_4G);
        FOUR_G_DATA_ICONS_MAP.put("2", FOUR_G_PLUS_INSTEAD_OF_4G);
        FOUR_G_DATA_ICONS_MAP.put("3", FOUR_HALF_G_INSTEAD_OF_4G_PLUS);
        FIVE_G_DATA_ICONS_MAP.put("0", "DEFAULT");
        FIVE_G_DATA_ICONS_MAP.put("1", FIVE_G_ONE_SHAPED_ICON);
        FIVE_G_DATA_ICONS_MAP.put("2", FIVE_G_PLUS_ICON);
    }

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        final Class<?> mobileDataUtilClass = XposedHelpers.findClass(MOBILE_DATA_UTIL, classLoader);

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

        String behaviorIndex4g = prefs.getString(PREF_4G_DATA_ICON_BEHAVIOR, "0");
        String dataBehavior4g = get4gDataIconBehavior(behaviorIndex4g);
        if (!dataBehavior4g.equals("DEFAULT")) {
            changeDataIcon(mobileDataUtilClass, dataBehavior4g);
        }

        String behaviorIndex5g = prefs.getString(PREF_5G_DATA_ICON_BEHAVIOR, "0");
        String dataBehavior5g = get5gDataIconBehavior(behaviorIndex5g);
        if (!dataBehavior5g.equals("DEFAULT")) {
            changeDataIcon(mobileDataUtilClass, dataBehavior5g);
        }
    }

    private static void changeDataIcon(Class<?> aClass, String dataIconBehavior) {
        try {
            XposedHelpers.findAndHookMethod(aClass,
                    dataIconBehavior,
                    int.class,
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

    private static String get4gDataIconBehavior(String behaviorIndex) {
        return FOUR_G_DATA_ICONS_MAP.get(behaviorIndex);
    }

    private static String get5gDataIconBehavior(String behaviorIndex) {
        return FIVE_G_DATA_ICONS_MAP.get(behaviorIndex);
    }
}

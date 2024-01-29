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
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetBooleanPref;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetStringPref;
import static sb.firefds.u.firefdskit.utils.Packages.SYSTEM_UI;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_4G_DATA_ICON_BEHAVIOR;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_5G_DATA_ICON_BEHAVIOR;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_CARRIER_SIZE;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_HIDE_CARRIER_LABEL;

import android.telephony.TelephonyManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;

public class XSysUINotificationPanelPackage {

    private static final String LTE = "LTE";
    private static final String LTE_PLUS = "LTE_PLUS";
    private static final String FOUR_G_PLUS = "FOUR_G_PLUS";
    private static final String FOUR_HALF_G = "FOUR_HALF_G";
    private static final String NR_5G_CONNECTED = "NR_5G_CONNECTED";
    private static final String NR_5G = "NR_5G";
    private static final String NR_5G_PLUS = "NR_5G_PLUS";
    private static final String CARRIER_TEXT_CONTROLLER$1 = "com.android.keyguard.CarrierTextController$1";
    private static final String CARRIER_TEXT_CALLBACK_INFO = "com.android.keyguard.CarrierTextManager.CarrierTextCallbackInfo";
    private static final String SHADE_CARRIER_GROUP_CONTROLLER = SYSTEM_UI +
                                                                 ".shade.carrier.ShadeCarrierGroupController";
    private static final String MOBILE_ICON_INTERACTOR_IMPL$UPDATED_MOBILE_ICON_MAPPING$1 = SYSTEM_UI +
                                                                                            ".statusbar.pipeline.mobile" +
                                                                                            ".domain.interactor" +
                                                                                            ".MobileIconInteractorImpl$updatedMobileIconMapping$1";
    private static final String TELEPHONY_ICONS = "com.android.settingslib.mobile.TelephonyIcons";
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
        FOUR_G_DATA_ICONS_MAP.put("1", LTE);
        FOUR_G_DATA_ICONS_MAP.put("2", FOUR_G_PLUS);
        FOUR_G_DATA_ICONS_MAP.put("3", FOUR_HALF_G);
        FIVE_G_DATA_ICONS_MAP.put("0", "DEFAULT");
        FIVE_G_DATA_ICONS_MAP.put("1", NR_5G_CONNECTED);
        FIVE_G_DATA_ICONS_MAP.put("2", NR_5G);
        FIVE_G_DATA_ICONS_MAP.put("3", NR_5G_PLUS);
    }

    public static void doHook(ClassLoader classLoader) {

        try {
            findAndHookMethod(SHADE_CARRIER_GROUP_CONTROLLER,
                              classLoader,
                              "handleUpdateState",
                              carrierTextStatusBarHook());

            findAndHookMethod(CARRIER_TEXT_CONTROLLER$1,
                              classLoader,
                              "updateCarrierInfo",
                              CARRIER_TEXT_CALLBACK_INFO,
                              carrierTextKeyguardHook());

            findAndHookMethod(CARRIER_TEXT,
                              classLoader,
                              "setTextSize",
                              int.class,
                              float.class,
                              carrierTextSizeKeyguardHook());
        } catch (Throwable e) {
            log(e);
        }

        try {
            final Class<?> telephonyIconsClass = findClass(TELEPHONY_ICONS, classLoader);

            findAndHookMethod(MOBILE_ICON_INTERACTOR_IMPL$UPDATED_MOBILE_ICON_MAPPING$1,
                              classLoader,
                              "invokeSuspend",
                              Object.class,
                              new XC_MethodHook() {


                                  @SuppressWarnings("unchecked")
                                  @Override
                                  protected void afterHookedMethod(MethodHookParam param) {
                                      HashMap<String, Object> hashMap = (HashMap<String, Object>) param.getResult();
                                      set4gDataIcon(hashMap, telephonyIconsClass);

                                      set5gDataIcon(hashMap, telephonyIconsClass);
                                  }
                              });
        } catch (Throwable e) {
            log(e);
        }
    }

    private static void set5gDataIcon(HashMap<String, Object> hashMap, Class<?> telephonyIconsClass) {
        String behaviorIndex5g = reloadAndGetStringPref(PREF_5G_DATA_ICON_BEHAVIOR, "0");
        String dataBehavior5g = FIVE_G_DATA_ICONS_MAP.get(behaviorIndex5g);
        String NETWORK_TYPE_NR = Integer.toString(TelephonyManager.NETWORK_TYPE_NR);

        switch (Objects.requireNonNull(dataBehavior5g)) {
            case NR_5G_CONNECTED:
                Object fiveGConnected = getStaticObjectField(telephonyIconsClass, NR_5G_CONNECTED);
                hashMap.put(NETWORK_TYPE_NR, fiveGConnected);
                break;
            case NR_5G:
                Object fiveG = getStaticObjectField(telephonyIconsClass, NR_5G);
                hashMap.put(NETWORK_TYPE_NR, fiveG);
                break;
            case NR_5G_PLUS:
                Object fiveGPlus = getStaticObjectField(telephonyIconsClass, NR_5G_PLUS);
                hashMap.put(NETWORK_TYPE_NR, fiveGPlus);
                break;
        }
    }

    private static void set4gDataIcon(HashMap<String, Object> hashMap, Class<?> telephonyIconsClass) {
        String behaviorIndex4g = reloadAndGetStringPref(PREF_4G_DATA_ICON_BEHAVIOR, "0");
        String dataBehavior4g = FOUR_G_DATA_ICONS_MAP.get(behaviorIndex4g);
        String NETWORK_TYPE_LTE = Integer.toString(TelephonyManager.NETWORK_TYPE_LTE);
        String NETWORK_TYPE_LTE_PLUS = NETWORK_TYPE_LTE + "CA_Plus";

        switch (Objects.requireNonNull(dataBehavior4g)) {
            case LTE:
                Object lte = getStaticObjectField(telephonyIconsClass, LTE);
                Object ltePlus = getStaticObjectField(telephonyIconsClass, LTE_PLUS);
                hashMap.put(NETWORK_TYPE_LTE, lte);
                hashMap.put(NETWORK_TYPE_LTE_PLUS, ltePlus);
                break;
            case FOUR_G_PLUS:
                Object fourGPlus = getStaticObjectField(telephonyIconsClass, FOUR_G_PLUS);
                hashMap.put(NETWORK_TYPE_LTE, fourGPlus);
                hashMap.put(NETWORK_TYPE_LTE_PLUS, fourGPlus);
                break;
            case FOUR_HALF_G:
                Object fourHalfG = getStaticObjectField(telephonyIconsClass, FOUR_HALF_G);
                hashMap.put(NETWORK_TYPE_LTE_PLUS, fourHalfG);
                break;
        }
    }

    @NonNull
    private static XC_MethodHook carrierTextSizeKeyguardHook() {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.args[1] = getCarrierSizeValue(reloadAndGetStringPref(PREF_CARRIER_SIZE, "Small"));
            }
        };
    }

    @NonNull
    private static XC_MethodHook carrierTextKeyguardHook() {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (reloadAndGetBooleanPref(PREF_HIDE_CARRIER_LABEL, false)) {
                    setObjectField(param.args[0], "carrierText", " ");
                }
            }
        };
    }

    @NonNull
    private static XC_MethodHook carrierTextStatusBarHook() {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Object[] mCarrierGroups = (Object[]) getObjectField(param.thisObject, "mCarrierGroups");
                TextView mCarrierText = (TextView) getObjectField(mCarrierGroups[0], "mCarrierText");
                if (reloadAndGetBooleanPref(PREF_HIDE_CARRIER_LABEL, false)) {
                    mCarrierText.setText(" ");
                } else {
                    mCarrierText.setTextSize(getClockSizeValue(reloadAndGetStringPref(PREF_CARRIER_SIZE, "Small")));
                }
            }
        };
    }

    public static Float getCarrierSizeValue(String sizeName) {
        return CARRIER_SIZES_MAP.get(sizeName);
    }

    public static Integer getClockSizeValue(String sizeName) {
        return CLOCK_SIZES_MAP.get(sizeName);
    }
}

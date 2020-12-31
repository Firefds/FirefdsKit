/*
 * Copyright (C) 2020 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.r.firefdskit;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.r.firefdskit.utils.Packages.SAMSUNG_MESSAGING;
import static sb.firefds.r.firefdskit.utils.Preferences.PREF_DISABLE_SMS_TO_MMS;
import static sb.firefds.r.firefdskit.utils.Preferences.PREF_ENABLE_BLOCKED_PHRASES;
import static sb.firefds.r.firefdskit.utils.Preferences.PREF_FORCE_MMS_CONNECT;

public class XMessagingPackage {

    private static final String FEATURE = SAMSUNG_MESSAGING + ".common.configuration.Feature";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        boolean disableSmsToMms = prefs.getBoolean(PREF_DISABLE_SMS_TO_MMS, false);
        final Class<?> messagingFeatureClass = XposedHelpers.findClass(FEATURE, classLoader);

        if (prefs.getBoolean(PREF_ENABLE_BLOCKED_PHRASES, false)) {
            try {
                XposedHelpers.findAndHookMethod(messagingFeatureClass,
                        "getEnableSpamReport4Kor",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));

                XposedHelpers.findAndHookMethod(messagingFeatureClass,
                        "isKorModel",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        try {
            XposedHelpers.findAndHookMethod(messagingFeatureClass,
                    "getSupportMMSThroughWifi",
                    XC_MethodReplacement.returnConstant(prefs.getBoolean(PREF_FORCE_MMS_CONNECT, false)));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(messagingFeatureClass,
                    "getSmsToMmsByThreshold",
                    XC_MethodReplacement.returnConstant(!disableSmsToMms));
        } catch (Throwable e) {
            try {
                XposedHelpers.findAndHookMethod(messagingFeatureClass,
                        "getSmsToMmsByThreshold",
                        int.class,
                        int.class,
                        XC_MethodReplacement.returnConstant(!disableSmsToMms));
            } catch (Throwable e1) {
                XposedBridge.log(e1);
            }
        }

        try {
            XposedHelpers.findAndHookMethod(messagingFeatureClass,
                    "getSmsMaxByte",
                    XC_MethodReplacement.returnConstant(disableSmsToMms ? 999 : 140));

            XposedHelpers.findAndHookMethod(messagingFeatureClass,
                    "getMaxPhoneNumberLength",
                    XC_MethodReplacement.returnConstant(999));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

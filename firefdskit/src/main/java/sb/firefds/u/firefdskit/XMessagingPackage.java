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
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetBooleanPref;
import static sb.firefds.u.firefdskit.utils.Packages.SAMSUNG_MESSAGING;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_SMS_TO_MMS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_BLOCKED_PHRASES;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_FORCE_MMS_CONNECT;

import de.robv.android.xposed.XC_MethodReplacement;

public class XMessagingPackage {

    private static final String FEATURE = SAMSUNG_MESSAGING + ".common.configuration.Feature";

    public static void doHook(ClassLoader classLoader) {

        final Class<?> messagingFeatureClass = findClass(FEATURE, classLoader);

        try {
            findAndHookMethod(messagingFeatureClass,
                              "getEnableSpamReport4Kor",
                              XC_MethodReplacement.returnConstant(reloadAndGetBooleanPref(PREF_ENABLE_BLOCKED_PHRASES,
                                                                                          false)));
        } catch (Throwable e) {
            log(e);
        }

        try {
            findAndHookMethod(messagingFeatureClass,
                              "getSupportMMSThroughWifi",
                              XC_MethodReplacement.returnConstant(reloadAndGetBooleanPref(PREF_FORCE_MMS_CONNECT,
                                                                                          false)));
        } catch (Throwable e) {
            log(e);
        }

        try {
            findAndHookMethod(messagingFeatureClass,
                              "getSmsToMmsByThreshold",
                              int.class,
                              int.class,
                              XC_MethodReplacement.returnConstant(!reloadAndGetBooleanPref(PREF_DISABLE_SMS_TO_MMS,
                                                                                           false)));
        } catch (Throwable e1) {
            log(e1);
        }

        try {
            findAndHookMethod(messagingFeatureClass,
                              "getSmsMaxByte",
                              XC_MethodReplacement.returnConstant(reloadAndGetBooleanPref(PREF_DISABLE_SMS_TO_MMS,
                                                                                          false) ? 999 : 140));

            findAndHookMethod(messagingFeatureClass,
                              "getMaxPhoneNumberLength",
                              XC_MethodReplacement.returnConstant(999));
        } catch (Throwable e) {
            log(e);
        }
    }
}

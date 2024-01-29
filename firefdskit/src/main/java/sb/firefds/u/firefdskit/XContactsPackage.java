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
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_NUMBER_FORMATTING;

import de.robv.android.xposed.XC_MethodReplacement;

public class XContactsPackage {
    private static final String CSC_FEATURE_UTIL = "com.samsung.android.dialtacts.util.CscFeatureUtil";

    public static void doHook(ClassLoader classLoader) {

        try {
            findAndHookMethod(CSC_FEATURE_UTIL,
                              classLoader,
                              "getDisablePhoneNumberFormatting",
                              XC_MethodReplacement.returnConstant(reloadAndGetBooleanPref(PREF_DISABLE_NUMBER_FORMATTING,
                                                                                          false)));
        } catch (Throwable e) {
            log(e);
        }
    }
}

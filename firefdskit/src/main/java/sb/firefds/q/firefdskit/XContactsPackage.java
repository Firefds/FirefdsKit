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
package sb.firefds.q.firefdskit;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DISABLE_NUMBER_FORMATTING;

public class XContactsPackage {
    private static final String CSC_FEATURE_UTIL = "com.samsung.android.dialtacts.util.CscFeatureUtil";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        try {
            XposedHelpers.findAndHookMethod(CSC_FEATURE_UTIL,
                    classLoader,
                    "getDisablePhoneNumberFormatting",
                    XC_MethodReplacement.returnConstant(prefs
                            .getBoolean(PREF_DISABLE_NUMBER_FORMATTING, false)));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

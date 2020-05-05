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
package sb.firefds.pie.firefdskit;

import android.content.Context;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_SECURE_FOLDER;

public class XSecureFolder {

    private static final String SETUP_WIZARD_UTILS = "com.samsung.knox.securefolder.setupwizard.Utils";

    public static void doHook(XSharedPreferences prefs, ClassLoader classloader) {

        try {
            if (prefs.getBoolean(PREF_ENABLE_SECURE_FOLDER, false)) {
                XposedHelpers.findAndHookMethod(SETUP_WIZARD_UTILS,
                        classloader,
                        "isDeviceTrustable",
                        Context.class,
                        XC_MethodReplacement.returnConstant(true));
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }
}

/*
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Modifications Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
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

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Packages;

public class XSysUIGlobalActions {

    private final static String GLOBAL_ACTION_FEATURES_CLASS =
            Packages.SYSTEM_UI + ".globalactions.presentation.features.GlobalActionFeatures";

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(GLOBAL_ACTION_FEATURES_CLASS,
                    classLoader,
                    "isDataModeSupported",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            prefs.reload();
                            return prefs.getBoolean("enableDataModeSwitch", false);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
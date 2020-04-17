/*
 * Copyright (C) 2020 Shauli Bracha for FirefdsKit Project (firefds@xda)
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


import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_MAKE_OFFICIAL;

public class XFotaAgentPackage {

    private static final String DEVICE_UTILS = "com.samsung.android.fem.common.util.DeviceUtils";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        try {
            if (prefs.getBoolean(PREF_MAKE_OFFICIAL, true)) {
                XposedHelpers.findAndHookMethod(DEVICE_UTILS,
                        classLoader,
                        "isRootingDevice",
                        boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                param.setResult(false);
                            }
                        });
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}


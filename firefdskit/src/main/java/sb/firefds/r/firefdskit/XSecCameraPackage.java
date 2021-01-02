/*
 * Copyright (C) 2021 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.r.firefdskit.utils.Packages.SAMSUNG_CAMERA;
import static sb.firefds.r.firefdskit.utils.Preferences.PREF_DISABLE_TEMPERATURE_CHECKS;
import static sb.firefds.r.firefdskit.utils.Preferences.PREF_ENABLE_CAMERA_SHUTTER_MENU;

public class XSecCameraPackage {

    private static final String FEATURE = SAMSUNG_CAMERA + ".feature.Feature";
    private static final String BOOLEAN_TAG = SAMSUNG_CAMERA + ".feature.BooleanTag";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        if (prefs.getBoolean(PREF_DISABLE_TEMPERATURE_CHECKS, false)) {
            try {
                XposedHelpers.findAndHookMethod(FEATURE,
                        classLoader,
                        "get",
                        BOOLEAN_TAG,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                String booleanTagName = (String) XposedHelpers.callMethod(param.args[0], "name");
                                if (booleanTagName.equals("SUPPORT_THERMISTOR_TEMPERATURE")) {
                                    param.setResult(false);
                                }
                            }
                        });
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        if (prefs.getBoolean(PREF_ENABLE_CAMERA_SHUTTER_MENU, false)) {
            try {
                XposedHelpers.findAndHookMethod(FEATURE,
                        classLoader,
                        "get",
                        BOOLEAN_TAG,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                String booleanTagName = (String) XposedHelpers.callMethod(param.args[0], "name");
                                if (booleanTagName.equals("SUPPORT_SHUTTER_SOUND_MENU")) {
                                    param.setResult(true);
                                }
                            }
                        });
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }
}

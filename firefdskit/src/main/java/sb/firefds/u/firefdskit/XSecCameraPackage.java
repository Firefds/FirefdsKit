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
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetBooleanPref;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_TEMPERATURE_CHECKS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_CAMERA_SHUTTER_MENU;

import de.robv.android.xposed.XC_MethodHook;

public class XSecCameraPackage {

    private static final String FEATURE = "d4.c";
    private static final String BOOLEAN_TAG = "d4.b";

    public static void doHook(ClassLoader classLoader) {

        try {
            findAndHookMethod(FEATURE, classLoader, "e", BOOLEAN_TAG, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DISABLE_TEMPERATURE_CHECKS, false)) {
                        String booleanTagName = (String) callMethod(param.args[0], "name");
                        if (booleanTagName.equals("SUPPORT_THERMISTOR_TEMPERATURE")) {
                            param.setResult(false);
                        }
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }

        try {
            findAndHookMethod(FEATURE, classLoader, "e", BOOLEAN_TAG, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_ENABLE_CAMERA_SHUTTER_MENU, false)) {
                        String booleanTagName = (String) callMethod(param.args[0], "name");
                        if (booleanTagName.equals("SUPPORT_SHUTTER_SOUND_MENU")) {
                            param.setResult(true);
                        }
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }
    }
}

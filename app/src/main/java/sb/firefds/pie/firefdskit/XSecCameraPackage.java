/*
 * Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
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
import sb.firefds.pie.firefdskit.utils.Packages;

public class XSecCameraPackage {


    public static void doHook(final XSharedPreferences prefs, ClassLoader classLoader) {

        final Class<?> cameraFeatureClass
                = XposedHelpers.findClass(Packages.SAMSUNG_CAMERA + ".feature.Feature", classLoader);

        if (prefs.getBoolean("disableTemperatureChecks", false)) {
            try {
                XposedHelpers.findAndHookMethod(Packages.CAMERA + ".provider.CameraTemperatureManager",
                        classLoader,
                        "start",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                XposedHelpers.setStaticBooleanField(cameraFeatureClass,
                                        "SUPPORT_THERMISTOR_TEMPERATURE", false);
                            }
                        });
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }

        try {
            XposedHelpers.findAndHookMethod(Packages.CAMERA + ".setting.PreferenceSettingFragment",
                    classLoader,
                    "updateFeaturedPreference",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            prefs.reload();
                            XposedHelpers.setStaticBooleanField(cameraFeatureClass,
                                    "ENABLE_SHUTTER_SOUND_MENU",
                                    prefs.getBoolean("enableCameraShutterMenu", false));
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

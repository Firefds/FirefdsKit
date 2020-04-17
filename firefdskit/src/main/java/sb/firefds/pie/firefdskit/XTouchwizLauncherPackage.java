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

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_TRANSITION_EFFECT;

public class XTouchwizLauncherPackage {

    private static final String PAGE_TRANSITION_MANAGER =
            "com.android.launcher3.framework.view.features.pagetransition.PageTransitionManager";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {
        int transEffect = Integer.parseInt(prefs.getString(PREF_TRANSITION_EFFECT, "0"));
        if (transEffect != 0) {
            try {
                XposedHelpers.findAndHookMethod(PAGE_TRANSITION_MANAGER,
                        classLoader,
                        "setCurrentTransitionEffect",
                        int.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                param.args[0] = transEffect;
                            }
                        });

            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }
}

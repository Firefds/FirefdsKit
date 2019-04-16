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

package sb.firefds.oreo.firefdskit;

import android.graphics.Color;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.oreo.firefdskit.utils.Packages;

public class XSysUIFeaturePackage {

    private static Object mVolumePanel;

    public static void doHook(final ClassLoader classLoader) {


        try {
            Class<?> classVolumePanel = XposedHelpers.findClass(Packages.SYSTEM_UI + ".volume.SecVolumeDialogImpl", classLoader);

            XposedBridge.hookAllConstructors(classVolumePanel, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) {
                    mVolumePanel = param.thisObject;
                }
            });

            XposedHelpers.findAndHookMethod(classVolumePanel, "updateTintColor", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {

                    //if (prefs.getBoolean("semiTransparentVolumePanel", false)) {
                    XposedHelpers.setObjectField(
                            mVolumePanel,
                            "mVolumePanelBgColor",
                            XposedHelpers.callMethod(mVolumePanel, "colorToColorStateList",
                                    Color.parseColor("#55FF0000")));
                    XposedBridge.log(Packages.FIREFDSKIT + "Color set to #55FF0000");
                    //}
                }
            });

            XposedHelpers.findAndHookMethod(classVolumePanel, "updateDefaultTintColor", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {

                    //if (prefs.getBoolean("semiTransparentVolumePanel", false)) {
                    XposedHelpers.setObjectField(
                            mVolumePanel,
                            "mVolumePanelBgDefaultColor",
                            XposedHelpers.callMethod(mVolumePanel, "colorToColorStateList",
                                    Color.parseColor("#55FF0000")));
                    XposedBridge.log(Packages.FIREFDSKIT + "Color default set to #55FF0000");
                    //}
                }
            });
        } catch (Throwable e1) {
            XposedBridge.log(e1.getMessage());
        }
    }

}
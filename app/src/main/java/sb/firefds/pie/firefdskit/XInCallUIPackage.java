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

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Packages;

public class XInCallUIPackage {

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        try {
            XposedHelpers.findAndHookMethod(
                    Packages.INCALLUI + ".modelimpl.feature.function.VoiceRecordingFeatureImpl",
                    classLoader,
                    "isSupportVoiceRecording",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            if (prefs.getBoolean("enableCallAdd", false)
                                    && !prefs.getBoolean("enableAutoCallRecording", false)) {
                                return Boolean.FALSE;
                            } else {
                                return Boolean.TRUE;
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(
                    Packages.INCALLUI + ".modelimpl.feature.function.VoiceRecordingByMenuFeatureImpl",
                    classLoader,
                    "isSupportVoiceRecording",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            if (prefs.getBoolean("enableCallRecordingMenu", false)) {
                                return Boolean.TRUE;
                            } else {
                                return Boolean.FALSE;
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(
                    Packages.INCALLUI + ".modelimpl.feature.function.VoiceRecordingByButtonFeatureImpl",
                    classLoader,
                    "isSupportVoiceRecording",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            if (prefs.getBoolean("enableCallAdd", false)) {
                                return Boolean.FALSE;
                            } else {
                                return Boolean.TRUE;
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(
                    Packages.INCALLUI + ".modelimpl.callcontext.VoiceRecordingContextImpl",
                    classLoader,
                    "isForcedToAutoRecord",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            if (prefs.getBoolean("enableAutoCallRecording", false)) {
                                return Boolean.TRUE;
                            } else {
                                return Boolean.FALSE;
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

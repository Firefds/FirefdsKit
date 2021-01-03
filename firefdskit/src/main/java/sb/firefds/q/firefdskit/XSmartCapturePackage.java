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

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.q.firefdskit.utils.Preferences.PREF_ENABLE_SCREEN_RECORDER_IN_CALL;

public class XSmartCapturePackage {

    private static final String RECORDING_STOP_REASON = "com.samsung.android.app.screenrecorder" +
            ".ScreenRecorderController.RecordingStopReason";
    private static final String SCREEN_RECORDER_CONTROLLER = "com.samsung.android.app.screenrecorder" +
            ".ScreenRecorderController";
    private static final String SCREEN_RECORDER_CONTROLLER$1 = "com.samsung.android.app.screenrecorder" +
            ".ScreenRecorderController$1";
    private static final String SCREEN_RECORDER_UTILS = "com.samsung.android.app.screenrecorder.util.Utils";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        if (prefs.getBoolean(PREF_ENABLE_SCREEN_RECORDER_IN_CALL, false)) {
            try {
                XposedHelpers.findAndHookMethod(SCREEN_RECORDER_CONTROLLER$1,
                        classLoader,
                        "onCallStateChanged",
                        int.class,
                        String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                param.setResult(null);
                            }
                        });

                Class<?> recordingStopReason = XposedHelpers.findClass(RECORDING_STOP_REASON, classLoader);
                XposedHelpers.findAndHookMethod(SCREEN_RECORDER_CONTROLLER,
                        classLoader,
                        "stopRecordingAccordingToAction",
                        recordingStopReason,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (((Enum<?>) param.args[0]).name().equalsIgnoreCase("INCOMING_CALL")) {
                                    param.setResult(null);
                                }
                            }
                        });

                XposedHelpers.findAndHookMethod(SCREEN_RECORDER_UTILS,
                        classLoader,
                        "isDuringCallState",
                        Context.class,
                        XC_MethodReplacement.returnConstant(Boolean.FALSE));

                XposedHelpers.findAndHookMethod(SCREEN_RECORDER_UTILS,
                        classLoader,
                        "isReceivingCallState",
                        Context.class,
                        XC_MethodReplacement.returnConstant(Boolean.FALSE));
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }
    }
}

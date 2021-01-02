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

import static sb.firefds.r.firefdskit.utils.Packages.MTP_APPLICATION;
import static sb.firefds.r.firefdskit.utils.Preferences.PREF_HIDE_MTP_NOTIFICATION;

public class XMtpApplication {

    private static final String USB_CONNECTION = MTP_APPLICATION + ".USBConnection";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        if (prefs.getBoolean(PREF_HIDE_MTP_NOTIFICATION, false)) {
            try {
                XposedHelpers.findAndHookMethod(USB_CONNECTION,
                        classLoader,
                        "showDiaglog",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Object mReceiver = XposedHelpers.getObjectField(param.thisObject, "mReceiver");
                                XposedHelpers.callMethod(mReceiver, "changeMtpMode");
                                XposedHelpers.callMethod(param.thisObject, "finish");
                            }
                        });
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }
}
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
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetBooleanPref;
import static sb.firefds.u.firefdskit.utils.Packages.MTP_APPLICATION;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_HIDE_MTP_NOTIFICATION;

import de.robv.android.xposed.XC_MethodHook;

public class XMtpApplication {

    private static final String USB_CONNECTION = MTP_APPLICATION + ".USBConnection";

    public static void doHook(ClassLoader classLoader) {

        try {
            findAndHookMethod(USB_CONNECTION, classLoader, "showDiaglog", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_HIDE_MTP_NOTIFICATION, false)) {
                        Object mReceiver = getObjectField(param.thisObject, "mReceiver");
                        callMethod(mReceiver, "changeMtpMode");
                        callMethod(param.thisObject, "finish");
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }
    }
}
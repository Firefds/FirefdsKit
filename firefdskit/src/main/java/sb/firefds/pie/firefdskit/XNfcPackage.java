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

import static sb.firefds.pie.firefdskit.utils.Preferences.*;

@SuppressWarnings("SynchronizeOnNonFinalField")
public class XNfcPackage {

    private static final int SCREEN_STATE_ON_LOCKED = 4;
    private static final int SCREEN_STATE_ON_UNLOCKED = 8;
    private static String behavior;

    private static final String NFC_SERVICE = Packages.NFC + ".NfcService";

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        try {
            XposedHelpers.findAndHookMethod(NFC_SERVICE,
                    classLoader,
                    "showIcon",
                    boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {

                            if ((Boolean) XposedHelpers.callMethod(param.thisObject,
                                    "isNfcEnabled")) {
                                prefs.reload();
                                param.args[0] =
                                        !prefs.getBoolean(PREF_HIDE_NFC_ICON, false);
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(NFC_SERVICE,
                    classLoader,
                    "applyRouting",
                    boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            behavior = prefs.getString(PREF_NFC_BEHAVIOR, "0");
                            if (behavior.equals("0")) {
                                return;
                            }
                            try {
                                final int currScreenState;
                                final Object mScreenStateHelper = XposedHelpers
                                        .getObjectField(param.thisObject, "mScreenStateHelper");
                                if (mScreenStateHelper != null) {
                                    currScreenState = (Integer) XposedHelpers.callMethod(mScreenStateHelper, "checkScreenState");
                                } else {
                                    currScreenState = (Integer) XposedHelpers.callMethod(param.thisObject, "checkScreenState");
                                }
                                if ((currScreenState == SCREEN_STATE_ON_UNLOCKED)
                                        || (behavior.equals("1") && currScreenState != SCREEN_STATE_ON_LOCKED)) {
                                    XposedHelpers.setAdditionalInstanceField(param.thisObject,
                                            "mOrigScreenState",
                                            -1);
                                    return;
                                }

                                synchronized (param.thisObject) {
                                    XposedHelpers.setAdditionalInstanceField(param.thisObject,
                                            "mOrigScreenState",
                                            XposedHelpers
                                                    .getIntField(param.thisObject, "mScreenState"));
                                    XposedHelpers.setIntField(param.thisObject,
                                            "mScreenState",
                                            SCREEN_STATE_ON_UNLOCKED);
                                }
                            } catch (Exception e) {
                                XposedBridge.log(e);
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (behavior.equals("0")) {
                                return;
                            }

                            final int mOrigScreenState = (Integer) XposedHelpers
                                    .getAdditionalInstanceField(param.thisObject, "mOrigScreenState");
                            if (mOrigScreenState == -1)
                                return;

                            synchronized (param.thisObject) {
                                XposedHelpers.setIntField(param.thisObject,
                                        "mScreenState",
                                        mOrigScreenState);
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

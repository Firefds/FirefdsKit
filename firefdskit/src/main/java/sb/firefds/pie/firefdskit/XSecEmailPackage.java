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

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Packages;

import static sb.firefds.pie.firefdskit.utils.Preferences.*;

public class XSecEmailPackage {

    private static final String POLICY_SET = "com.samsung.android.emailcommon.service.PolicySet";
    private static final String SECURITY_POLICY = Packages.EMAIL + ".SecurityPolicy";
    private static final String ACCOUNT = "com.samsung.android.emailcommon.Account";
    private static final String SETUP_DATA = "com.samsung.android.email.ui.settings.setup.SetupData";

    private static ClassLoader classLoader;

    public static void doHook(final XSharedPreferences prefs, ClassLoader classLoader) {

        XSecEmailPackage.classLoader = classLoader;

        if (prefs.getBoolean(PREF_DISABLE_EXCHANGE_SECURITY, false)) {
            try {
                disableExchangeLockSecurity();
            } catch (Throwable e) {
                XposedBridge.log(e);

            }
        }
    }

    private static void setPolicySets(MethodHookParam param) {
        disableAdmin(param.args[0]);
    }

    private static void disableAdmin(Object param) {
        XposedHelpers.setBooleanField(param, "mAllowBrowser", true);
        XposedHelpers.setBooleanField(param, "mAllowCamera", true);
        XposedHelpers.setBooleanField(param, "mAllowDesktopSync", true);
        XposedHelpers.setBooleanField(param, "mAllowHTMLEmail", true);
        XposedHelpers.setBooleanField(param, "mAllowInternetSharing", true);
        XposedHelpers.setBooleanField(param, "mAllowIrDA", true);
        XposedHelpers.setBooleanField(param, "mAllowPOPIMAPEmail", true);
        XposedHelpers.setBooleanField(param, "mAllowSMIMESoftCerts", true);
        XposedHelpers.setBooleanField(param, "mAllowStorageCard", true);
        XposedHelpers.setBooleanField(param, "mAllowTextMessaging", true);
        XposedHelpers.setBooleanField(param, "mAllowUnsignedApp", true);
        XposedHelpers.setBooleanField(param, "mAllowUnsignedInstallationPkg", true);
        XposedHelpers.setBooleanField(param, "mAllowWifi", true);
        XposedHelpers.setBooleanField(param, "mAttachmentsEnabled", true);
        XposedHelpers.setBooleanField(param, "mDeviceEncryptionEnabled", false);
        XposedHelpers.setBooleanField(param, "mPasswordRecoverable", true);
        XposedHelpers.setBooleanField(param, "mRequireEncryptedSMIMEMessages", false);
        XposedHelpers.setBooleanField(param, "mRequireEncryption", false);
        XposedHelpers.setBooleanField(param, "mRequireManualSyncWhenRoaming", false);
        XposedHelpers.setBooleanField(param, "mRequireRemoteWipe", false);
        XposedHelpers.setBooleanField(param, "mRequireSignedSMIMEMessages", false);
        XposedHelpers.setBooleanField(param, "mSimplePasswordEnabled", false);
        XposedHelpers.setIntField(param, "mPasswordMode", 0);
        XposedHelpers.setIntField(param, "mRequireEncryptionSMIMEAlgorithm", 0);
    }

    private static void disableExchangeLockSecurity() {

        try {
            Class<?> policySet = XposedHelpers.findClass(POLICY_SET, classLoader);
            XposedHelpers.findAndHookMethod(SECURITY_POLICY,
                    classLoader,
                    "isActive",
                    Context.class,
                    policySet,
                    XC_MethodReplacement.returnConstant(Boolean.TRUE));

            XposedHelpers.findAndHookMethod(ACCOUNT,
                    classLoader,
                    "setPolicySet",
                    policySet,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            setPolicySets(param);
                        }
                    });

            XposedHelpers.findAndHookMethod(SETUP_DATA,
                    classLoader,
                    "setPolicySet",
                    policySet,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            setPolicySets(param);
                        }
                    });

            XposedBridge.hookAllConstructors(policySet, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    disableAdmin(param.thisObject);
                }
            });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

    }
}

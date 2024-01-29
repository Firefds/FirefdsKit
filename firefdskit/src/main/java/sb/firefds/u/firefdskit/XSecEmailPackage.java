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

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetBooleanPref;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_EXCHANGE_SECURITY;

import android.content.Context;

import androidx.annotation.NonNull;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import sb.firefds.u.firefdskit.utils.Packages;

public class XSecEmailPackage {

    private static final String POLICY_SET = "com.samsung.android.emailcommon.service.PolicySet";
    private static final String SECURITY_POLICY = Packages.EMAIL + ".SecurityPolicy";
    private static final String ACCOUNT = "com.samsung.android.emailcommon.Account";
    private static final String SETUP_DATA = "com.samsung.android.email.ui.settings.setup.SetupData";

    private static ClassLoader classLoader;

    public static void doHook(ClassLoader classLoader) {

        XSecEmailPackage.classLoader = classLoader;

        try {
            disableExchangeLockSecurity();
        } catch (Throwable e) {
            log(e);

        }
    }

    private static void setPolicySets(@NonNull MethodHookParam param) {
        disableAdmin(param.args[0]);
    }

    private static void disableAdmin(Object param) {
        setBooleanField(param, "mAllowBrowser", true);
        setBooleanField(param, "mAllowCamera", true);
        setBooleanField(param, "mAllowDesktopSync", true);
        setBooleanField(param, "mAllowHTMLEmail", true);
        setBooleanField(param, "mAllowInternetSharing", true);
        setBooleanField(param, "mAllowIrDA", true);
        setBooleanField(param, "mAllowPOPIMAPEmail", true);
        setBooleanField(param, "mAllowSMIMESoftCerts", true);
        setBooleanField(param, "mAllowStorageCard", true);
        setBooleanField(param, "mAllowTextMessaging", true);
        setBooleanField(param, "mAllowUnsignedApp", true);
        setBooleanField(param, "mAllowUnsignedInstallationPkg", true);
        setBooleanField(param, "mAllowWifi", true);
        setBooleanField(param, "mAttachmentsEnabled", true);
        setBooleanField(param, "mDeviceEncryptionEnabled", false);
        setBooleanField(param, "mPasswordRecoverable", true);
        setBooleanField(param, "mRequireEncryptedSMIMEMessages", false);
        setBooleanField(param, "mRequireEncryption", false);
        setBooleanField(param, "mRequireManualSyncWhenRoaming", false);
        setBooleanField(param, "mRequireRemoteWipe", false);
        setBooleanField(param, "mRequireSignedSMIMEMessages", false);
        setBooleanField(param, "mSimplePasswordEnabled", false);
        setIntField(param, "mPasswordMode", 0);
        setIntField(param, "mRequireEncryptionSMIMEAlgorithm", 0);
    }

    private static void disableExchangeLockSecurity() {

        try {
            Class<?> policySet = findClass(POLICY_SET, classLoader);
            findAndHookMethod(SECURITY_POLICY,
                              classLoader,
                              "isActive",
                              Context.class,
                              policySet,
                              XC_MethodReplacement.returnConstant(reloadAndGetBooleanPref(PREF_DISABLE_EXCHANGE_SECURITY,
                                                                                          false)));

            findAndHookMethod(ACCOUNT, classLoader, "setPolicySet", policySet, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DISABLE_EXCHANGE_SECURITY, false)) {
                        setPolicySets(param);
                    }
                }
            });

            findAndHookMethod(SETUP_DATA, classLoader, "setPolicySet", policySet, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DISABLE_EXCHANGE_SECURITY, false)) {
                        setPolicySets(param);
                    }
                }
            });

            hookAllConstructors(policySet, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DISABLE_EXCHANGE_SECURITY, false)) {
                        disableAdmin(param.thisObject);
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }

    }
}

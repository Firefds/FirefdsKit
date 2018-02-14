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

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import sb.firefds.oreo.firefdskit.utils.Packages;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSecEmailPackage {

	private static ClassLoader classLoader;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XSecEmailPackage.classLoader = classLoader;

		if (prefs.getBoolean("disableExchangeLockSecurity", false)) {
			try {
				disableExchangeLockSecurity();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

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
			Class<?> policySet = XposedHelpers.findClass("com.android.emailcommon.service.PolicySet",
					classLoader);
			XposedHelpers.findAndHookMethod(Packages.EMAIL + ".SecurityPolicy", classLoader, "isActive", policySet,
					new XC_MethodReplacement() {

						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							return Boolean.valueOf(true);
						}
					});

			XposedHelpers.findAndHookMethod(Packages.EMAIL +".Account", classLoader, "setPolicySet",
					policySet, new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							setPolicySets(param);
						}

					});

			XposedHelpers.findAndHookMethod(Packages.EMAIL +".activity.setup.SetupData", classLoader,
					"setPolicySet", policySet, new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							setPolicySets(param);
						}

					});

			XposedBridge.hookAllConstructors(policySet, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					disableAdmin(param.thisObject);
				}

			});
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}
}

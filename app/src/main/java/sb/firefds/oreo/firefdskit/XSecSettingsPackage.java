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

//import com.samsung.android.app.SemColorPickerDialog;

import android.content.Context;
import android.content.ContentResolver;
//import android.content.res.Resources;
import android.os.Bundle;
//import android.view.View;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.oreo.firefdskit.utils.Packages;

public class XSecSettingsPackage {

	private static ClassLoader classLoader;
	//private static SemColorPickerDialog semColorPickerDialog;
	//private static Context mContext;
	//private static int[] colorArray;


	public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

		XSecSettingsPackage.classLoader = classLoader;

		try {
			XposedHelpers.findAndHookMethod(Packages.SAMSUNG_SETTINGS + ".bluetooth.BluetoothScanDialog", classLoader,
					"onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					if (prefs.getBoolean("disableBluetoothScanDialog", false))
						((android.app.Activity) param.thisObject).finish();
				}
			});

		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		if (prefs.getBoolean("disableTetherProvisioning", false)) {
			try {
				disableTetherProvisioning();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

		/*try {
			final Class<?> NavigationbarColorPreference = XposedHelpers.findClass(Packages.SAMSUNG_SETTINGS + ".navigationbar.NavigationbarColorPreference", classLoader);

			XposedBridge.hookAllConstructors(NavigationbarColorPreference, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					if (prefs.getBoolean("navigationBarCustomColor", false)){
						mContext = (Context) param.args[0];
						Context gbContext = mContext.createPackageContext(Packages.FIREFDSKIT,
								Context.CONTEXT_IGNORE_SECURITY);
						Resources gbRes = gbContext.getResources();
						XposedHelpers.setObjectField(param.thisObject, "mColorPickerDialog", XposedHelpers.callMethod(param.thisObject, "getSemColorPickerDialog"));
						semColorPickerDialog=(SemColorPickerDialog) XposedHelpers.getObjectField(param.thisObject, "mColorPickerDialog");
						colorArray = gbRes.getIntArray(R.array.navigationbar_color_values);
						XposedHelpers.setObjectField(param.thisObject, "color_value",colorArray);
					}
				}
			});

			XposedHelpers.findAndHookMethod(Packages.SAMSUNG_SETTINGS + ".navigationbar.NavigationbarColorPreference.CustomGrid$1", classLoader,
					"onClick", View.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if (prefs.getBoolean("navigationBarCustomColor", false)){
						XposedHelpers.callMethod(semColorPickerDialog, "show");
						param.setResult(null);
					}
				}
			});

		} catch (Throwable e) {
			XposedBridge.log(e.toString());
		}*/

		try {
			XposedHelpers.findAndHookMethod(Packages.SAMSUNG_SETTINGS + ".qstile.SecAccountTiles", classLoader,
					"showConfirmPopup", boolean.class, new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							if (prefs.getBoolean("disableSyncDialog", false)){
								ContentResolver.setMasterSyncAutomatically((Boolean) param.args[0]);
								param.setResult(null);
							}
						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

	}

	private static void disableTetherProvisioning() {
		try {
			XposedHelpers.findAndHookMethod("com.android.settingslib.TetherUtil", classLoader, "isProvisioningNeeded",
					Context.class, XC_MethodReplacement.returnConstant(Boolean.FALSE));

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			XposedHelpers.findAndHookMethod(Packages.SAMSUNG_SETTINGS + ".wifi.mobileap.WifiApBroadcastReceiver", classLoader,
					"isProvisioningNeeded", Context.class, XC_MethodReplacement.returnConstant(Boolean.FALSE));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			XposedHelpers.findAndHookMethod(Packages.SAMSUNG_SETTINGS + ".wifi.mobileap.WifiApSwitchEnabler", classLoader,
					"isProvisioningNeeded", XC_MethodReplacement.returnConstant(Boolean.FALSE));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {

			XposedHelpers.findAndHookMethod(Packages.SAMSUNG_SETTINGS + ".wifi.mobileap.WifiApWarning", classLoader,
					"isProvisioningNeeded", XC_MethodReplacement.returnConstant(Boolean.FALSE));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			XSystemProp.set("net.tethering.noprovisioning", "false");
			XSystemProp.set("Provisioning.disable", "0");
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}
}

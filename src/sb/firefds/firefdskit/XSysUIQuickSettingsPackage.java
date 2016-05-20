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
package sb.firefds.firefdskit;

import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.firefdskit.utils.Packages;
import sb.firefds.firefdskit.utils.Utils;

public class XSysUIQuickSettingsPackage {

	private static ClassLoader classLoader;
	private static XSharedPreferences prefs;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XSysUIQuickSettingsPackage.classLoader = classLoader;
		XSysUIQuickSettingsPackage.prefs = prefs;

		if (prefs.getBoolean("qsTilesEnable", false)) {
			try {
				hideQuickSettingsTiles();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}
		else {
			if (prefs.getBoolean("addTorchTile", false)) {
				try {
					enableFlashlightTile();
				} catch (Throwable e) {
					XposedBridge.log(e.toString());

				}
			}
		}

		if (prefs.getBoolean("quickSettingsCollapseOnToggle", false)) {
			try {
				enableCollapseOnToggle();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

		if (prefs.getBoolean("disableSFinderQConnect", false)) {
			try {
				disableSFinderQConnect(prefs);
			} catch (Throwable e) {
				XposedBridge.log(e);

			}
		}
	}

	private static void enableFlashlightTile() {
		XposedHelpers.findAndHookMethod(Settings.System.class, "getStringForUser", ContentResolver.class, String.class, int.class,
				new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {

				if (param.args[1].equals("notification_panel_active_app_list"))
				{
					param.setResult(param.getResult().toString()+"TorchLight;");
				}
			}
		});
	}

	private static void hideQuickSettingsTiles() {
		XposedHelpers.findAndHookMethod(Settings.System.class, "getStringForUser", ContentResolver.class, String.class, int.class,
				new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {

				if (param.args[1].equals("notification_panel_active_app_list"))
				{
					Set<String> selections = XSysUIQuickSettingsPackage.prefs.getStringSet("qsTiles",null);
					String qsList = param.getResult().toString()+"TorchLight;";
					for(String s: selections){
						switch (Integer.valueOf(s))
						{
						case 0:
							qsList=(qsList.replace("Wifi;", ""));
							break;
						case 1:
							qsList=(qsList.replace("Location;", ""));
							break;
						case 2:
							qsList=(qsList.replace("SilentMode;", ""));
							break;
						case 3:
							qsList=(qsList.replace("AutoRotate;", ""));
							break;
						case 4:
							qsList=(qsList.replace("Bluetooth;", ""));
							break;
						case 5:
							qsList=(qsList.replace("MobileData;", ""));
							break;
						case 6:
							qsList=(qsList.replace("NetworkBooster;", ""));
							break;
						case 7:
							qsList=(qsList.replace("UltraPowerSaving;", ""));
							break;
						case 8:
							qsList=(qsList.replace("MultiWindow;", ""));
							break;
						case 9:
							qsList=(qsList.replace("Toolbox;", ""));
							break;
						case 10:
							qsList=(qsList.replace("WiFiHotspot;", ""));
							break;
						case 11:
							qsList=(qsList.replace("AllShareCast;", ""));
							break;
						case 12:
							qsList=(qsList.replace("Nfc;", ""));
							break;
						case 13:
							qsList=(qsList.replace("Sync;", ""));
							break;
						case 14:
							qsList=(qsList.replace("SmartStay;", ""));
							break;
						case 15:
							qsList=(qsList.replace("SmartPause;", ""));
							break;
						case 16:
							qsList=(qsList.replace("PowerSaving;", ""));
							break;
						case 17:
							qsList=(qsList.replace("DormantMode;", ""));
							break;
						case 18:
							qsList=(qsList.replace("AirplaneMode;", ""));
							break;
						case 19:
							qsList=(qsList.replace("CarMode;", ""));
							break;
						case 20:
							qsList=(qsList.replace("PersonalMode;", ""));
							break;
						case 21:
							qsList=(qsList.replace("TouchSensitivity;", ""));
							break;
						case 22:
							qsList=(qsList.replace("TorchLight;", ""));
							break;
						}
					}
					param.setResult(qsList);
				}
			}
		});
	}

	private static void enableCollapseOnToggle() {
		try {
			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".qs.QSTile", classLoader, "handleStateChanged",
					new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
					Utils.closeStatusBar(mContext);
				}
			});
		} catch (Throwable e) {
			XposedBridge.log(e);

		}
	}

	private static void disableSFinderQConnect(final XSharedPreferences prefs) {
		try {
			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.phone.PhoneStatusBar", classLoader,
					"showHideQConnectLayout", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					try {
						XposedHelpers.setBooleanField(param.thisObject, "mShowSFinderQConnectView", false);
					} catch (Throwable e) {
					}
				}

			});

		} catch (Throwable e) {
			// Not implemented
			XposedBridge.log(e);
		}

	}
}

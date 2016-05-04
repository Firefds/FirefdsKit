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
package sb.firefds.xtouchwizS5;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.xtouchwizS5.utils.Packages;

public class XSysUIAlarmPackage {

	private static ClassLoader classLoader;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XSysUIAlarmPackage.classLoader = classLoader;

		try {
			if (prefs.getBoolean("hideAlarmClockIcon", false)) {
				hideSmartAlarmIcon(prefs);
			}
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

	private static void hideSmartAlarmIcon(final XSharedPreferences prefs) {
		try {
			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.phone.PhoneStatusBarPolicy", classLoader,
					"updateAlarm", boolean.class, new XC_MethodReplacement() {

						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							Object mService = XposedHelpers.getObjectField(param.thisObject, "mService");

							if (mService != null) {
								Object[] arrayOfObject = new Object[2];
								arrayOfObject[0] = "alarm_clock";
								arrayOfObject[1] = false;
								XposedHelpers.callMethod(mService, "setIconVisibility", arrayOfObject);
							}
							return null;
						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
		try {
			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.phone.PhoneStatusBarPolicy", classLoader,
					"updateAlarm", boolean.class, new XC_MethodReplacement() {

						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							Object mService = XposedHelpers.getObjectField(param.thisObject, "mService");

							if (mService != null) {
								Object[] arrayOfObject = new Object[2];
								arrayOfObject[0] = "alarm_clock";
								arrayOfObject[1] = false;
								XposedHelpers.callMethod(mService, "setIconVisibility", arrayOfObject);
							}
							return null;
						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
		
	}
}

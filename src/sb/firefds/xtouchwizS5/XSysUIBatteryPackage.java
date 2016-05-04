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

public class XSysUIBatteryPackage {

	private static ClassLoader classLoader;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XSysUIBatteryPackage.classLoader = classLoader;

		if (prefs.getBoolean("hideFullBatteryNotification", false)) {
			try {
				hideFullBatteryNotification();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

		if (prefs.getBoolean("hideBatteryIcon", false) || prefs.getBoolean("selectedBatteryIcon", false)) {
			try {
				XBatteryStyle.init(prefs, classLoader);
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
	}

	private static void hideFullBatteryNotification() {
		try {
			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".power.PowerNotificationWarnings", classLoader,
					"showFullBatteryNotice", XC_MethodReplacement.DO_NOTHING);
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}
}

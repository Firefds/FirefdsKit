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

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class XSysUIPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		try {
			XSysUIFeaturePackage.doHook(prefs, classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		try {
			XSysUIQuickSettingsPackage.doHook(prefs, classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		try {
			XSysUIBatteryPackage.doHook(prefs, classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		try {
			XSysUIAlarmPackage.doHook(prefs, classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
		
		try {
			XSysUINotificationPanelPackage.doHook(prefs, classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		
		try {
			XSysUIStatusBarCarrierPackage.doHook(prefs, classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}


	}

}
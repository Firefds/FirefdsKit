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

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.firefdskit.utils.Packages;
import sb.firefds.firefdskit.utils.Utils;

public class XSysUIQuickSettingsPackage {

	private static ClassLoader classLoader;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XSysUIQuickSettingsPackage.classLoader = classLoader;

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

	private static void enableCollapseOnToggle() {
		try {
			Class<?> state = XposedHelpers.findClass(Packages.SYSTEM_UI + ".qs.QSTile$State", classLoader);
			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".qs.QSTileView", classLoader, "onStateChanged",
					state, new XC_MethodHook() {
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

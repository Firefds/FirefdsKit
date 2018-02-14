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

import static de.robv.android.xposed.XposedHelpers.findClass;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.oreo.firefdskit.utils.Packages;

public class XFrameworkWidgetPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		if (prefs.getBoolean("disableScrollingCache", false)) {
			try {
				disableScrollingCache();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}
	}

	private static void disableScrollingCache() {
		try {
			Class<?> absListView = findClass(Packages.ANDROID + ".widget.AbsListView", null);
			XposedHelpers.findAndHookMethod(absListView, "setScrollingCacheEnabled", boolean.class,
					new XC_MethodHook() {

						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.args[0] = false;
						}

					});

		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}
}

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

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSmartStay {

	private static ClassLoader classLoader;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XSmartStay.classLoader = classLoader;

		if (prefs.getBoolean("hideSmartStayIcon", false)) {
			try {
				hideSmartStayIcon();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}
		}
	}

	private static void hideSmartStayIcon() {
		try {
			Class<?> classStatusBarManager = XposedHelpers.findClass("com.samsung.android.smartface.SmartFaceIndicator", classLoader);
			XposedHelpers.findAndHookMethod(classStatusBarManager, "setIndicator", int.class, 
					new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.args[0]=1;
						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}
}

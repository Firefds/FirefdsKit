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
package sb.firefds.nougat.firefdskit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.nougat.firefdskit.utils.Packages;

public class XNfcPackage {

	private static final int SCREEN_STATE_ON_LOCKED = 2;
	private static final int SCREEN_STATE_ON_UNLOCKED = 3;
	private static XSharedPreferences prefs;
	private static ClassLoader classLoader;
	private static int behavior;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XNfcPackage.prefs = prefs;
		XNfcPackage.classLoader = classLoader;

		try {
			setIcon();
		} catch (Throwable e1) {
			XposedBridge.log(e1.toString());

		}

		try {
			behavior = prefs.getInt("nfcBehavior", 0);
			if (behavior != 0) {
				setListenMode();
			}
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

	private static void setListenMode() {
		try {
			XposedHelpers.findAndHookMethod(Packages.NFC + ".NfcService", classLoader, "applyRouting", boolean.class,
					new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

							final Object mScreenStateHelper = XposedHelpers.getObjectField(param.thisObject,
									"mScreenStateHelper");
							final int currScreenState = (Integer) XposedHelpers.callMethod(mScreenStateHelper,
									"checkScreenState");
							if ((currScreenState == SCREEN_STATE_ON_UNLOCKED)
									|| (behavior == 1 && currScreenState != SCREEN_STATE_ON_LOCKED)) {
								XposedHelpers.setAdditionalInstanceField(param.thisObject, "mOrigScreenState", -1);
								return;
							}

							synchronized (param.thisObject) {
								XposedHelpers.setAdditionalInstanceField(param.thisObject, "mOrigScreenState",
										XposedHelpers.getIntField(param.thisObject, "mScreenState"));
								XposedHelpers.setIntField(param.thisObject, "mScreenState", SCREEN_STATE_ON_UNLOCKED);
							}
						}

						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							final int mOrigScreenState = (Integer) XposedHelpers.getAdditionalInstanceField(
									param.thisObject, "mOrigScreenState");
							if (mOrigScreenState == -1)
								return;

							synchronized (param.thisObject) {
								XposedHelpers.setIntField(param.thisObject, "mScreenState", mOrigScreenState);
							}
						}

					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

	private static void setIcon() {
		try {
			XposedHelpers.findAndHookMethod(Packages.NFC + ".NfcService", classLoader, "showIcon", boolean.class,
					new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

							if ((Boolean) XposedHelpers.callMethod(param.thisObject, "isNfcEnabled")) {
								param.args[0] = !prefs.getBoolean("hideNfcIcon", false);
							}
						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

}

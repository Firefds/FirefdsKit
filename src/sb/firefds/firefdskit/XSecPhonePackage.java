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

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.firefdskit.utils.Packages;

public class XSecPhonePackage {

	private static final String PHONE_FEATURE = Packages.PHONE + ".TeleServiceFeature";
	private static ClassLoader classLoader;
	private static XSharedPreferences prefs;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {
		XSecPhonePackage.classLoader = classLoader;
		XSecPhonePackage.prefs = prefs;

		enableCallRecording();
		enableCallRecordingMenu();
		disableEscalatingRing();

	}

	private static void disableEscalatingRing() {
		try {
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = String.class;
			arrayOfObject[1] = new XC_MethodHook() {
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					if ("ringtone_escalating".equals(param.args[0])) {
						param.setResult(!XSecPhonePackage.prefs.getBoolean("disableEscalatingRingtone", false));
					}
				}
			};
			XposedHelpers.findAndHookMethod(PHONE_FEATURE, classLoader, "hasFeature", arrayOfObject);
		} catch (Throwable e) {
		}
	}

	private static void enableCallRecording() {

		try {
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = String.class;
			arrayOfObject[1] = new XC_MethodHook() {
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					if ("voice_call_recording".equals(param.args[0])) {
						param.setResult(!XSecPhonePackage.prefs.getBoolean("enableCallAdd", false)
								|| (XSecPhonePackage.prefs.getBoolean("enableCallAdd", false) && XSecPhonePackage.prefs
										.getBoolean("enableCallRecordingMenu", false)));
					}
				}
			};
			XposedHelpers.findAndHookMethod(PHONE_FEATURE, classLoader, "hasFeature", arrayOfObject);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	private static void enableCallRecordingMenu() {

		try {
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = String.class;
			arrayOfObject[1] = new XC_MethodHook() {
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					if (("voice_call_recording_menu".equals(param.args[0]))) {
						param.setResult(XSecPhonePackage.prefs.getBoolean("enableCallAdd", false)
								&& XSecPhonePackage.prefs.getBoolean("enableCallRecordingMenu", false));
					}
				}
			};
			XposedHelpers.findAndHookMethod(PHONE_FEATURE, classLoader, "hasFeature", arrayOfObject);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}

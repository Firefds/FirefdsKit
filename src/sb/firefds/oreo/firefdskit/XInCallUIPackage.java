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

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.oreo.firefdskit.utils.Packages;

public class XInCallUIPackage {

	public static void doHook(final XSharedPreferences prefs, ClassLoader classLoader) {

		//Enable call recording
		try {
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = String.class;
			arrayOfObject[1] = new XC_MethodHook() {
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					prefs.reload();
					if ((prefs.getBoolean("enableCallAdd", false) && prefs.getBoolean("enableCallRecordingMenu", false)) || 
							(!prefs.getBoolean("enableCallAdd", false)) || 
							(prefs.getBoolean("enableAutoCallRecording", false)))
						if ("voice_call_recording".equals(param.args[0])) {
							param.setResult(Boolean.TRUE);
						}
				}
			};
			XposedHelpers.findAndHookMethod(Packages.INCALLUI + ".InCallUIFeature", classLoader, "hasFeature",
					arrayOfObject);
		} catch (Throwable e1) {
			XposedBridge.log(e1.toString());
		}

		//Enable call recording menu
		try {
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = String.class;
			arrayOfObject[1] = new XC_MethodHook() {
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					prefs.reload();
					if (prefs.getBoolean("enableCallAdd", false) && prefs.getBoolean("enableCallRecordingMenu", false))
						if ("voice_call_recording_menu".equals(param.args[0])) {
							param.setResult(Boolean.TRUE);
						}
				}
			};
			XposedHelpers.findAndHookMethod(Packages.INCALLUI + ".InCallUIFeature", classLoader, "hasFeature",
					arrayOfObject);
		} catch (Throwable e) {
			XposedBridge.log(e.toString());
		}

		//Enable automatic call recording
		try {
			final Class<?> mCallList = XposedHelpers.findClass(Packages.INCALLUI + ".CallList", classLoader);

			XposedHelpers.findAndHookMethod(Packages.INCALLUI + ".InCallPresenter", classLoader,
					"processOnCallListChange", mCallList, new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					prefs.reload();
					if (prefs.getBoolean("enableAutoCallRecording", false)){
						Object mCall = XposedHelpers.callMethod(param.args[0], "getFirstCall");

						Object mRecorderMgr = XposedHelpers.getObjectField(param.thisObject, "mRecorderMgr");

						if (mRecorderMgr != null) {

							Boolean mIsRecording = (Boolean) XposedHelpers.callMethod(mRecorderMgr, "isRecording");
							if (mCall != null) {
								int mState = (Integer) XposedHelpers.callMethod(mCall, "getState");

								if (mIsRecording && mState != 3) {// Recording on an Inactive Call
									XposedHelpers.callMethod(mRecorderMgr, "toggleRecord");
								} else if (!mIsRecording && mState == 3) { // Not recording on an Active Call
									XposedHelpers.callMethod(mRecorderMgr, "toggleRecord");
								}
							}
						}
					}
				}
			});
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}
}

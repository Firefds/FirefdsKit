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

public class XInCallUIPackage {

	private static ClassLoader classLoader;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XInCallUIPackage.classLoader = classLoader;

		if (prefs.getBoolean("enableCallAdd", false) && prefs.getBoolean("enableCallRecordingMenu", false)) {
			try {
				enableCallRecording();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}
			try {
				enableCallRecordingMenu();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}
		}

		if (!prefs.getBoolean("enableCallAdd", false)) {
			try {
				enableCallRecording();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

		if (prefs.getBoolean("enableAutoCallRecording", false)) {
			try {
				enableCallRecording();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
			try {
				enableAutoCallRecording();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}
	}

	private static void enableAutoCallRecording() {
		try {
			final Class<?> mCallList = XposedHelpers.findClass(Packages.INCALLUI + ".CallList", classLoader);

			XposedHelpers.findAndHookMethod(Packages.INCALLUI + ".InCallPresenter", classLoader,
					"processOnCallListChange", mCallList, new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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
			});
		} catch (Throwable e) {
			try {
				Class<?> mCallList = XposedHelpers.findClass(Packages.INCALLUI + ".CallList", classLoader);
				final Class<?> mInCallState = XposedHelpers.findClass(Packages.INCALLUI
						+ ".InCallPresenter$InCallState", classLoader);
				XposedHelpers.findAndHookMethod(Packages.INCALLUI + ".InCallPresenter", classLoader, "onStateChange",
						mInCallState, mInCallState, mCallList, new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Object mCall = XposedHelpers.callMethod(param.args[1], "getFirstCall");
						if (param.thisObject != null) {
							Object mRecorderMgr = XposedHelpers
									.getObjectField(param.thisObject, "mRecorderMgr");
							if (mRecorderMgr != null) {
								Boolean mIsRecording = (Boolean) XposedHelpers.callMethod(mRecorderMgr,
										"isRecording");
								if (mCall != null) {
									int mState = (Integer) XposedHelpers.callMethod(mCall, "getState");
									if (mIsRecording && mState != 3) {// Recording on an Inactive Cal
										XposedHelpers.callMethod(param.thisObject, "toggleRecord");
									} else if (!mIsRecording && mState == 3) { // Not recording on an Active Call
										XposedHelpers.callMethod(param.thisObject, "toggleRecord");
									}
								}
							}
						}
					}
				});
			} catch (Throwable e1) {
				XposedBridge.log(e1);
			}
		}
	}

	private static void enableCallRecording() {

		try {
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = String.class;
			arrayOfObject[1] = new XC_MethodHook() {
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
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

	}

	private static void enableCallRecordingMenu() {

		try {
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = String.class;
			arrayOfObject[1] = new XC_MethodHook() {
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					if ("voice_call_recording_menu".equals(param.args[0])) {
						param.setResult(Boolean.TRUE);
					}
				}
			};
			XposedHelpers.findAndHookMethod(Packages.INCALLUI + ".InCallUIFeature", classLoader, "hasFeature",
					arrayOfObject);
		} catch (Throwable e1) {
			XposedBridge.log(e1.toString());

		}
	}
}

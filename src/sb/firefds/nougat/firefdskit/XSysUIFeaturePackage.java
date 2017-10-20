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

import java.lang.reflect.Method;

import android.graphics.Color;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.nougat.firefdskit.utils.Packages;

public class XSysUIFeaturePackage {

	private static Object objKeyguardAbsKeyInputView;

	public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

		if (prefs.getBoolean("hideStorageNotification", false)) {
			try {
				hideStorageNotification(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);

			}
		}

		try {
			Class<?> KeyguardAbsKeyInputView = XposedHelpers.findClass("com.android.keyguard.KeyguardAbsKeyInputView",
					classLoader);
			Class<?> PasswordTextView = XposedHelpers.findClass("com.android.keyguard.PasswordTextView", classLoader);
			final Method verifyPasswordAndUnlock = XposedHelpers.findMethodExact(KeyguardAbsKeyInputView,
					"verifyPasswordAndUnlock");

			XposedHelpers.findAndHookMethod(KeyguardAbsKeyInputView, "onFinishInflate", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {

					objKeyguardAbsKeyInputView = param.thisObject;
				}
			});

			XposedHelpers.findAndHookMethod(PasswordTextView, "append", char.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					prefs.reload();
					if (prefs.getBoolean("quickPinUnlockEnabled", false)) {
						String mText = (String) XposedHelpers.getObjectField(param.thisObject, "mText");

						if (mText.length() == prefs.getInt("PINSize", 4)) {
							XposedBridge.invokeOriginalMethod(verifyPasswordAndUnlock, objKeyguardAbsKeyInputView, null);
						}
					}
				}
			});
		}
		catch (Exception e1) {
			XposedBridge.log(e1.getMessage());
		}

		try {
			Class<?> mVolumePanel = XposedHelpers.findClass(Packages.SYSTEM_UI + ".volume.SecVolumeDialog", classLoader);
			XposedHelpers.findAndHookMethod(mVolumePanel, "updateTintColor", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {

					prefs.reload();
					if (prefs.getBoolean("semiTransparentVolumePanel", false)) {
						XposedHelpers.setObjectField(
								param.thisObject,
								"mVolumePanelBgColor",
								XposedHelpers.callMethod(param.thisObject, "colorToColorStateList",
										Color.parseColor("#7fffffff")));
					}
				}
			});
		}
		catch (Throwable e1){
			XposedBridge.log(e1.getMessage());
		}
	}

	private static void hideStorageNotification(ClassLoader classLoader) {
		try {
			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".usb.StorageNotification", classLoader,
					"onVolumeMounted", "android.os.storage.VolumeInfo", new XC_MethodReplacement() {

				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					return null;
				}
			});
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}
}
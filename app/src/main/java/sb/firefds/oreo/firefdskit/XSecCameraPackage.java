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

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.oreo.firefdskit.utils.Packages;

public class XSecCameraPackage {

	private static ClassLoader classLoader;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XSecCameraPackage.classLoader = classLoader;

		if (prefs.getBoolean("disableTemperatureChecks", false)) {
			try {
				disableTemperatureChecks();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

		if (prefs.getBoolean("disableShutterSound", false)) {
			try {
				disableShutterSound();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}
	}

	private static void disableTemperatureChecks() {

		try {
			XposedHelpers.findAndHookMethod(Packages.CAMERA + ".CameraSettings", classLoader,
					"isTemperatureHighToUseFlash", XC_MethodReplacement.returnConstant(false));
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		try {
			XposedHelpers.findAndHookMethod(Packages.CAMERA + ".CameraSettings", classLoader,
					"isTemperatureLowToUseFlash", XC_MethodReplacement.returnConstant(false));
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		try {
			XposedHelpers.findAndHookMethod(Packages.CAMERA + ".CameraSettings", classLoader,
					"isTemperatureHighToRecord", XC_MethodReplacement.returnConstant(false));
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

	private static void disableShutterSound() {

		try {
			XposedHelpers.findAndHookMethod(Packages.CAMERA + ".Camera", classLoader,
					"playCameraSound", "com.sec.android.app.camera.interfaces.CameraContext.SoundId",
					int.class, XC_MethodReplacement.DO_NOTHING);
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

}

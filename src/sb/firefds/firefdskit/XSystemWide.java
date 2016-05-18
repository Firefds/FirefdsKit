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
import android.content.pm.ActivityInfo;
import android.content.res.Resources.NotFoundException;
import android.content.res.XResources;
import android.os.IBinder;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.firefdskit.utils.Packages;

public class XSystemWide {

	private static final String MULTI_WINDOW_FACADE = "com.samsung.android.multiwindow.MultiWindowFacade";
	private static final String MULTI_WINDOW_APPLICATION_INFOS = "com.samsung.android.multiwindow.MultiWindowApplicationInfos";
	private static XSharedPreferences prefs;

	public static void doHook(String modulePath, XSharedPreferences prefs, ClassLoader classLoader) {

		XSystemWide.prefs = prefs;

		try {
			setSystemWideTweaks();
			
		} catch (Throwable e) {
			XposedBridge.log(e.toString());
		}

		if (prefs.getBoolean("longPressTrackSkip", false)) {
			try {
				XVolumeKeysSkipTrack.init(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

		try {
			setMultiWindowApps(classLoader);

		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

	}

	private static void setMultiWindowApps(ClassLoader classLoader) {

		if (!prefs.getString("selectedMwApps", "").trim().equalsIgnoreCase("")) {
			String[] selectedApps = prefs.getString("selectedMwApps", "").split(";");
			try {
				String[] defaultApps = XResources.getSystem().getStringArray(
						XResources.getSystem().getIdentifier("config_multiWindowSupportAppList", "array",
								Packages.ANDROID));

				XResources.setSystemWideReplacement("android", "array", "config_multiWindowSupportAppList",
						combine(defaultApps, selectedApps));
			} catch (NotFoundException e) {
				XposedBridge.log(e.toString());
			}

			try {
				String[] defaultPenApps = XResources.getSystem().getStringArray(
						XResources.getSystem().getIdentifier("config_pen_window_applist", "array", Packages.ANDROID));

				XResources.setSystemWideReplacement("android", "array", "config_pen_window_applist",
						combine(defaultPenApps, selectedApps));
			} catch (NotFoundException e) {
				XposedBridge.log(e.toString());
			}

			try {
				String[] defaultPenApps = XResources.getSystem().getStringArray(
						XResources.getSystem().getIdentifier("config_multiWindowSupportPackageList", "array",
								Packages.ANDROID));

				XResources.setSystemWideReplacement("android", "array", "config_multiWindowSupportPackageList",
						combine(defaultPenApps, selectedApps));
			} catch (NotFoundException e) {
				XposedBridge.log(e.toString());
			}

			try {
				String[] defaultPenApps = XResources.getSystem().getStringArray(
						XResources.getSystem().getIdentifier("config_multiWindowSupportComponentList", "array",
								Packages.ANDROID));

				XResources.setSystemWideReplacement("android", "array", "config_multiWindowSupportComponentList",
						combine(defaultPenApps, selectedApps));
			} catch (NotFoundException e) {
				XposedBridge.log(e.toString());
			}

			try {
				XposedHelpers.findAndHookMethod(MULTI_WINDOW_FACADE, classLoader, "isSplitSupportedForTask",
						IBinder.class, XC_MethodReplacement.returnConstant(true));
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}

			try {
				XposedHelpers.findAndHookMethod(MULTI_WINDOW_APPLICATION_INFOS, classLoader, "isSupportPackageList",
						String.class, XC_MethodReplacement.returnConstant(true));
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}

			try {
				XposedHelpers.findAndHookMethod(MULTI_WINDOW_APPLICATION_INFOS, classLoader, "isSupportComponentList",
						String.class, XC_MethodReplacement.returnConstant(true));
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}

			try {
				XposedHelpers.findAndHookMethod(MULTI_WINDOW_APPLICATION_INFOS, classLoader, "isSupportApp",
						String.class, XC_MethodReplacement.returnConstant(true));
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}

			try {
				XposedHelpers.findAndHookMethod(MULTI_WINDOW_APPLICATION_INFOS, classLoader, "isSupportScaleApp",
						ActivityInfo.class, Context.class, XC_MethodReplacement.returnConstant(true));
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}

			try {
				XposedHelpers.findAndHookMethod(MULTI_WINDOW_APPLICATION_INFOS, classLoader, "isSupportMultiWindow",
						ActivityInfo.class, XC_MethodReplacement.returnConstant(true));
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}
		}
	}

	private static String[] combine(String[] defaultApps, String[] selectedApps) {
		int length = defaultApps.length + selectedApps.length;
		String[] result = new String[length];
		System.arraycopy(defaultApps, 0, result, 0, defaultApps.length);
		System.arraycopy(selectedApps, 0, result, defaultApps.length, selectedApps.length);
		return result;
	}

	private static void setSystemWideTweaks() {

		if (prefs.getBoolean("disbaleLowBatteryCloseWarningLevel", false)) {
			try {
				XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_lowBatteryWarningLevel", 1);
				XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_criticalBatteryWarningLevel",
						1);
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}
		} else if (prefs.getInt("configCriticalBatteryWarningLevel", 5) != 5) {
			try {
				XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_criticalBatteryWarningLevel",
						prefs.getInt("configCriticalBatteryWarningLevel", 5));
				XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_lowBatteryCloseWarningLevel",
						prefs.getInt("configCriticalBatteryWarningLevel", 5));
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}
		}

	}
}

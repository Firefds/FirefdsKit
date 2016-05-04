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

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.firefdskit.utils.Packages;
import sb.firefds.firefdskit.utils.Utils.SuTask;

public class XAndroidPackage {

	public static final String PHONE_WINDOW_MANAGER = "com.android.server.policy.PhoneWindowManager";
	private static XSharedPreferences prefs;
	private static ClassLoader classLoader;
	private static Context mContext = null;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XAndroidPackage.prefs = prefs;
		XAndroidPackage.classLoader = classLoader;

		if (prefs.getBoolean("disableTIMA", true))
			try {
				disableTIMA();
			} catch (Throwable e) {
				e.printStackTrace();
			}

		if (prefs.getBoolean("disableLoudVolumeWarning", true)) {
			try {
				disableLoudVolumeWarningDialog();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

		if (prefs.getBoolean("hideSmartStayIcon", false)) {
			try {
				hideSmartStayIcon();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

		try {
			handleLongBackKill();

		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		if (prefs.getBoolean("disableCameraShutterSound", false)) {
			try {
				disableCameraShutterSound();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

		if (prefs.getBoolean("disableWakeOnHome", false)) {
			disableHomeWake();
		}

	}
	private static void hideSmartStayIcon() {
		try {
			Class<?> classStatusBarManager = XposedHelpers.findClass("android.app.StatusBarManager", classLoader);
			XposedHelpers.findAndHookMethod(classStatusBarManager, "setIconVisibility", String.class, boolean.class,
					new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							if (param.args[0].equals("smart_scroll")) {
								param.args[1] = false;
							}
						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

	private static void disableTIMA() {
		final Class<?> mTimaService = XposedHelpers.findClass("com.android.server.TimaService", classLoader);

		XposedHelpers.findAndHookMethod(mTimaService, "checkEvent", int.class, int.class,
				XC_MethodReplacement.returnConstant(null));
	}

	private static void disableHomeWake() {
		final Class<?> PhoneWindowManager = XposedHelpers.findClass(PHONE_WINDOW_MANAGER, classLoader);

		XposedHelpers.findAndHookMethod(PhoneWindowManager, "isWakeKeyWhenScreenOff", int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				int keyCode = (Integer) param.args[0];
				if (keyCode == KeyEvent.KEYCODE_HOME) {
					param.setResult(Boolean.FALSE);
				}
			}
		});
	}

	private static void disableCameraShutterSound() {
		try {
			XposedHelpers.findAndHookMethod("com.sec.android.seccamera.SecCamera", classLoader,
					"setShutterSoundEnable", boolean.class, new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.args[0] = false;
						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		try {
			XposedHelpers.findAndHookMethod("com.sec.android.seccamera.SecCamera", classLoader, "enableShutterSound",
					boolean.class, new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.args[0] = false;
						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		try {
			final Class<?> mCameraInfo = XposedHelpers
					.findClass("com.sec.android.seccamera.SecCamera$CameraInfo", null);
			XposedHelpers.findAndHookMethod("com.sec.android.seccamera.SecCamera", classLoader, "getCameraInfo",
					int.class, mCameraInfo, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							if (param.args[1] != null) {
								XposedHelpers.setBooleanField(param.args[1], "canDisableShutterSound", true);
							}
						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

		try {
			XposedHelpers.findAndHookMethod("android.media.AudioService", classLoader, "handleConfigurationChanged",
					Context.class, new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							XposedHelpers.setObjectField(param.thisObject, "mCameraSoundForced", Boolean.valueOf(false));
						}

						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							XposedHelpers.setObjectField(param.thisObject, "mCameraSoundForced", Boolean.valueOf(false));
						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}

	}

	private static Runnable mBackLongPress = new Runnable() {

		@Override
		public void run() {
			if (prefs.getBoolean("enableLongBackKill", false)) {
				killTask();
			}
		}
	};

	private static XC_MethodHook handleInterceptKeyBeforeQueueing = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			try {
				final KeyEvent event = (KeyEvent) param.args[0];
				final int keyCode = event.getKeyCode();

				boolean isScreenOn = (Boolean) XposedHelpers.callMethod(param.thisObject, "isScreenOn");

				if (isScreenOn) {

					mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

					boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
					Handler mHandler = (Handler) XposedHelpers.getObjectField(param.thisObject, "mHandler");
					if (down) {
						boolean isFromSystem = (event.getFlags() & KeyEvent.FLAG_FROM_SYSTEM) != 0;
						if (keyCode == KeyEvent.KEYCODE_BACK && isFromSystem && event.getRepeatCount() == 0) {
							mHandler.postDelayed(mBackLongPress, ViewConfiguration.getLongPressTimeout());
						}
					} else {
						mHandler.removeCallbacks(mBackLongPress);
					}
				}
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}
	};

	private static void handleLongBackKill() {
		final Class<?> classPhoneWindowManager = findClass("com.android.server.policy.PhoneWindowManager", classLoader);

		findAndHookMethod(classPhoneWindowManager, "interceptKeyBeforeQueueing", KeyEvent.class, int.class,
				handleInterceptKeyBeforeQueueing);
	}

	private static void killTask() {

		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		@SuppressWarnings("deprecation")
		List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
		RunningTaskInfo info = tasks.get(0);

		List<String> checkedApps = new ArrayList<String>();
		String[] launchersList = Packages.LONGBACK_KILL_WHITELIST;
		String[] whiteList = prefs.getString("enableLongBackKillWhiteList", "").split(";");
		checkedApps.addAll(Arrays.asList(launchersList));
		checkedApps.addAll(Arrays.asList(whiteList));

		for (int i = 0; i < checkedApps.size(); i++) {
			String packageName = checkedApps.get(i);
			if (info.topActivity.getPackageName().equalsIgnoreCase(packageName)) {
				return;
			}
		}

		String targetKilled = info.topActivity.getPackageName();

		try {
			new SuTask().execute("am force-stop " + targetKilled);
		} catch (Throwable e) {
		}

		try {
			final PackageManager pm = mContext.getPackageManager();
			targetKilled = (String) pm.getApplicationLabel(pm.getApplicationInfo(targetKilled, 0));
		} catch (PackageManager.NameNotFoundException nfe) {

		}

		Intent intent = new Intent("ma.wanam.xposed.action.SHOW_TOAST");
		intent.putExtra("processName", targetKilled);
		mContext.sendBroadcast(intent);
	}

	private static void disableLoudVolumeWarningDialog() {
		try {
			XposedHelpers.findAndHookConstructor("android.media.AudioManager", classLoader, Context.class,
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {

							Object objService = XposedHelpers.callMethod(param.thisObject, "getService");
							Context mApplicationContext = (Context) XposedHelpers.getObjectField(param.thisObject,
									"mApplicationContext");

							if (objService != null && mApplicationContext != null) {
								XposedHelpers.callMethod(param.thisObject, "disableSafeMediaVolume");
							}
						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

}

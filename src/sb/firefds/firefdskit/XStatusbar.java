/*
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
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

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.firefdskit.battery.StatusBarIconManager;
import sb.firefds.firefdskit.battery.StatusBarIconManager.IconManagerListener;
import sb.firefds.firefdskit.receivers.BroadcastSubReceiver;
import sb.firefds.firefdskit.utils.Packages;

public class XStatusbar {
	private static final String CLASS_PHONE_STATUSBAR_VIEW = "com.android.systemui.statusbar.phone.PhoneStatusBarView";
	private static final String CLASS_PHONE_STATUSBAR = "com.android.systemui.statusbar.phone.PhoneStatusBar";
	private static final String CLASS_SB_TRANSITIONS = "com.android.systemui.statusbar.phone.PhoneStatusBarTransitions";

	private static View mPanelBar;
	private static StatusBarIconManager mIconManager;
	private static List<BroadcastSubReceiver> mBroadcastSubReceivers;

	public static void registerIconManagerListener(IconManagerListener listener) {
		if (mIconManager != null) {
			mIconManager.registerListener(listener);
		}
	}

	private static BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			for (BroadcastSubReceiver bsr : mBroadcastSubReceivers) {
				bsr.onBroadcastReceived(context, intent);
			}
		}
	};

	// in process hooks
	public static void init(final XSharedPreferences prefs, final ClassLoader classLoader) {
		try {
			final Class<?> phoneStatusbarViewClass = XposedHelpers.findClass(CLASS_PHONE_STATUSBAR_VIEW, classLoader);
			final Class<?> phoneStatusbarClass = XposedHelpers.findClass(CLASS_PHONE_STATUSBAR, classLoader);
			final Class<?> sbTransitionsClass = XposedHelpers.findClass(CLASS_SB_TRANSITIONS, classLoader);
			mBroadcastSubReceivers = new ArrayList<BroadcastSubReceiver>();

			XposedBridge.hookAllConstructors(phoneStatusbarViewClass, new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					mPanelBar = (View) param.thisObject;

					IntentFilter intentFilter = new IntentFilter();
					intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
					mPanelBar.getContext().registerReceiver(mBroadcastReceiver, intentFilter);
					Context gbContext = mPanelBar.getContext().createPackageContext(Packages.XTOUCHWIZ,
							Context.CONTEXT_IGNORE_SECURITY);
					mIconManager = new StatusBarIconManager(mPanelBar.getContext(), gbContext);
					mBroadcastSubReceivers.add(mIconManager);
				}
			});

			XposedHelpers.findAndHookMethod(phoneStatusbarClass, "getNavigationBarLayoutParams", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					WindowManager.LayoutParams lp = (WindowManager.LayoutParams) param.getResult();
					if (lp != null) {
						lp.format = PixelFormat.TRANSLUCENT;
						param.setResult(lp);
					}
				}
			});

			XposedHelpers.findAndHookMethod(sbTransitionsClass, "applyMode", int.class, boolean.class,
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							if (mIconManager != null) {
								final float signalClusterAlpha = (Float) XposedHelpers.callMethod(param.thisObject,
										"getNonBatteryClockAlphaFor", (Integer) param.args[0]);
								final float textAndBatteryAlpha = (Float) XposedHelpers.callMethod(param.thisObject,
										"getBatteryClockAlpha", (Integer) param.args[0]);
								mIconManager.setIconAlpha(signalClusterAlpha, textAndBatteryAlpha);
							}
						}
					});

		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

}

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

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import sb.firefds.firefdskit.battery.CmCircleBattery;
import sb.firefds.firefdskit.utils.Packages;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class XBatteryStyle {
	public static final String CLASS_BATTERY_CONTROLLER = Packages.SYSTEM_UI + ".statusbar.policy.BatteryController";

	private static CmCircleBattery mCircleBattery1;
	private static CmCircleBattery mCircleBattery2;
	private static View mStockBatteryST;
	private static View mStockBatteryLC;
	private static XSharedPreferences xPrefs;
	private static int circleColor = Color.WHITE;
	private static int circleTextColor = Color.WHITE;

	public static void initResources(final XSharedPreferences prefs, InitPackageResourcesParam resparam) {

		int padingSize = 6;
		String tsPrefVal = prefs.getString("batterySize", "Medium");
		if (tsPrefVal.equals("Medium")) {
			padingSize = 7;
		} else if (tsPrefVal.equals("Large")) {
			padingSize = 8;
		} else if (tsPrefVal.equals("Larger")) {
			padingSize = 9;
		} else if (tsPrefVal.equals("Largest")) {
			padingSize = 10;
		}

		setBatteryIcon1(prefs, resparam, "super_status_bar", padingSize);
		setBatteryIcon2(prefs, resparam, "keyguard_status_bar", padingSize);
	}

	private static void setBatteryIcon1(final XSharedPreferences prefs, InitPackageResourcesParam resparam,
			String layout, final int padingSize) {
		try {
			xPrefs = prefs;
			resparam.res.hookLayout(Packages.SYSTEM_UI, "layout", layout, new XC_LayoutInflated() {

				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {

					ViewGroup vg = (ViewGroup) liparam.view.findViewById(liparam.res.getIdentifier(
							"signal_battery_cluster", "id", Packages.SYSTEM_UI));

					// inject circle battery view
					mCircleBattery1 = new CmCircleBattery(vg.getContext(), circleColor, prefs);
					mCircleBattery1.setTag("circle_battery");
					LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lParams.gravity = Gravity.CENTER_VERTICAL;
					mCircleBattery1.setLayoutParams(lParams);
					mCircleBattery1.setPadding(padingSize, 0, 0, 0);
					mCircleBattery1.setCircleTextColor(circleTextColor);
					mCircleBattery1.setVisibility(View.GONE);
					XStatusbar.registerIconManagerListener(mCircleBattery1);
					vg.addView(mCircleBattery1);

					// find battery
					mStockBatteryST = vg.findViewById(liparam.res.getIdentifier("battery", "id", Packages.SYSTEM_UI));
					if (mStockBatteryST != null) {
						mStockBatteryST.setTag("stock_battery");
					}
				}

			});
		} catch (ClassCastException t) {
			XposedBridge.log(t);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private static void setBatteryIcon2(final XSharedPreferences prefs, InitPackageResourcesParam resparam,
			String layout, final int padingSize) {
		try {
			xPrefs = prefs;
			resparam.res.hookLayout(Packages.SYSTEM_UI, "layout", layout, new XC_LayoutInflated() {

				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {

					ViewGroup vg = (ViewGroup) liparam.view.findViewById(liparam.res.getIdentifier(
							"signal_battery_cluster", "id", Packages.SYSTEM_UI));

					// inject circle battery view
					mCircleBattery2 = new CmCircleBattery(vg.getContext(), circleColor, prefs);
					mCircleBattery2.setTag("circle_battery");
					LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lParams.gravity = Gravity.CENTER_VERTICAL;
					mCircleBattery2.setLayoutParams(lParams);
					mCircleBattery2.setPadding(padingSize, 0, 0, 0);
					mCircleBattery2.setCircleTextColor(circleTextColor);
					mCircleBattery2.setVisibility(View.GONE);
					XStatusbar.registerIconManagerListener(mCircleBattery2);
					vg.addView(mCircleBattery2);

					// find battery
					mStockBatteryLC = vg.findViewById(liparam.res.getIdentifier("battery", "id", Packages.SYSTEM_UI));
					if (mStockBatteryLC != null) {
						mStockBatteryLC.setTag("stock_battery");
					}
				}

			});
		} catch (ClassCastException t) {
			XposedBridge.log(t);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void init(final XSharedPreferences prefs, ClassLoader classLoader) {

		try {

			XStatusbar.init(prefs, classLoader);

			Class<?> batteryControllerClass = XposedHelpers.findClass(CLASS_BATTERY_CONTROLLER, classLoader);

			XposedBridge.hookAllConstructors(batteryControllerClass, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					updateBatteryStyle();
				}
			});

		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private static void updateBatteryStyle() {
		try {
			if (mStockBatteryST != null) {
				mStockBatteryST.setVisibility(View.GONE);
			}

			if (mCircleBattery1 != null) {
				if (xPrefs.getBoolean("hideBatteryIcon", false)) {
					mCircleBattery1.setVisibility(View.GONE);
				} else {
					mCircleBattery1.setVisibility(View.VISIBLE);
					mCircleBattery1.setPercentage(true); // display percentage
				}
			}

			if (mStockBatteryLC != null) {
				mStockBatteryLC.setVisibility(View.GONE);
			}

			if (mCircleBattery2 != null) {
				if (xPrefs.getBoolean("hideBatteryIcon", false)) {
					mCircleBattery2.setVisibility(View.GONE);
				} else {
					mCircleBattery2.setVisibility(View.VISIBLE);
					mCircleBattery2.setPercentage(true); // display percentage
				}
			}

		} catch (ClassCastException t) {
			XposedBridge.log(t);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
}

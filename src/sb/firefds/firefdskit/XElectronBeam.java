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

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.SparseArray;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;

public class XElectronBeam {
	private static final String CLASS_DISPLAY_POWER_CONTROLLER = "com.android.server.display.DisplayPowerController";
	private static final String CLASS_DISPLAY_POWER_POWERSTATE = "com.android.server.display.DisplayPowerState";
	private static final String CLASS_COLOR_FADE = "com.android.server.display.ColorFade";

	public static void initZygote(final XSharedPreferences prefs, ClassLoader classLoader) {
		try {
			final int mode = Integer.valueOf(prefs.getString("screenOffEffect", "0"));

			if (mode != 0) {
				final Class<?> clsDisplayPowerController = XposedHelpers.findClass(CLASS_DISPLAY_POWER_CONTROLLER,
						classLoader);

				final Class<?> clsDisplayPowerState = XposedHelpers.findClass(CLASS_DISPLAY_POWER_POWERSTATE,
						classLoader);
				
				final Class<?> clsColorFade = XposedHelpers.findClass(CLASS_COLOR_FADE,
						classLoader);
				
				if (mode==5)
				{
					try{
						// Disable android 5.0+ ColorFade  // by NUI
						XposedHelpers.findAndHookMethod(clsDisplayPowerState, "prepareColorFade", Context.class, int.class,
								XC_MethodReplacement.returnConstant(false));
						XposedHelpers.findAndHookMethod(clsColorFade, "draw", float.class,
								XC_MethodReplacement.returnConstant(true));
						XposedHelpers.findAndHookMethod(clsColorFade, "drawFaded", float.class, float.class, float.class, float.class,
								XC_MethodReplacement.returnConstant(null));
						XposedHelpers.findAndHookMethod(clsDisplayPowerState, "scheduleColorFadeDraw",
								XC_MethodReplacement.returnConstant(null));
						XposedHelpers.findAndHookMethod(clsDisplayPowerController, "animateScreenStateChange", int.class, int.class, boolean.class,
								new XC_MethodReplacement() {
									@Override
									protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
										int target = (Integer) param.args[0];
										if (target == 2) {
										} else if (target == 3) {
										} else if (target == 4) {
										} else {
											param.args[2] = false;
										}
										return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
									}
								}
						);
					} catch (Throwable e) {
						XposedBridge.log("Attempt to remove native screen off animation failed - " + e.toString());
					}
				}
				else {

					XposedHelpers.findAndHookMethod(clsDisplayPowerController, "animateScreenStateChange", int.class, int.class, boolean.class,
						new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
							param.args[2] = true;
						}
					});

					XposedHelpers.findAndHookMethod(clsDisplayPowerState, "prepareColorFade", Context.class, int.class,
							new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
							param.args[1] = mode;
						}
					});

					XposedHelpers.findAndHookMethod(clsDisplayPowerController, "initialize", new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
							@SuppressWarnings("unchecked")
							SparseArray<ObjectAnimator> oa = (SparseArray<ObjectAnimator>) XposedHelpers.getObjectField(param.thisObject,
									"mColorFadeOffAnimator");

							if (oa != null && oa.get(0).getDuration() < 400) {
								oa.get(0).setDuration(400);
							}
						}
					});
				}
			}
		} catch (ClassNotFoundError t) {
			XposedBridge.log(t);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

	}

}
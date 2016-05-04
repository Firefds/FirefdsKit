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
package sb.firefds.xtouchwizS5;

import android.os.Parcel;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.xtouchwizS5.utils.Packages;

public class XSysScopePackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		if (prefs.getBoolean("makeMeTooLegit", true)) {

			try {
				Object[] arrayOfObject3 = new Object[2];
				arrayOfObject3[0] = String.class;
				arrayOfObject3[1] = XC_MethodReplacement.returnConstant(Integer.valueOf(1));
				XposedHelpers.findAndHookMethod(Packages.SYS_SCOPE + ".job.RootProcessScanner", classLoader,
						"checkIsApprivedProcess", arrayOfObject3);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}

			try {
				Object[] arrayOfObject2 = new Object[2];
				arrayOfObject2[0] = String.class;
				arrayOfObject2[1] = XC_MethodReplacement.returnConstant(Boolean.TRUE);
				XposedHelpers.findAndHookMethod(Packages.SYS_SCOPE + ".job.KernelStatusChecker", classLoader, "b",
						arrayOfObject2);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}

			try {
				XposedHelpers.findAndHookMethod(Packages.SYS_SCOPE + ".service.SysScopeResultInfo", classLoader, "a",
						XC_MethodReplacement.returnConstant(Integer.valueOf(1)));
			} catch (Throwable e) {
				XposedBridge.log(e);
			}

			try {
				XposedHelpers.findAndHookMethod(Packages.SYS_SCOPE + ".service.SysScopeResultInfo", classLoader, "a",
						Parcel.class, new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								XposedHelpers.setObjectField(param.thisObject, "b", null);
							}
						});
			} catch (Throwable e) {
				XposedBridge.log(e);
			}

			try {
				XposedHelpers.findAndHookMethod(Packages.SYS_SCOPE + ".engine.SystemDiagnosisManager", classLoader,
						"storeResult", int.class, new XC_MethodHook() {
							@Override
							protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
								param.args[0] = 1;
							}
						});
			} catch (Throwable e) {
				XposedBridge.log(e);
			}

		}
	}

}
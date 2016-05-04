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

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.firefdskit.utils.Packages;

public class XPM23 {
	private static final String CLASS_PACKAGE_MANAGER_SERVICE = "com.android.server.pm.PackageManagerService";
	private static final String CLASS_PACKAGE_PARSER_PACKAGE = "android.content.pm.PackageParser.Package";

	private static final String REBOOT = "android.permission.REBOOT";
	private static final String INTERACT_ACROSS_USERS_FULL = "android.permission.INTERACT_ACROSS_USERS_FULL";
	private static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
	private static final String WRITE_SECURE_SETTINGS = "android.permission.WRITE_SECURE_SETTINGS";
	private static final String PERM_ACCESS_SURFACE_FLINGER = "android.permission.ACCESS_SURFACE_FLINGER";

	public static void initZygote(final XSharedPreferences prefs, final ClassLoader classLoader) {

		try {
			final Class<?> pmServiceClass = XposedHelpers.findClass(CLASS_PACKAGE_MANAGER_SERVICE, classLoader);

			XposedHelpers.findAndHookMethod(pmServiceClass, "grantPermissionsLPw", CLASS_PACKAGE_PARSER_PACKAGE,
					boolean.class, String.class, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							final String pkgName = (String) XposedHelpers.getObjectField(param.args[0], "packageName");
							if (Packages.XTOUCHWIZ.equals(pkgName)) {
							final Object extras = XposedHelpers.getObjectField(param.args[0], "mExtras");
							final Object ps = XposedHelpers.callMethod(extras, "getPermissionsState");
							final Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
							final Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");

								if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", REBOOT)) {
									final Object pAccess = XposedHelpers.callMethod(permissions, "get", REBOOT);
									XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
								}

								if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission",
										INTERACT_ACROSS_USERS_FULL)) {
									final Object pAccess = XposedHelpers.callMethod(permissions, "get",
											INTERACT_ACROSS_USERS_FULL);
									XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
								}

								if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", WRITE_SETTINGS)) {
									final Object pAccess = XposedHelpers.callMethod(permissions, "get", WRITE_SETTINGS);
									XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
								}

								if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission",
										WRITE_SECURE_SETTINGS)) {
									final Object pAccess = XposedHelpers.callMethod(permissions, "get",
											WRITE_SECURE_SETTINGS);
									XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
								}

								if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission",
										PERM_ACCESS_SURFACE_FLINGER)) {
									final Object pAccess = XposedHelpers.callMethod(permissions, "get",
											PERM_ACCESS_SURFACE_FLINGER);
									XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
								}
							}

						}
					});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

	}

}
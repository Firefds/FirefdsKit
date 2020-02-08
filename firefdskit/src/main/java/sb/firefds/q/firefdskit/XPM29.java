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
package sb.firefds.q.firefdskit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.q.firefdskit.utils.Packages.FIREFDSKIT;
import static sb.firefds.q.firefdskit.utils.Packages.SYSTEM_UI;

public class XPM29 {
    private static final String PERMISSION = "com.android.server.pm.permission";
    private static final String PERMISSION_MANAGER_SERVICE = PERMISSION + ".PermissionManagerService";
    private static final String PACKAGE_PARSER_PACKAGE = "android.content.pm.PackageParser.Package";
    private static final String PERMISSION_CALLBACK = PERMISSION + ".PermissionManagerServiceInternal.PermissionCallback";

    private static final String REBOOT = "android.permission.REBOOT";
    private static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static final String STATUSBAR = "android.permission.EXPAND_STATUS_BAR";
    private static final String RECOVERY = "android.permission.RECOVERY";
    private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";

    public static void doHook(final ClassLoader classLoader) {
        try {
            final Class<?> pmServiceClass = XposedHelpers.findClass(PERMISSION_MANAGER_SERVICE, classLoader);
            final Class<?> pmCallbackClass = XposedHelpers.findClass(PERMISSION_CALLBACK, classLoader);

            XposedHelpers.findAndHookMethod(pmServiceClass,
                    "restorePermissionState",
                    PACKAGE_PARSER_PACKAGE,
                    boolean.class,
                    String.class,
                    pmCallbackClass,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            final String pkgName = (String) XposedHelpers.getObjectField(param.args[0], "packageName");
                            if (FIREFDSKIT.equals(pkgName)) {
                                final Object extras = XposedHelpers.getObjectField(param.args[0], "mExtras");
                                final Object ps = XposedHelpers.callMethod(extras, "getPermissionsState");
                                final Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                                final Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");

                                if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", REBOOT)) {
                                    final Object pAccess = XposedHelpers.callMethod(permissions, "get", REBOOT);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
                                }

                                if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", STATUSBAR)) {
                                    final Object pAccess = XposedHelpers.callMethod(permissions, "get", STATUSBAR);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
                                }

                                if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", WRITE_SETTINGS)) {
                                    final Object pAccess = XposedHelpers.callMethod(permissions, "get", WRITE_SETTINGS);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
                                }

                                if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", RECOVERY)) {
                                    final Object pAccess = XposedHelpers.callMethod(permissions, "get", RECOVERY);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
                                }

                                if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", WRITE_EXTERNAL_STORAGE)) {
                                    final Object pAccess = XposedHelpers.callMethod(permissions, "get", WRITE_EXTERNAL_STORAGE);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
                                }

                                if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", RECORD_AUDIO)) {
                                    final Object pAccess = XposedHelpers.callMethod(permissions, "get", RECORD_AUDIO);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
                                }
                            }
                            if (SYSTEM_UI.equals(pkgName)) {
                                final Object extras = XposedHelpers.getObjectField(param.args[0], "mExtras");
                                final Object ps = XposedHelpers.callMethod(extras, "getPermissionsState");
                                final Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                                final Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");

                                if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", REBOOT)) {
                                    final Object pAccess = XposedHelpers.callMethod(permissions, "get", REBOOT);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
                                }

                                if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", RECOVERY)) {
                                    final Object pAccess = XposedHelpers.callMethod(permissions, "get", RECOVERY);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
                                }

                                if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", WRITE_EXTERNAL_STORAGE)) {
                                    final Object pAccess = XposedHelpers.callMethod(permissions, "get", WRITE_EXTERNAL_STORAGE);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
                                }

                                if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", RECORD_AUDIO)) {
                                    final Object pAccess = XposedHelpers.callMethod(permissions, "get", RECORD_AUDIO);
                                    XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
                                }
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

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
package sb.firefds.pie.firefdskit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.pie.firefdskit.utils.Packages.FIREFDSKIT;
import static sb.firefds.pie.firefdskit.utils.Packages.SYSTEM_UI;

public class XPM28 {
    private static final String PERMISSION = "com.android.server.pm.permission";
    private static final String PERMISSION_MANAGER_SERVICE = PERMISSION + ".PermissionManagerService";
    private static final String PACKAGE_PARSER_PACKAGE = "android.content.pm.PackageParser.Package";
    private static final String PERMISSION_CALLBACK = PERMISSION + ".PermissionManagerInternal.PermissionCallback";

    private static final String REBOOT = "android.permission.REBOOT";
    private static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static final String STATUSBAR = "android.permission.EXPAND_STATUS_BAR";
    private static final String RECOVERY = "android.permission.RECOVERY";

    public static void doHook(ClassLoader classLoader) {
        try {
            final Class<?> pmServiceClass = XposedHelpers.findClass(PERMISSION_MANAGER_SERVICE, classLoader);
            final Class<?> pmCallbackClass = XposedHelpers.findClass(PERMISSION_CALLBACK, classLoader);

            XposedHelpers.findAndHookMethod(pmServiceClass,
                    "grantPermissions",
                    PACKAGE_PARSER_PACKAGE,
                    boolean.class,
                    String.class,
                    pmCallbackClass,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            final String pkgName = (String) XposedHelpers.getObjectField(param.args[0], "packageName");
                            if (pkgName.equals(FIREFDSKIT) || pkgName.equals(SYSTEM_UI)) {
                                final Object extras = XposedHelpers.getObjectField(param.args[0], "mExtras");
                                final Object ps = XposedHelpers.callMethod(extras, "getPermissionsState");
                                final Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                                final Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");

                                switch (pkgName) {
                                    case FIREFDSKIT:
                                        grantPermission(ps, permissions, STATUSBAR);
                                        grantPermission(ps, permissions, WRITE_SETTINGS);
                                    case SYSTEM_UI:
                                        grantPermission(ps, permissions, REBOOT);
                                        grantPermission(ps, permissions, RECOVERY);
                                        break;
                                }
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void grantPermission(Object ps, Object permissions, String permission) {
        if (!(Boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", permission)) {
            final Object pAccess = XposedHelpers.callMethod(permissions, "get", permission);
            XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess);
        }
    }
}

/*
 * Copyright (C) 2020 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.r.firefdskit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.r.firefdskit.utils.Packages.FIREFDSKIT;
import static sb.firefds.r.firefdskit.utils.Packages.SYSTEM_UI;

public class XPM30 {
    private static final String PERMISSION = "com.android.server.pm.permission";
    private static final String PERMISSION_MANAGER_SERVICE = PERMISSION + ".PermissionManagerService";
    private static final String ANDROID_PACKAGE = "com.android.server.pm.parsing.pkg.AndroidPackage";
    private static final String PERMISSION_CALLBACK = PERMISSION + ".PermissionManagerServiceInternal" +
            ".PermissionCallback";

    private static final String REBOOT = "android.permission.REBOOT";
    private static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static final String STATUSBAR = "android.permission.EXPAND_STATUS_BAR";
    private static final String RECOVERY = "android.permission.RECOVERY";
    private static final String ACCESS_SCREEN_RECORDER_SVC = "com.samsung.android.app.screenrecorder.permission" +
            ".ACCESS_SCREEN_RECORDER_SVC";

    public static void doHook(ClassLoader classLoader) {
        try {
            final Class<?> pmServiceClass = XposedHelpers.findClass(PERMISSION_MANAGER_SERVICE, classLoader);
            final Class<?> pmCallbackClass = XposedHelpers.findClass(PERMISSION_CALLBACK, classLoader);

            XposedHelpers.findAndHookMethod(pmServiceClass,
                    "restorePermissionState",
                    ANDROID_PACKAGE,
                    boolean.class,
                    String.class,
                    pmCallbackClass,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            final String pkgName = (String) XposedHelpers.callMethod(param.args[0], "getPackageName");
                            if (pkgName.equals(FIREFDSKIT) || pkgName.equals(SYSTEM_UI)) {
                                XposedBridge.log("FFK: Package Name: " + pkgName);
                                final Object ps = XposedHelpers.callMethod(param.args[0], "getPermissionsState");
                                XposedBridge.log("FFK: getPermissionsState: " + ps);
                                final Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                                XposedBridge.log("FFK: mSettings: " + settings);
                                final Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");
                                XposedBridge.log("FFK: mPermissions: " + permissions);

                                switch (pkgName) {
                                    case FIREFDSKIT:
                                        grantPermission(ps, permissions, STATUSBAR);
                                        grantPermission(ps, permissions, WRITE_SETTINGS);
                                    case SYSTEM_UI:
                                        grantPermission(ps, permissions, REBOOT);
                                        grantPermission(ps, permissions, RECOVERY);
                                        grantPermission(ps, permissions, ACCESS_SCREEN_RECORDER_SVC);
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
            XposedBridge.log("FFK: Inside hasInstallPermission");
            final Object pAccess = XposedHelpers.callMethod(permissions, "get", permission);
            XposedBridge.log("FFK: grantInstallPermission for " + pAccess + "returns " + XposedHelpers.callMethod(ps, "grantInstallPermission", pAccess));
        }
    }
}

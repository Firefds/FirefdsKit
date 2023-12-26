/*
 * Copyright (C) 2023 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.u.firefdskit;

import static sb.firefds.u.firefdskit.utils.Packages.FIREFDSKIT;
import static sb.firefds.u.firefdskit.utils.Packages.SYSTEM_UI;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XPM34 {
    private static final String PERMISSION = "com.android.server.pm.permission";
    private static final String PERMISSION_MANAGER_SERVICE = PERMISSION + ".PermissionManagerServiceImpl";
    private static final String ANDROID_PACKAGE = "com.android.server.pm.pkg.AndroidPackage";
    private static final String PERMISSION_CALLBACK = PERMISSION_MANAGER_SERVICE + ".PermissionCallback";

    private static final String REBOOT = "android.permission.REBOOT";
    private static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static final String STATUSBAR = "android.permission.EXPAND_STATUS_BAR";
    private static final String RECOVERY = "android.permission.RECOVERY";
    private static final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
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
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            final Object pkg = param.args[0];
                            final String pkgName = (String) XposedHelpers.callMethod(param.args[0], "getPackageName");
                            if (pkgName.equals(FIREFDSKIT) || pkgName.equals(SYSTEM_UI)) {
                                final Object mRegistry = XposedHelpers.getObjectField(param.thisObject, "mRegistry");
                                switch (pkgName) {
                                    case FIREFDSKIT:
                                        grantInstallPermission(mRegistry, STATUSBAR, pkg, param.thisObject);
                                        grantInstallPermission(mRegistry, WRITE_SETTINGS, pkg, param.thisObject);
                                        grantInstallPermission(mRegistry, POST_NOTIFICATIONS, pkg, param.thisObject);
                                    case SYSTEM_UI:
                                        grantInstallPermission(mRegistry, REBOOT, pkg, param.thisObject);
                                        grantInstallPermission(mRegistry, RECOVERY, pkg, param.thisObject);
                                        grantInstallPermission(mRegistry, ACCESS_SCREEN_RECORDER_SVC, pkg,
                                                param.thisObject);
                                        break;
                                }
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void grantInstallPermission(Object mRegistry,
                                               String permission,
                                               Object pkg,
                                               Object permissionManager) {
        Object bp = XposedHelpers.callMethod(mRegistry, "getPermission", permission);
        Object uidState = XposedHelpers.callMethod(permissionManager, "getUidStateLocked", pkg, 0);
        XposedHelpers.callMethod(uidState, "grantPermission", bp);
    }
}

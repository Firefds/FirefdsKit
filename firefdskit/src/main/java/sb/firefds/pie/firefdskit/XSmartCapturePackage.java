package sb.firefds.pie.firefdskit;

import android.content.Context;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_SCREEN_RECORDER_IN_CALL;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_SCREEN_RECORDER_TILE;

public class XSmartCapturePackage {

    private static final String SCREEN_RECORDER_CONTROLLER$1 = "com.samsung.android.app.screenrecorder.ScreenRecorderController$1";
    private static final String SCREEN_RECORDER_UTILS = "com.samsung.android.app.screenrecorder.util.Utils";
    private static final String SMART_CAPTURE_UTILS = "com.samsung.android.app.util.SmartCaptureUtils";
    private static final String PACKAGE_PARSER = "android.content.pm.PackageParser";

    public static void doHook(XSharedPreferences prefs, XC_LoadPackage.LoadPackageParam lparam) {

        if (prefs.getBoolean(PREF_ENABLE_SCREEN_RECORDER_TILE, false)) {
            try {
                XposedHelpers.findAndHookMethod(SMART_CAPTURE_UTILS,
                        lparam.classLoader,
                        "isScreenRecorderEnabled",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }

        if (prefs.getBoolean(PREF_ENABLE_SCREEN_RECORDER_IN_CALL, false)) {
            String packageVersion = getPackageVersion(lparam).split("\\.")[0];
            String[] methodsToHook = new String[2];

            switch (packageVersion) {
                case "4":
                    methodsToHook[0] = "isDuringCallState";
                    methodsToHook[1] = "isReceivingCallState";
                    break;
                case "3":
                    methodsToHook[0] = "isPhoneCallingState";
                    methodsToHook[1] = "isVoIPCallState";
                    break;
                default:
                    XposedBridge.log("Invalid version of SmartCapture - " + packageVersion);
                    return;
            }

            try {
                XposedHelpers.findAndHookMethod(SCREEN_RECORDER_CONTROLLER$1,
                        lparam.classLoader,
                        "onCallStateChanged",
                        int.class,
                        String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                param.setResult(null);
                            }
                        });

                XposedHelpers.findAndHookMethod(SCREEN_RECORDER_UTILS,
                        lparam.classLoader,
                        methodsToHook[0],
                        Context.class,
                        XC_MethodReplacement.returnConstant(Boolean.FALSE));

                XposedHelpers.findAndHookMethod(SCREEN_RECORDER_UTILS,
                        lparam.classLoader,
                        methodsToHook[1],
                        Context.class,
                        XC_MethodReplacement.returnConstant(Boolean.FALSE));
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }
    }

    private static String getPackageVersion(XC_LoadPackage.LoadPackageParam lparam) {
        try {
            Class<?> parserCls = XposedHelpers.findClass(PACKAGE_PARSER, lparam.classLoader);
            Object parser = parserCls.newInstance();
            File apkPath = new File(lparam.appInfo.sourceDir);
            Object pkg = XposedHelpers.callMethod(parser, "parsePackage", apkPath, 0);
            return (String) XposedHelpers.getObjectField(pkg, "mVersionName");
        } catch (Throwable e) {
            return "(unknown)";
        }
    }
}

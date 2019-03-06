package sb.firefds.pie.firefdskit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Packages;

public class XMtpApplication {

    private static ClassLoader classLoader;

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        XMtpApplication.classLoader = classLoader;

        if (prefs.getBoolean("hideMTPNotification", false)) {
            try {
                hideMTPNotification();
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }

    private static void hideMTPNotification() {
        try {
            XposedHelpers.findAndHookMethod(Packages.MTP_APPLICATION + ".USBConnection",
                    classLoader,
                    "showDiaglog",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Object mReceiver = XposedHelpers.getObjectField(param.thisObject, "mReceiver");
                            XposedHelpers.callMethod(mReceiver, "changeMtpMode");
                            XposedHelpers.callMethod(param.thisObject, "finish");
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}
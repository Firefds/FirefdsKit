package sb.firefds.pie.firefdskit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Packages;

import static sb.firefds.pie.firefdskit.utils.Preferences.*;

public class XMtpApplication {

    private static ClassLoader classLoader;
    private static final String USB_CONNECTION = Packages.MTP_APPLICATION + ".USBConnection";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        XMtpApplication.classLoader = classLoader;

        if (prefs.getBoolean(PREF_HIDE_MTP_NOTIFICATION, false)) {
            try {
                hideMTPNotification();
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }

    private static void hideMTPNotification() {
        try {
            XposedHelpers.findAndHookMethod(USB_CONNECTION,
                    classLoader,
                    "showDiaglog",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
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
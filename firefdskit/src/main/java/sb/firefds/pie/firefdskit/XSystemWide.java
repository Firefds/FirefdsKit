package sb.firefds.pie.firefdskit;

import android.os.PowerManager;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DEFAULT_REBOOT_BEHAVIOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DISABLE_SECURE_FLAG;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS;

public class XSystemWide {

    private final static String WIFI_AP_CUST_CLASS = "android.net.wifi.WifiApCust";

    public static void doHook(final XSharedPreferences prefs) {

        try {
            if (prefs.getBoolean(PREF_DISABLE_SECURE_FLAG, false)) {

                XposedHelpers.findAndHookMethod(Window.class,
                        "setFlags",
                        int.class,
                        int.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                Integer flags = (Integer) param.args[0];
                                flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                                param.args[0] = flags;
                            }
                        });

                XposedHelpers.findAndHookMethod(SurfaceView.class,
                        "setSecure",
                        boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                param.args[0] = false;
                            }
                        });
            }

            if (prefs.getBoolean(PREF_DEFAULT_REBOOT_BEHAVIOR, false)) {
                XposedHelpers.findAndHookMethod(PowerManager.class,
                        "reboot",
                        String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (((String) param.args[0]).isEmpty()) {
                                    param.args[0] = "recovery";
                                }
                            }
                        });
            }

            if (prefs.getBoolean(PREF_ENABLE_ADVANCED_HOTSPOT_OPTIONS, false)) {
                Class<?> WifiApCustClass = XposedHelpers.findClass(WIFI_AP_CUST_CLASS, null);
                XposedHelpers.setStaticBooleanField(WifiApCustClass, "mSupportMaxClientMenu", true);
                XposedHelpers.setStaticBooleanField(WifiApCustClass, "mSupport5G", true);
                XposedHelpers.setStaticBooleanField(WifiApCustClass, "mSupport5GBasedOnCountry", true);
                XposedHelpers.setStaticObjectField(WifiApCustClass, "mRegion", "NA");
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

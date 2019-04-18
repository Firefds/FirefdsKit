package sb.firefds.pie.firefdskit;


import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_MAKE_OFFICIAL;

public class XFotaAgentPackage {

    private static final String DEVICE_UTILS = "com.samsung.android.fem.common.util.DeviceUtils";

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        try {
            if (prefs.getBoolean(PREF_MAKE_OFFICIAL, true)) {
                XposedHelpers.findAndHookMethod(DEVICE_UTILS,
                        classLoader,
                        "isRootingDevice",
                        boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                param.setResult(false);
                            }
                        });
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}


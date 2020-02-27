package sb.firefds.q.firefdskit;


import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.q.firefdskit.utils.Preferences.PREF_MAKE_OFFICIAL;

public class XFotaAgentPackage {

    private static final String DEVICE_UTILS_OLD = "com.samsung.android.fem.common.util.DeviceUtils";
    private static final String DEVICE_UTILS_NEW = "com.idm.fotaagent.enabler.utils.DeviceUtils";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {


        if (prefs.getBoolean(PREF_MAKE_OFFICIAL, true)) {
            try {
                XposedHelpers.findAndHookMethod(DEVICE_UTILS_OLD,
                        classLoader,
                        "isRootingDevice",
                        boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                param.setResult(false);
                            }
                        });

            } catch (Throwable e) {
                XposedBridge.log("FFK: " + DEVICE_UTILS_OLD + " not found. Trying " + DEVICE_UTILS_NEW);
                try {
                    XposedHelpers.findAndHookMethod(DEVICE_UTILS_NEW,
                            classLoader,
                            "isRootingDevice",
                            boolean.class,
                            new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) {
                                    param.setResult(false);
                                }
                            });

                } catch (Throwable e1) {
                    XposedBridge.log(e1);
                }
            }
        }
    }
}


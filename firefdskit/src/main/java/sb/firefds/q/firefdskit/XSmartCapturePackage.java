package sb.firefds.q.firefdskit;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.q.firefdskit.utils.Preferences.PREF_ENABLE_SCREEN_RECORDER_IN_CALL;

public class XSmartCapturePackage {

    private static final String SCREEN_RECORDER_CONTROLLER$1 = "com.samsung.android.app.screenrecorder.ScreenRecorderController$1";
    private static final String SCREEN_RECORDER_UTILS = "com.samsung.android.app.screenrecorder.util.Utils";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        if (prefs.getBoolean(PREF_ENABLE_SCREEN_RECORDER_IN_CALL, false)) {
            try {
                XposedHelpers.findAndHookMethod(SCREEN_RECORDER_CONTROLLER$1,
                        classLoader,
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
                        classLoader,
                        "isDuringCallState",
                        Context.class,
                        XC_MethodReplacement.returnConstant(Boolean.FALSE));

                XposedHelpers.findAndHookMethod(SCREEN_RECORDER_UTILS,
                        classLoader,
                        "isReceivingCallState",
                        Context.class,
                        XC_MethodReplacement.returnConstant(Boolean.FALSE));
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }
    }
}

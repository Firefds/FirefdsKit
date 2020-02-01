package sb.firefds.q.firefdskit;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DISABLE_NUMBER_FORMATTING;

public class XContactsPackage {
    private static final String CSC_FEATURE_UTIL = "com.samsung.android.dialtacts.util.CscFeatureUtil";

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        try {
            XposedHelpers.findAndHookMethod(CSC_FEATURE_UTIL,
                    classLoader,
                    "getDisablePhoneNumberFormatting",
                    XC_MethodReplacement.returnConstant(prefs
                            .getBoolean(PREF_DISABLE_NUMBER_FORMATTING, false)));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

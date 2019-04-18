package sb.firefds.pie.firefdskit;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Packages;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_BLOCKED_PHRASES;

public class XMessagingPackage {

    private static final String FEATURE = Packages.SAMSUNG_MESSAGING + ".common.configuration.Feature";

    public static void doHook(final XSharedPreferences prefs, ClassLoader classLoader) {

        final Class<?> messagingFeatureClass
                = XposedHelpers.findClass(FEATURE, classLoader);

        if (prefs.getBoolean(PREF_ENABLE_BLOCKED_PHRASES, false)) {
            try {
                XposedHelpers.findAndHookMethod(messagingFeatureClass,
                        "getEnableSpamReport4Kor",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));

                XposedHelpers.findAndHookMethod(messagingFeatureClass,
                        "isKorModel",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }
}

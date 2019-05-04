package sb.firefds.pie.firefdskit;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.pie.firefdskit.utils.Packages.SAMSUNG_MESSAGING;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DISABLE_SMS_TO_MMS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_BLOCKED_PHRASES;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_FORCE_MMS_CONNECT;

public class XMessagingPackage {

    private static final String FEATURE = SAMSUNG_MESSAGING + ".common.configuration.Feature";

    public static void doHook(final XSharedPreferences prefs, ClassLoader classLoader) {

        boolean disableSmsToMms = prefs.getBoolean(PREF_DISABLE_SMS_TO_MMS, false);
        final Class<?> messagingFeatureClass = XposedHelpers.findClass(FEATURE, classLoader);

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

        try {
            XposedHelpers.findAndHookMethod(messagingFeatureClass,
                    "getEnableMmsOnMobileDataOff",
                    XC_MethodReplacement.returnConstant(prefs.getBoolean(PREF_FORCE_MMS_CONNECT, false)));

            XposedHelpers.findAndHookMethod(messagingFeatureClass,
                    "getSmsToMmsByThreshold",
                    XC_MethodReplacement.returnConstant(!disableSmsToMms));

            XposedHelpers.findAndHookMethod(messagingFeatureClass,
                    "getSmsMaxByte",
                    XC_MethodReplacement.returnConstant(disableSmsToMms ? 999 : 140));

            XposedHelpers.findAndHookMethod(messagingFeatureClass,
                    "getMaxPhoneNumberLength",
                    XC_MethodReplacement.returnConstant(999));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

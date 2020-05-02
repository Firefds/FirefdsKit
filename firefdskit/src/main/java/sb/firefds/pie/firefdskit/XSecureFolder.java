package sb.firefds.pie.firefdskit;

import android.content.Context;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_SECURE_FOLDER;

public class XSecureFolder {

    private static final String SETUP_WIZARD_UTILS = "com.samsung.knox.securefolder.setupwizard.Utils";

    public static void doHook(XSharedPreferences prefs, ClassLoader classloader) {

        try {
            if (prefs.getBoolean(PREF_ENABLE_SECURE_FOLDER, false)) {
                XposedHelpers.findAndHookMethod(SETUP_WIZARD_UTILS,
                        classloader,
                        "isDeviceTrustable",
                        Context.class,
                        XC_MethodReplacement.returnConstant(true));
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }
}

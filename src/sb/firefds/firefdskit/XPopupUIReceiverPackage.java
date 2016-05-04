package sb.firefds.firefdskit;

import android.content.Context;
import android.view.View;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.firefdskit.utils.Packages;

public class XPopupUIReceiverPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {
		if (prefs.getBoolean("disableUSBCover", false)) {
			try {
				XposedHelpers.findAndHookMethod(Packages.POPUPUIRECEIVER + ".PopupuiService", classLoader,
						"showUSBCDetacheddDialog", Context.class, XC_MethodReplacement.DO_NOTHING);
			} catch (Throwable e) {
			}
			
			try {
				XposedHelpers.findAndHookMethod(Packages.POPUPUIRECEIVER + ".PopupuiService", classLoader,
						"showUSBCoverDialog", Context.class, XC_MethodReplacement.DO_NOTHING);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (prefs.getBoolean("disableBatteryCover", false)) {
			try {
				XposedHelpers.findAndHookMethod(Packages.POPUPUIRECEIVER + ".BatteryCover", classLoader,
						"showPopupBatteryCover", new XC_MethodReplacement() {

							@Override
							protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
								XposedHelpers.callMethod(param.thisObject, "closeBatterycoverPopup", (View) null);
								return null;
							}
						});
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}
	}

}
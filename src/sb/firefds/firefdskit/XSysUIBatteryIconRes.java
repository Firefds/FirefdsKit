package sb.firefds.firefdskit;

import android.content.res.XModuleResources;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

public class XSysUIBatteryIconRes {

	public static void doHook(XSharedPreferences prefs, InitPackageResourcesParam resparam,
			XModuleResources moduleResources) {

		if (prefs.getBoolean("selectedBatteryIcon", false) || prefs.getBoolean("hideBatteryIcon", false)) {
			try {
				XBatteryStyle.initResources(prefs, resparam);
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

	}

}

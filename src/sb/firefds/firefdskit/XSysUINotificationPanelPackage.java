package sb.firefds.firefdskit;

import sb.firefds.firefdskit.utils.Utils;
import android.view.View;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSysUINotificationPanelPackage {

	private static XSharedPreferences prefs;
	private static ClassLoader classLoader;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XSysUINotificationPanelPackage.prefs = prefs;
		XSysUINotificationPanelPackage.classLoader = classLoader;


		if (!prefs.getBoolean("hideCarrierLabel", false) 
				|| prefs.getBoolean("hideCarrierLabel", false)
				|| !prefs.getString("customCarrierLabel", "").isEmpty()
				|| !prefs.getString("carrierSize", "Medium").equals("Medium")) {
			try {
				handleMakeCarrierStatusBarView();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}
	}

	private static void handleMakeCarrierStatusBarView() {
		try {

			XposedHelpers.findAndHookMethod("com.android.keyguard.CarrierText", classLoader, "updateCarrierText",
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {

							TextView tvCarrier = (TextView) param.thisObject;

							if (prefs.getBoolean("hideCarrierLabel", false)
									|| !prefs.getString("customCarrierLabel", "").isEmpty()) {
								String networkName = prefs.getString("customCarrierLabel", "");

								if (prefs.getBoolean("hideCarrierLabel", false)) {
									tvCarrier.setVisibility(View.GONE);
								} else if (networkName.isEmpty()) {
									return;
								} else {
									tvCarrier.setText(networkName);
								}
							}

							int textSize = 16;
							String tsPrefVal = prefs.getString("carrierSize", "Medium");
							if (tsPrefVal.equalsIgnoreCase("Small")) {
								textSize = 14;
							} else if (tsPrefVal.equalsIgnoreCase("Large")) {
								textSize = 18;
							} else if (tsPrefVal.equalsIgnoreCase("Larger")) {
								textSize = 19;
							} else if (tsPrefVal.equalsIgnoreCase("Largest")) {
								textSize = 20;
							}
							tvCarrier.setTextSize(textSize);
							tvCarrier = Utils.setTypeface(prefs, tvCarrier);
						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}
}

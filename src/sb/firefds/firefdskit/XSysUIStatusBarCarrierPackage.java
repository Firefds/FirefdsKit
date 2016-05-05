package sb.firefds.firefdskit;

import sb.firefds.firefdskit.utils.Packages;
import sb.firefds.firefdskit.utils.Utils;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSysUIStatusBarCarrierPackage {

	private static XSharedPreferences prefs;
	private static ClassLoader classLoader;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		XSysUIStatusBarCarrierPackage.prefs = prefs;
		XSysUIStatusBarCarrierPackage.classLoader = classLoader;

		if (!prefs.getString("carrierSize", "Medium").equals("Medium")) {
			try {
				setTypefaceSizeAndColor();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}
		}

		if (prefs.getBoolean("hideCarrierLabel", false) || !prefs.getString("customCarrierLabel", "").isEmpty()) {
			try {
				setTextAndVisibility();
			} catch (Throwable e) {
				XposedBridge.log(e.toString());
			}
		}
	}

	private static void setTextAndVisibility() {
		try {
			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.policy.NetworkControllerImpl",
					classLoader, "updateNetworkName", boolean.class, String.class, boolean.class, String.class,
					new XC_MethodHook() {

						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {

							String networkName = prefs.getString("customCarrierLabel", "");

							if (prefs.getBoolean("hideCarrierLabel", false)) {
								networkName = "";
							} else if (networkName.isEmpty()) {
								return;
							}
							XposedHelpers.setObjectField(param.thisObject, "mNetworkName", networkName);
							XposedHelpers.setObjectField(param.thisObject, "mATTNetworkName", networkName);

						}
					});
		} catch (Throwable e) {
		}
	}

	private static void setTypefaceSizeAndColor() {

		try {
			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.policy.NetworkControllerImpl",
					classLoader, "addMobileLabelView", TextView.class, new XC_MethodHook() {

						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							TextView textView = (TextView) param.args[0];
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
							textView.setTextSize(textSize);
							textView = Utils.setTypeface(prefs, textView);

						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());
		}

		try {
			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.policy.NetworkControllerImpl",
					classLoader, "addATTMobileLabelView", TextView.class, new XC_MethodHook() {

						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							TextView textView = (TextView) param.args[0];
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
							textView.setTextSize(textSize);
							textView = Utils.setTypeface(prefs, textView);

						}
					});
		} catch (Throwable e) {
			XposedBridge.log(e.toString());
		}
	}

}

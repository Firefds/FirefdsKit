package sb.firefds.pie.firefdskit;

import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Utils;

public class XSysUINotificationPanelPackage {

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        try {
            XposedBridge.log("Perf Boolean out: " + prefs.getBoolean("hideCarrierLabel", false));
            XposedHelpers.findAndHookMethod("com.android.keyguard.CarrierText",
                    classLoader, "updateCarrierText",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            TextView tvCarrier = (TextView) param.thisObject;

                            prefs.reload();
                            XposedBridge.log("Perf Boolean: " + prefs.getBoolean("hideCarrierLabel", false));
                            if (prefs.getBoolean("hideCarrierLabel", false)) {
                                tvCarrier.setText(" ");
                            }

                            int textSize = 14;
                            String tsPrefVal = prefs.getString("carrierSize", "Small");
                            if (tsPrefVal.equalsIgnoreCase("Medium")) {
                                textSize = 16;
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
            XposedBridge.log(e);
        }
    }
}

package sb.firefds.oreo.firefdskit;

import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.oreo.firefdskit.utils.Utils;

public class XSysUINotificationPanelPackage {

    public static void doHook(final XSharedPreferences prefs, ClassLoader classLoader) {

        try {
            XposedHelpers.findAndHookMethod("com.android.keyguard.CarrierText", classLoader, "updateCarrierText",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            TextView tvCarrier = (TextView) param.thisObject;

                            prefs.reload();
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
                            Utils.setTypeface(prefs, tvCarrier);
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e.toString());
        }
    }
}

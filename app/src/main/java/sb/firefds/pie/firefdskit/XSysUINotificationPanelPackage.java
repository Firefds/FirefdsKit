package sb.firefds.pie.firefdskit;

import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Packages;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Preferences.*;

public class XSysUINotificationPanelPackage {

    private static final String LTE_INSTEAD_OF_4G = "STATBAR_DISPLAY_LTE_INSTEAD_OF_4G_ICON";
    private static final String FOUR_G_PLUS_INSTEAD_OF_4G =
            "STATBAR_DISPLAY_4G_PLUS_INSTEAD_OF_4G_ICON";
    private static final String FOUR_G_INSTEAD_OF_4G_PLUS =
            "STATBAR_DISPLAY_4G_INSTEAD_OF_4G_PLUS_ICON";
    private static final String FOUR_HALF_G_INSTEAD_OF_4G_PLUS =
            "STATBAR_DISPLAY_4_HALF_G_INSTEAD_OF_4G_PLUS_ICON";
    private static final String RUNE = Packages.SYSTEM_UI + ".Rune";
    private static final String CARRIER_TEXT = "com.android.keyguard.CarrierText";
    private static final String DATA_USAGE_BAR = Packages.SYSTEM_UI + ".bar.DataUsageBar";
    private static final String NETSPEED_VIEW =
            Packages.SYSTEM_UI + ".statusbar.policy.NetspeedView";
    private static final String MOBILE_SIGNAL_CONTROLLER_CLASS =
            Packages.SYSTEM_UI + ".statusbar.policy.MobileSignalController";

    private static String dataIconBehavior;
    private static ClassLoader classLoader;

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        XSysUINotificationPanelPackage.classLoader = classLoader;
        final Class<?> systemUIRuneClass = XposedHelpers.findClass(RUNE, classLoader);

        try {
            XposedHelpers.findAndHookMethod(CARRIER_TEXT,
                    classLoader,
                    "updateCarrierText",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            TextView tvCarrier = (TextView) param.thisObject;

                            prefs.reload();
                            if (prefs.getBoolean(PREF_HIDE_CARRIER_LABEL, false)) {
                                tvCarrier.setText(" ");
                            }

                            int textSize = 14;
                            String tsPrefVal = prefs.getString(PREF_CARRIER_SIZE, "Small");
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
            XposedBridge.log(e);
        }

        dataIconBehavior = prefs.getString(PREF_DATA_ICON_BEHAVIOR, "0");
        if (!dataIconBehavior.equals("0")) {
            changeDataIcon(systemUIRuneClass);
        }

        try {
            XposedHelpers.findAndHookMethod(DATA_USAGE_BAR,
                    classLoader,
                    "isAvailable",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            prefs.reload();
                            return prefs.getBoolean(PREF_DATA_USAGE_VIEW, false);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (prefs.getBoolean(PREF_SHOW_NETWORK_SPEED_MENU, false)) {
            try {
                XposedHelpers.findAndHookMethod(NETSPEED_VIEW,
                        classLoader,
                        "onAttachedToWindow",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                XposedHelpers.setStaticBooleanField(systemUIRuneClass,
                                        "STATBAR_SUPPORT_REAL_TIME_NETWORK_SPEED",
                                        true);
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void changeDataIcon(Class<?> aClass) {
        try {
            XC_MethodHook mobileSignalMethodHook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    switch (dataIconBehavior) {
                        case "1":
                            XposedHelpers.setStaticBooleanField(aClass,
                                    LTE_INSTEAD_OF_4G,
                                    true);
                            break;
                        case "2":
                            XposedHelpers.setStaticBooleanField(aClass,
                                    FOUR_G_PLUS_INSTEAD_OF_4G,
                                    true);
                            break;
                        case "3":
                            XposedHelpers.setStaticBooleanField(aClass,
                                    FOUR_G_INSTEAD_OF_4G_PLUS,
                                    true);
                            break;
                        case "4":
                            XposedHelpers.setStaticBooleanField(aClass,
                                    FOUR_HALF_G_INSTEAD_OF_4G_PLUS,
                                    true);
                            break;
                    }
                }
            };

            XposedHelpers.findAndHookMethod(MOBILE_SIGNAL_CONTROLLER_CLASS,
                    classLoader,
                    "updateMobileIconGroup",
                    mobileSignalMethodHook);
            XposedHelpers.findAndHookMethod(MOBILE_SIGNAL_CONTROLLER_CLASS,
                    classLoader,
                    "updateTelephony",
                    mobileSignalMethodHook);

        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

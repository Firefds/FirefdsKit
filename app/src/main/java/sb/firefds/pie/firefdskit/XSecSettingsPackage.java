/*
 * Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sb.firefds.pie.firefdskit;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserManager;
import android.util.AttributeSet;
import android.util.Xml;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.utils.Packages;
import sb.firefds.pie.firefdskit.utils.Utils;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Constructor;

import static sb.firefds.pie.firefdskit.utils.Preferences.*;

public class XSecSettingsPackage {

    private static final String BLUETOOTH_SCAN_DIALOG =
            Packages.SAMSUNG_SETTINGS + ".bluetooth.BluetoothScanDialog";
    private static final String SEC_ACCOUNT_TILES =
            Packages.SAMSUNG_SETTINGS + ".qstile.SecAccountTiles";
    private static final String SYSCOPE_STATUS_PREFERENCE_CONTROLLER =
            Packages.SAMSUNG_SETTINGS + ".deviceinfo.status.SyscopeStatusPreferenceController";
    private static final String SEC_DEVICE_INFO_UTILS =
            Packages.SAMSUNG_SETTINGS + ".deviceinfo.SecDeviceInfoUtils";
    private static final String STATUS_BAR = Packages.SAMSUNG_SETTINGS + ".display.StatusBar";
    private static final String NAVIGATION_BAR_SETTINGS =
            Packages.SAMSUNG_SETTINGS + ".navigationbar.NavigationBarSettings";
    private static final String NAVIGATIONBAR_COLOR_PREFERENCE =
            Packages.SAMSUNG_SETTINGS + ".navigationbar.NavigationbarColorPreference";


    private static ClassLoader classLoader;
    private static XSharedPreferences prefs;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static int[] colorArray;
    private static Class<?> NavigationBarSettings;
    private static Class<?> NavigationBarColorPreference;

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        XSecSettingsPackage.classLoader = classLoader;
        XSecSettingsPackage.prefs = prefs;

        try {
            NavigationBarSettings = XposedHelpers.findClass(NAVIGATION_BAR_SETTINGS, classLoader);
            NavigationBarColorPreference =
                    XposedHelpers.findClass(NAVIGATIONBAR_COLOR_PREFERENCE, classLoader);

            addNavigationBarColorPreference();
            createColorPalette();

        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        if (prefs.getBoolean(PREF_MAKE_OFFICIAL, true)) {
            makeOfficial();
        }

        if (prefs.getBoolean(PREF_SHOW_NETWORK_SPEED_MENU, false)) {
            showNetworkSpeedMenu();
        }

        try {
            XposedHelpers.findAndHookMethod(
                    BLUETOOTH_SCAN_DIALOG,
                    classLoader,
                    "onCreate",
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            prefs.reload();
                            if (prefs.getBoolean(PREF_DISABLE_BLUETOOTH_DIALOG, false))
                                ((android.app.Activity) param.thisObject).finish();
                        }
                    });

        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(SEC_ACCOUNT_TILES,
                    classLoader,
                    "showConfirmPopup",
                    boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if (prefs.getBoolean(PREF_DISABLE_SYNC_DIALOG, false)) {
                                ContentResolver.setMasterSyncAutomatically((Boolean) param.args[0]);
                                param.setResult(null);
                            }
                        }
                    });

            if (prefs.getBoolean(PREF_SUPPORTS_MULTIPLE_USERS, false)) {
                XposedHelpers.findAndHookMethod(UserManager.class, "supportsMultipleUsers",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                param.setResult(true);
                            }
                        });

                XposedHelpers.findAndHookMethod(UserManager.class, "getMaxSupportedUsers",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                param.setResult(prefs.getInt(PREF_MAX_SUPPORTED_USERS, 3));
                            }
                        });
            }

        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void makeOfficial() {
        try {
            XposedHelpers.findAndHookMethod(SYSCOPE_STATUS_PREFERENCE_CONTROLLER,
                    classLoader,
                    "getICDVerification",
                    XC_MethodReplacement.returnConstant(1));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(SEC_DEVICE_INFO_UTILS,
                    classLoader,
                    "checkRootingCondition",
                    XC_MethodReplacement.returnConstant(Boolean.FALSE));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(SEC_DEVICE_INFO_UTILS,
                    classLoader,
                    "isSupportRootBadge",
                    Context.class,
                    XC_MethodReplacement.returnConstant(Boolean.FALSE));
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void showNetworkSpeedMenu() {
        try {
            XposedHelpers.findAndHookMethod(STATUS_BAR,
                    classLoader,
                    "onCreate",
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedHelpers.setStaticBooleanField(param.thisObject.getClass(),
                                    "isSupportNetworkSpeedFeature",
                                    true);
                        }
                    });

            XposedHelpers.findAndHookMethod(STATUS_BAR,
                    classLoader,
                    "onResume",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedHelpers.setStaticBooleanField(param.thisObject.getClass(),
                                    "isSupportNetworkSpeedFeature",
                                    true);
                        }
                    });

        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void addNavigationBarColorPreference() {
        try {
            XposedHelpers.findAndHookMethod(NavigationBarSettings,
                    "initUI",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Context ctx = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                            Resources res = ctx.getResources();
                            Context gbContext = Utils.getGbContext(ctx, res.getConfiguration());
                            Constructor constructor = NavigationBarColorPreference.getDeclaredConstructor(Context.class, AttributeSet.class);

                            XmlPullParser parser = gbContext.getResources().getXml(R.xml.org_navigationbar_color_preference);
                            parser.next();
                            parser.nextTag();

                            AttributeSet attr = Xml.asAttributeSet(parser);
                            Object navigationColorPreference = constructor.newInstance(ctx, attr);
                            Object preferenceScreen = XposedHelpers.callMethod(param.thisObject, "getPreferenceScreen");
                            XposedHelpers.callMethod(preferenceScreen, "addPreference", navigationColorPreference);
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void createColorPalette() {
        try {
            XposedBridge.hookAllConstructors(NavigationBarColorPreference, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mContext = (Context) param.args[0];
                    Context gbContext = mContext.createPackageContext(Packages.FIREFDSKIT,
                            Context.CONTEXT_IGNORE_SECURITY);
                    Resources gbRes = gbContext.getResources();
                    colorArray = gbRes.getIntArray(R.array.navigationbar_color_values);
                    colorArray[7] = prefs.getInt(PREF_NAVIGATION_BAR_COLOR, 0);
                    XposedHelpers.setObjectField(param.thisObject, "color_value", colorArray);
                }
            });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}

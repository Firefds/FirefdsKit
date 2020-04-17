/*
 * Copyright (C) 2020 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.q.firefdskit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.Display;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static sb.firefds.q.firefdskit.utils.Packages.SYSTEM_UI;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_CLOCK_DATE_ON_RIGHT;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_CLOCK_DATE_PREFERENCE;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DISABLE_EYE_STRAIN_DIALOG;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DISABLE_LOW_BATTERY_SOUND;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DISABLE_VOLUME_CONTROL_SOUND;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DISABLE_VOLUME_WARNING;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_ENABLE_BIOMETRICS_UNLOCK;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_ENABLE_SAMSUNG_BLUR;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_HIDE_CHARGING_NOTIFICATION;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_MAX_SUPPORTED_USERS;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_SHOW_CLOCK_SECONDS;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_STATUSBAR_DOUBLE_TAP;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_SUPPORTS_MULTIPLE_USERS;

public class XSysUIFeaturePackage {

    private static final String CUSTOM_SDK_MONITOR = SYSTEM_UI + ".KnoxStateMonitor.CustomSdkMonitor";
    private static final String TOGGLE_SLIDER_VIEW = SYSTEM_UI + ".settings.ToggleSliderView";
    private static final String VOLUME_DIALOG_CONTROLLER_IMPL = SYSTEM_UI + ".volume.VolumeDialogControllerImpl";
    private static final String KEYGUARD_STRONG_AUTH_TRACKER = "com.android.keyguard.KeyguardUpdateMonitor.StrongAuthTracker";
    private static final String QS_CLOCK = SYSTEM_UI + ".statusbar.policy.QSClock";
    private static final String STATUS_BAR_WINDOW_CONTROLLER = SYSTEM_UI + ".statusbar.phone.StatusBarWindowController";
    private static final String STATE = STATUS_BAR_WINDOW_CONTROLLER + ".State";
    private static final String POWER_NOTIFICATION_WARNINGS = SYSTEM_UI + ".power.PowerNotificationWarnings";
    private static final String SETTINGS_HELPER = SYSTEM_UI + ".util.SettingsHelper";
    private static final String SEC_VOLUME_DIALOG_IMPL = SYSTEM_UI + ".volume.SecVolumeDialogImpl";
    private static final String NOTIFICATION_PLAYER = SYSTEM_UI + ".media.NotificationPlayer";

    @SuppressLint("StaticFieldLeak")
    private static TextView mClock;
    private static SimpleDateFormat mSecondsFormat;
    private static Handler mSecondsHandler;
    private static Class<?> qsClock;
    private static Method updateClock;

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        try {
            if (prefs.getBoolean(PREF_STATUSBAR_DOUBLE_TAP, false)) {
                XposedHelpers.findAndHookMethod(CUSTOM_SDK_MONITOR,
                        classLoader,
                        "isStatusBarDoubleTapEnabled",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            }

            if (prefs.getBoolean(PREF_DISABLE_EYE_STRAIN_DIALOG, false)) {
                XposedHelpers.findAndHookMethod(TOGGLE_SLIDER_VIEW,
                        classLoader,
                        "updateEyeStrainDialog",
                        int.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                param.args[0] = 1;
                            }
                        });
                XposedHelpers.findAndHookConstructor(SETTINGS_HELPER,
                        classLoader,
                        Context.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Context mContext = (Context) param.args[0];
                                Settings.System.putInt(mContext.getContentResolver(), "shown_max_brightness_dialog", 1);
                            }
                        });
            }

            if (prefs.getBoolean(PREF_DISABLE_VOLUME_WARNING, false)) {
                XposedHelpers.findAndHookConstructor(VOLUME_DIALOG_CONTROLLER_IMPL,
                        classLoader,
                        Context.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Object mAudio = XposedHelpers.getObjectField(param.thisObject, "mAudio");
                                XposedHelpers.callMethod(mAudio, "disableSafeMediaVolume");
                            }
                        });
            }

            if (prefs.getBoolean(PREF_ENABLE_BIOMETRICS_UNLOCK, false)) {
                XposedHelpers.findAndHookMethod(KEYGUARD_STRONG_AUTH_TRACKER,
                        classLoader,
                        "hasUserAuthenticatedSinceBoot",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));

                XposedHelpers.findAndHookMethod(KEYGUARD_STRONG_AUTH_TRACKER,
                        classLoader,
                        "isUnlockingWithBiometricAllowed",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));

                XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor$20",
                        classLoader,
                        "onAuthenticationError",
                        int.class,
                        CharSequence.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                param.setResult(null);
                            }
                        });
            }

            if (prefs.getBoolean(PREF_SHOW_CLOCK_SECONDS, false) ||
                    !prefs.getString(PREF_CLOCK_DATE_PREFERENCE, "disabled").equals("disabled")) {
                qsClock = XposedHelpers.findClass(QS_CLOCK, classLoader);
                XposedHelpers.findAndHookMethod(qsClock,
                        "notifyTimeChanged",
                        String.class,
                        String.class,
                        boolean.class,
                        String.class,
                        String.class,
                        new XC_MethodHook() {
                            @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                String tag = (String) (XposedHelpers.callMethod(param.thisObject, "getTag"));
                                if (tag.equals("phone_status_bar_clock")) {
                                    mClock = (TextView) param.thisObject;
                                    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                                    boolean is24 = DateFormat.is24HourFormat(mClock.getContext());
                                    if (prefs.getBoolean(PREF_SHOW_CLOCK_SECONDS, false)) {
                                        if (mSecondsHandler == null) {
                                            updateSecondsHandler();
                                        }
                                        if (mSecondsFormat == null) {
                                            mSecondsFormat = new SimpleDateFormat(
                                                    DateFormat.getBestDateTimePattern(
                                                            Locale.getDefault(), is24 ? "Hms" : "hms"));
                                        }
                                        mClock.setText(mSecondsFormat.format(calendar.getTime()));
                                    }
                                    String amPm = calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault());
                                    int amPmIndex = mClock.getText().toString().indexOf(amPm);
                                    if (!is24 && amPmIndex == -1) {
                                        // insert AM/PM if missing
                                        if (Locale.getDefault().equals(Locale.TAIWAN) || Locale.getDefault().equals(Locale.CHINA)) {
                                            mClock.setText(amPm + " " + mClock.getText());
                                        } else {
                                            mClock.setText(mClock.getText() + " " + amPm);
                                        }
                                    }
                                    String showClockDate =
                                            prefs.getString(PREF_CLOCK_DATE_PREFERENCE, "disabled");
                                    if (!showClockDate.equals("disabled")) {
                                        CharSequence date;
                                        SimpleDateFormat df = (SimpleDateFormat) SimpleDateFormat
                                                .getDateInstance(SimpleDateFormat.SHORT);
                                        String pattern = showClockDate.equals("localized") ?
                                                df.toLocalizedPattern().replaceAll(".?[Yy].?", "") :
                                                showClockDate;
                                        date = new SimpleDateFormat(pattern, Locale.getDefault()).format(calendar.getTime());
                                        if (prefs.getBoolean(PREF_CLOCK_DATE_ON_RIGHT, false)) {
                                            mClock.setText(mClock.getText().toString() + " " + date);
                                        } else {
                                            mClock.setText(date + " " + mClock.getText().toString());
                                        }
                                    }
                                }
                            }
                        });
            }

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

            if (prefs.getBoolean(PREF_ENABLE_SAMSUNG_BLUR, true)) {
                Class<?> stateClass = XposedHelpers.findClass(STATE, classLoader);

                XposedHelpers.findAndHookMethod(STATUS_BAR_WINDOW_CONTROLLER,
                        classLoader,
                        "applyBouncer",
                        stateClass,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                XposedHelpers.callMethod(param.thisObject, "applyPanelBlur", 0.15f);
                            }
                        });
            }

            if (prefs.getBoolean(PREF_HIDE_CHARGING_NOTIFICATION, false)) {
                XposedHelpers.findAndHookMethod(POWER_NOTIFICATION_WARNINGS,
                        classLoader,
                        "showChargingNotification",
                        int.class,
                        XC_MethodReplacement.returnConstant(null));
            }

            if (prefs.getBoolean(PREF_DISABLE_VOLUME_CONTROL_SOUND, false)) {
                XposedHelpers.findAndHookMethod(SEC_VOLUME_DIALOG_IMPL,
                        classLoader,
                        "makeSound",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                param.setResult(null);
                            }
                        });
            }

            if (prefs.getBoolean(PREF_DISABLE_LOW_BATTERY_SOUND, false)) {
                XposedHelpers.findAndHookConstructor(NOTIFICATION_PLAYER,
                        classLoader,
                        String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                XposedHelpers.setObjectField(param.thisObject,
                                        "mLowBatteryUri",
                                        Uri.parse("file:///system/media/audio/ui/LowBattery.ogg.disabled"));
                            }
                        });
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void updateSecondsHandler() {
        if (mClock == null) return;

        if (mClock.getDisplay() != null) {
            updateClock = XposedHelpers.findMethodExact(qsClock, "updateClock");
            mSecondsHandler = new Handler();
            if (mClock.getDisplay().getState() == Display.STATE_ON) {
                mSecondsHandler.postAtTime(mSecondTick, SystemClock.uptimeMillis() / 1000 * 1000 + 1000);
            }
        } else if (mSecondsHandler != null) {
            mSecondsHandler.removeCallbacks(mSecondTick);
            mSecondsHandler = null;
            updateClock();
        }
    }

    private static final Runnable mSecondTick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            if (mSecondsHandler != null) {
                mSecondsHandler.postAtTime(this, SystemClock.uptimeMillis() / 1000 * 1000 + 1000);
            }
        }
    };

    private static void updateClock() {
        try {
            if (mClock != null) {
                updateClock.invoke(mClock);
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}
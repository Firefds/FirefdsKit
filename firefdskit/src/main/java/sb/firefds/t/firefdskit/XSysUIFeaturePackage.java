/*
 * Copyright (C) 2022 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.t.firefdskit;

import static sb.firefds.t.firefdskit.utils.Packages.SYSTEM_UI;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_CLOCK_DATE_ON_RIGHT;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_CLOCK_DATE_PREFERENCE;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_CLOCK_SIZE;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_DISABLE_EYE_STRAIN_DIALOG;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_DISABLE_LOW_BATTERY_SOUND;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_DISABLE_SYNC_DIALOG;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_DISABLE_VOLUME_CONTROL_SOUND;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_DISABLE_VOLUME_WARNING;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_ENABLE_BIOMETRICS_UNLOCK;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_HIDE_CHARGING_NOTIFICATION;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_MAX_SUPPORTED_USERS;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_SHOW_AM_PM;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_SHOW_CLOCK_SECONDS;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_STATUSBAR_DOUBLE_TAP;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_SUPPORTS_MULTIPLE_USERS;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import java.util.Optional;
import java.util.TimeZone;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSysUIFeaturePackage {

    private static final String KNOX_STATE_MONITOR_IMPL = SYSTEM_UI + ".knox.KnoxStateMonitorImpl";
    private static final String SHOW_USING_HIGH_BRIGHTNESS_DIALOG = SYSTEM_UI + ".settings.brightness" +
            ".BrightnessSliderController";
    private static final String VOLUME_DIALOG_CONTROLLER_IMPL = SYSTEM_UI + ".volume.VolumeDialogControllerImpl";
    private static final String KEYGUARD_STRONG_AUTH_TRACKER = "com.android.keyguard.KeyguardUpdateMonitor" +
            ".StrongAuthTracker";
    private static final String QS_CLOCK_INDICATOR_VIEW = SYSTEM_UI + ".statusbar.policy.QSClockIndicatorView";
    private static final String QS_CLOCK_BELL_SOUND = "com.android.systemui.statusbar.policy.QSClockBellSound";
    private static final String CHARGING_NOTIFICATION = SYSTEM_UI + ".power.notification.ChargingNotification";
    private static final String SETTINGS_HELPER = SYSTEM_UI + ".util.SettingsHelper";
    private static final String DUMP_MANAGER = SYSTEM_UI + ".dump.DumpManager";
    private static final String SOUND_POOL_WRAPPER = SYSTEM_UI + ".volume.util.SoundPoolWrapper";
    private static final String SOUND_PATH_FINDER = SYSTEM_UI + ".power.sound.SoundPathFinder";
    private static final String SYNC_TILE = SYSTEM_UI + ".qs.tiles.SyncTile";

    @SuppressLint("StaticFieldLeak")
    private static TextView mClock;
    private static SimpleDateFormat mSecondsFormat;
    private static Handler mSecondsHandler;
    private static Class<?> qsClock;
    private static Class<?> qsClockBellSoundClass;
    private static Object qsClockBellSound;
    private static Method updateClock;

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        try {
            if (prefs.getBoolean(PREF_STATUSBAR_DOUBLE_TAP, false)) {
                XposedHelpers.findAndHookMethod(KNOX_STATE_MONITOR_IMPL,
                        classLoader,
                        "isStatusBarDoubleTapEnabled",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            if (prefs.getBoolean(PREF_DISABLE_EYE_STRAIN_DIALOG, false)) {
                Class<?> dumpManager = XposedHelpers.findClass(DUMP_MANAGER, classLoader);
                XposedHelpers.findAndHookMethod(SHOW_USING_HIGH_BRIGHTNESS_DIALOG,
                        classLoader,
                        "showUsingHighBrightnessDialog",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                param.setResult(null);
                            }
                        });
                XposedHelpers.findAndHookConstructor(SETTINGS_HELPER,
                        classLoader,
                        Context.class,
                        dumpManager,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Context mContext = (Context) param.args[0];
                                Settings.System.putInt(mContext.getContentResolver(), "shown_max_brightness_dialog", 1);
                            }
                        });
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            if (prefs.getBoolean(PREF_DISABLE_VOLUME_WARNING, false)) {
                Class<?> volumeDialogControllerImpl = XposedHelpers.findClass(VOLUME_DIALOG_CONTROLLER_IMPL,
                        classLoader);
                XposedBridge.hookAllConstructors(volumeDialogControllerImpl,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Object mAudio = XposedHelpers.getObjectField(param.thisObject, "mAudio");
                                XposedHelpers.callMethod(mAudio, "disableSafeMediaVolume");
                            }
                        });
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            if (prefs.getBoolean(PREF_ENABLE_BIOMETRICS_UNLOCK, false)) {
                XposedHelpers.findAndHookMethod(KEYGUARD_STRONG_AUTH_TRACKER,
                        classLoader,
                        "hasUserAuthenticatedSinceBoot",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));

                XposedHelpers.findAndHookMethod(KEYGUARD_STRONG_AUTH_TRACKER,
                        classLoader,
                        "isUnlockingWithBiometricAllowed",
                        boolean.class,
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));

                XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor$12",
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
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            if (prefs.getBoolean(PREF_SHOW_CLOCK_SECONDS, false) ||
                    !prefs.getString(PREF_CLOCK_DATE_PREFERENCE, "disabled").equals("disabled") ||
                    !prefs.getString(PREF_CLOCK_SIZE, "Small").equals("Small")) {
                qsClock = XposedHelpers.findClass(QS_CLOCK_INDICATOR_VIEW, classLoader);
                qsClockBellSoundClass = XposedHelpers.findClass(QS_CLOCK_BELL_SOUND, classLoader);
                XposedHelpers.findAndHookMethod(qsClock,
                        "notifyTimeChanged",
                        qsClockBellSoundClass,
                        new XC_MethodHook() {
                            @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                qsClockBellSound = param.args[0];
                                mClock = (TextView) param.thisObject;
                                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                                boolean is24 = DateFormat.is24HourFormat(mClock.getContext());

                                int textSize = XSysUINotificationPanelPackage
                                        .getClockSizeValue(prefs.getString(PREF_CLOCK_SIZE, "Small"));
                                mClock.setTextSize(textSize);

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
                                if (!is24) {
                                    String amPm = calendar.getDisplayName(Calendar.AM_PM,
                                            Calendar.SHORT,
                                            Locale.getDefault());
                                    int amPmIndex = mClock.getText().toString().indexOf(amPm);
                                    if (prefs.getBoolean(PREF_SHOW_AM_PM, false) && amPmIndex == -1) {
                                        if (Locale.getDefault().equals(Locale.TAIWAN) || Locale.getDefault().equals(Locale.CHINA)) {
                                            mClock.setText(amPm + " " + mClock.getText());
                                        } else {
                                            mClock.setText(mClock.getText() + " " + amPm);
                                        }
                                    }
                                    if (!prefs.getBoolean(PREF_SHOW_AM_PM, false) && amPmIndex != -1) {
                                        mClock.setText(mClock.getText()
                                                .toString()
                                                .substring(0, amPmIndex - 1));
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
                        });
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
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

        try {
            if (prefs.getBoolean(PREF_HIDE_CHARGING_NOTIFICATION, false)) {
                XposedHelpers.findAndHookMethod(CHARGING_NOTIFICATION,
                        classLoader,
                        "showNotification",
                        XC_MethodReplacement.returnConstant(null));
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            if (prefs.getBoolean(PREF_DISABLE_VOLUME_CONTROL_SOUND, false)) {
                try {
                    XposedHelpers.findAndHookMethod(SOUND_POOL_WRAPPER,
                            classLoader,
                            "makeSound",
                            new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    param.setResult(null);
                                }
                            });
                } catch (Exception e) {
                    XposedBridge.log(e);
                }
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            if (prefs.getBoolean(PREF_DISABLE_LOW_BATTERY_SOUND, false)) {
                XposedHelpers.findAndHookMethod(SOUND_PATH_FINDER,
                        classLoader,
                        "getBatteryCautionPath",
                        XC_MethodReplacement.returnConstant("system/media/audio/ui/TW_Battery_caution-disabled.ogg")
                );
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        try {
            XposedHelpers.findAndHookMethod(SYNC_TILE,
                    classLoader,
                    "showConfirmPopup",
                    boolean.class,
                    new XC_MethodHook() {
                        @SuppressLint("MissingPermission")
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if (prefs.getBoolean(PREF_DISABLE_SYNC_DIALOG, false)) {
                                ContentResolver.setMasterSyncAutomatically((Boolean) param.args[0]);
                                param.setResult(null);
                            }
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static void updateSecondsHandler() {
        if (mClock == null) return;

        if (Optional.ofNullable(mClock.getDisplay()).isPresent()) {
            updateClock = XposedHelpers.findMethodExact(qsClock, "notifyTimeChanged", qsClockBellSoundClass);
            mSecondsHandler = new Handler(Looper.getMainLooper());
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
                updateClock.invoke(mClock, qsClockBellSound);
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}
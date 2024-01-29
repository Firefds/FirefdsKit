/*
 * Copyright (C) 2023 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.u.firefdskit;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetBooleanPref;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetIntPref;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetStringPref;
import static sb.firefds.u.firefdskit.utils.Packages.SYSTEM_UI;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_CLOCK_DATE_ON_RIGHT;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_CLOCK_DATE_PREFERENCE;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_CLOCK_SIZE;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_EYE_STRAIN_DIALOG;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_LOW_BATTERY_SOUND;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_SYNC_DIALOG;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_VOLUME_CONTROL_SOUND;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_VOLUME_WARNING;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_QUICK_REPLY_ON_SECURE_LOCKSCREEN;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_HIDE_CHARGING_NOTIFICATION;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_MAX_SUPPORTED_USERS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SHOW_AM_PM;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SHOW_CLOCK_SECONDS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_STATUSBAR_DOUBLE_TAP;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SUPPORTS_MULTIPLE_USERS;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import de.robv.android.xposed.XC_MethodHook;

public class XSysUIFeaturePackage {

    private static final String SHOW_USING_HIGH_BRIGHTNESS_DIALOG = SYSTEM_UI +
                                                                    ".settings.brightness" +
                                                                    ".BrightnessSliderController";
    private static final String VOLUME_DIALOG_CONTROLLER_IMPL = SYSTEM_UI + ".volume.VolumeDialogControllerImpl";
    private static final String QS_CLOCK_INDICATOR_VIEW = SYSTEM_UI + ".statusbar.policy.QSClockIndicatorView";
    private static final String QS_CLOCK_BELL_SOUND = "com.android.systemui.statusbar.policy.QSClockBellSound";
    private static final String CHARGING_NOTIFICATION = SYSTEM_UI + ".power.notification.ChargingNotification";
    private static final String SETTINGS_HELPER = SYSTEM_UI + ".util.SettingsHelper";
    private static final String DUMP_MANAGER = SYSTEM_UI + ".dump.DumpManager";
    private static final String SOUND_POOL_WRAPPER$MAKE_SOUND$1 = SYSTEM_UI +
                                                                  ".volume.util.SoundPoolWrapper$makeSound$1";
    private static final String SOUND_PATH_FINDER = SYSTEM_UI + ".power.sound.SoundPathFinder";
    private static final String SYNC_TILE = SYSTEM_UI + ".qs.tiles.SyncTile.SyncDetailAdapter";
    private static final String NOTIFICATION_LOCKSCREEN_USER_MANAGER_IMPL = SYSTEM_UI +
                                                                            ".statusbar" +
                                                                            ".NotificationLockscreenUserManagerImpl";
    private static final String NOTIFICATION_PANEL_VIEW_CONTROLLER = SYSTEM_UI +
                                                                     ".shade.NotificationPanelViewController";
    private static final String NOTIFICATION_PANEL_VIEW_CONTROLLER$TOUCH_HANDLER = NOTIFICATION_PANEL_VIEW_CONTROLLER +
                                                                                   "$TouchHandler";
    private static final String SYSTEM_BAR_UTILS = "com.android.internal.policy.SystemBarUtils";

    @SuppressLint("StaticFieldLeak")
    private static TextView mClock;
    private static SimpleDateFormat mSecondsFormat;
    private static Handler mSecondsHandler;
    private static Class<?> qsClock;
    private static Class<?> qsClockBellSoundClass;
    private static Object qsClockBellSound;
    private static Method updateClock;

    private static Object mNotificationPanelViewController;
    private static int mQuickQsOffsetHeight;
    private static PowerManager mPowerManager;
    private static GestureDetector mDoubleTapGesture;

    public static void doHook(ClassLoader classLoader) {

        try {
            Class<?> notificationPanelViewControllerClass = findClass(NOTIFICATION_PANEL_VIEW_CONTROLLER, classLoader);
            hookAllConstructors(notificationPanelViewControllerClass, new XC_MethodHook() {
                final Class<?> clazz = findClass(SYSTEM_BAR_UTILS, classLoader);

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (clazz != null) {
                        mNotificationPanelViewController = param.thisObject;
                        View view = (View) getObjectField(param.thisObject, "mView");
                        mQuickQsOffsetHeight = (int) callStaticMethod(clazz,
                                                                      "getQuickQsOffsetHeight",
                                                                      view.getContext());

                        mPowerManager = (PowerManager) view.getContext().getSystemService(Context.POWER_SERVICE);
                        mDoubleTapGesture = new GestureDetector(view.getContext(),
                                                                new GestureDetector.SimpleOnGestureListener() {
                                                                    @Override
                                                                    public boolean onDoubleTap(
                                                                            @NonNull MotionEvent ev) {
                                                                        callMethod(mPowerManager,
                                                                                   "goToSleep",
                                                                                   ev.getEventTime());
                                                                        return true;
                                                                    }
                                                                });
                    } else {
                        log("FFK: Failed to get QuickQsOffsetHeight. Double tap to sleep is disabled.");
                    }
                }
            });

            findAndHookMethod(NOTIFICATION_PANEL_VIEW_CONTROLLER$TOUCH_HANDLER,
                              classLoader,
                              "onTouchEvent",
                              MotionEvent.class,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (mQuickQsOffsetHeight > 0) {
                                          MotionEvent ev = (MotionEvent) param.args[0];
                                          boolean isPulsing = getBooleanField(mNotificationPanelViewController,
                                                                              "mPulsing");
                                          boolean isDozing = getBooleanField(mNotificationPanelViewController,
                                                                             "mDozing");

                                          if (reloadAndGetBooleanPref(PREF_STATUSBAR_DOUBLE_TAP, false) &&
                                              ev.getY() < mQuickQsOffsetHeight &&
                                              !isPulsing &&
                                              !isDozing) mDoubleTapGesture.onTouchEvent(ev);
                                      }
                                  }
                              });
            findAndHookMethod("com.android.systemui.shade.PulsingGestureListener",
                              classLoader,
                              "onDoubleTapEvent",
                              MotionEvent.class,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (mQuickQsOffsetHeight > 0) {
                                          MotionEvent ev = (MotionEvent) param.args[0];
                                          Object statusBarStateController = getObjectField(param.thisObject,
                                                                                           "statusBarStateController");
                                          Object falsingManager = getObjectField(param.thisObject, "falsingManager");
                                          boolean isDozing = (boolean) callMethod(statusBarStateController, "isDozing");
                                          boolean isFalseDoubleTap = (boolean) callMethod(falsingManager,
                                                                                          "isFalseDoubleTap");

                                          if (reloadAndGetBooleanPref(PREF_STATUSBAR_DOUBLE_TAP, false) &&
                                              ev.getActionMasked() == MotionEvent.ACTION_UP &&
                                              ev.getY() < mQuickQsOffsetHeight &&
                                              !isDozing &&
                                              !isFalseDoubleTap) {
                                              callMethod(mPowerManager, "goToSleep", ev.getEventTime());
                                              param.setResult(true);
                                          }
                                      }
                                  }
                              });
        } catch (Throwable e) {
            log(e);
        }

        try {
            Class<?> dumpManager = findClass(DUMP_MANAGER, classLoader);
            findAndHookMethod(SHOW_USING_HIGH_BRIGHTNESS_DIALOG,
                              classLoader,
                              "updateUsingHighBrightnessDialog",
                              boolean.class,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (reloadAndGetBooleanPref(PREF_DISABLE_EYE_STRAIN_DIALOG, false)) {
                                          param.args[0] = false;
                                      }
                                  }
                              });
            findAndHookConstructor(SETTINGS_HELPER, classLoader, Context.class, dumpManager, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DISABLE_EYE_STRAIN_DIALOG, false)) {
                        Context mContext = (Context) param.args[0];
                        Settings.System.putInt(mContext.getContentResolver(), "shown_max_brightness_dialog", 1);
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }

        try {
            Class<?> volumeDialogControllerImpl = findClass(VOLUME_DIALOG_CONTROLLER_IMPL, classLoader);
            hookAllConstructors(volumeDialogControllerImpl, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DISABLE_VOLUME_WARNING, false)) {
                        Object mAudio = getObjectField(param.thisObject, "mAudio");
                        callMethod(mAudio, "disableSafeMediaVolume");
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }

        try {
            qsClock = findClass(QS_CLOCK_INDICATOR_VIEW, classLoader);
            qsClockBellSoundClass = findClass(QS_CLOCK_BELL_SOUND, classLoader);
            findAndHookMethod(qsClock, "notifyTimeChanged", qsClockBellSoundClass, new XC_MethodHook() {
                @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    qsClockBellSound = param.args[0];
                    mClock = (TextView) param.thisObject;
                    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                    boolean is24 = DateFormat.is24HourFormat(mClock.getContext());

                    int textSize = XSysUINotificationPanelPackage.getClockSizeValue(reloadAndGetStringPref(
                            PREF_CLOCK_SIZE,
                            "Small"));
                    mClock.setTextSize(textSize);

                    if (reloadAndGetBooleanPref(PREF_SHOW_CLOCK_SECONDS, false)) {
                        if (mSecondsHandler == null) {
                            updateSecondsHandler();
                        }
                        if (mSecondsFormat == null) {
                            mSecondsFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(),
                                                                                                    is24
                                                                                                    ? "Hms"
                                                                                                    : "hms"));
                        }
                        mClock.setText(mSecondsFormat.format(calendar.getTime()));
                    }
                    if (!is24) {
                        String amPm = calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault());
                        int amPmIndex = mClock.getText().toString().indexOf(amPm);
                        boolean showAmPm = reloadAndGetBooleanPref(PREF_SHOW_AM_PM, false);
                        if (showAmPm && amPmIndex == -1) {
                            if (Locale.getDefault().equals(Locale.TAIWAN) || Locale.getDefault().equals(Locale.CHINA)) {
                                mClock.setText(amPm + " " + mClock.getText());
                            } else {
                                mClock.setText(mClock.getText() + " " + amPm);
                            }
                        }
                        if (!showAmPm && amPmIndex != -1) {
                            mClock.setText(mClock.getText().toString().substring(0, amPmIndex - 1));
                        }
                    }
                    String showClockDate = reloadAndGetStringPref(PREF_CLOCK_DATE_PREFERENCE, "disabled");
                    if (!showClockDate.equals("disabled")) {
                        CharSequence date;
                        SimpleDateFormat df = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
                        String pattern = showClockDate.equals("localized")
                                         ? df.toLocalizedPattern()
                                             .replaceAll(".?[Yy].?", "")
                                         : showClockDate;
                        date = new SimpleDateFormat(pattern, Locale.getDefault()).format(calendar.getTime());
                        if (reloadAndGetBooleanPref(PREF_CLOCK_DATE_ON_RIGHT, false)) {
                            mClock.setText(mClock.getText().toString() + " " + date);
                        } else {
                            mClock.setText(date + " " + mClock.getText().toString());
                        }
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }

        try {
            findAndHookMethod(UserManager.class, "supportsMultipleUsers", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.setResult(reloadAndGetBooleanPref(PREF_SUPPORTS_MULTIPLE_USERS, false));
                }
            });

            findAndHookMethod(UserManager.class, "getMaxSupportedUsers", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_SUPPORTS_MULTIPLE_USERS, false)) {
                        param.setResult(reloadAndGetIntPref(PREF_MAX_SUPPORTED_USERS, 3));
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }

        try {
            findAndHookMethod(CHARGING_NOTIFICATION, classLoader, "showNotification", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_HIDE_CHARGING_NOTIFICATION, false)) {
                        param.setResult(null);
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }

        try {
            try {
                findAndHookMethod(SOUND_POOL_WRAPPER$MAKE_SOUND$1, classLoader, "run", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (reloadAndGetBooleanPref(PREF_DISABLE_VOLUME_CONTROL_SOUND, false)) {
                            param.setResult(null);
                        }
                    }
                });
            } catch (Exception e) {
                log(e);
            }
        } catch (Throwable e) {
            log(e);
        }

        try {
            findAndHookMethod(SOUND_PATH_FINDER,
                              classLoader,
                              "getSystemSoundPath",
                              String.class,
                              String.class,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (reloadAndGetBooleanPref(PREF_DISABLE_LOW_BATTERY_SOUND, false)) {
                                          param.setResult("system/media/audio/ui/TW_Battery_caution-disabled.ogg");
                                      }
                                  }
                              });
        } catch (Throwable e) {
            log(e);
        }

        try {
            Class<?> notificationLockscreenUserManagerImplClass =

                    findClass(NOTIFICATION_LOCKSCREEN_USER_MANAGER_IMPL, classLoader);
            Method setLockscreenAllowRemoteInput = findMethodBestMatch(notificationLockscreenUserManagerImplClass,
                                                                       "updateLockscreenNotificationSetting");
            hookMethod(setLockscreenAllowRemoteInput, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_ENABLE_QUICK_REPLY_ON_SECURE_LOCKSCREEN, false)) {
                        setObjectField(param.thisObject, "mAllowLockscreenRemoteInput", true);
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }

        try {
            findAndHookMethod(SYNC_TILE, classLoader, "setToggleState", boolean.class, new XC_MethodHook() {
                @SuppressLint("MissingPermission")
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_DISABLE_SYNC_DIALOG, false)) {
                        ContentResolver.setMasterSyncAutomatically((Boolean) param.args[0]);
                        param.setResult(null);
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }
    }

    private static void updateSecondsHandler() {
        if (mClock == null) return;

        if (Optional.ofNullable(mClock.getDisplay()).isPresent()) {
            updateClock = findMethodExact(qsClock, "notifyTimeChanged", qsClockBellSoundClass);
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
            log(e);
        }
    }
}
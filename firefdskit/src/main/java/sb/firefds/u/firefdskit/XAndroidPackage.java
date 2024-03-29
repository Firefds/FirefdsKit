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

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetBooleanPref;
import static sb.firefds.u.firefdskit.Xposed.reloadAndGetIntPref;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DEFAULT_REBOOT_BEHAVIOR;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_DISABLE_SIGNATURE_CHECK;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_ENABLE_DUAL_SIM_SD_CARD;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_HIDE_USB_NOTIFICATION;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_HIDE_VOLTE_ICON;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_MAX_SUPPORTED_USERS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SKIP_TRACKS_WITH_VOLUME_KEYS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SUPPORTS_MULTIPLE_USERS;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.Signature;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserManager;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import java.util.List;
import java.util.Optional;

import de.robv.android.xposed.XC_MethodHook;

public class XAndroidPackage {

    private static final String PACKAGE_MANAGER_SERVICE_UTILS = "com.android.server.pm.PackageManagerServiceUtils";
    private static final String PACKAGE_MANAGER_SERVICE = "com.android.server.pm.PackageManagerService";
    private static final String PACKAGE_MANAGER_SERVICE_INJECTOR = "com.android.server.pm" +
                                                                   ".PackageManagerServiceInjector";
    private static final String PACKAGE_MANAGER_SERVICE_TEST_PARAMS = "com.android.server.pm" +
                                                                      ".PackageManagerServiceTestParams";
    private static final String PACKAGE_SIGNATURES = "com.android.server.pm.PackageSignatures";
    private static final String SIGNING_DETAILS = "android.content.pm.SigningDetails";
    private static final String STATUS_BAR_MANAGER_SERVICE = "com.android.server.statusbar.StatusBarManagerService";
    private static final String USB_HANDLER = "com.android.server.usb.UsbDeviceManager.UsbHandler";
    private static final String SHUTDOWN_THREAD = "com.android.server.power.ShutdownThread";
    private static final String DEVICE_POLICY_MANAGER_SERVICE = "com.android.server.devicepolicy" +
                                                                ".DevicePolicyManagerService";
    public static final String PHONE_WINDOW_MANAGER = "com.android.server.policy.PhoneWindowManager";
    public static final String WINDOW_MANAGER_FUNCS = "com.android.server.policy.WindowManagerPolicy" +
                                                      ".WindowManagerFuncs";
    @SuppressLint("StaticFieldLeak")
    private static Context mPackageManagerServiceContext;
    private static boolean isFB;
    private static Object mPhoneWindowManager;
    @SuppressLint("StaticFieldLeak")
    private static Context mPhoneWindowManagerContext;
    private static boolean mVolumeLongPress;
    private static Handler mHandler;
    private static AudioManager mAudioManager;
    private static PowerManager mPowerManager;

    public static void doHook(ClassLoader classLoader) {

        try {

            findAndHookMethod(DEVICE_POLICY_MANAGER_SERVICE,
                              classLoader,
                              "semGetAllowStorageCard",
                              ComponentName.class,
                              int.class,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (reloadAndGetBooleanPref(PREF_ENABLE_DUAL_SIM_SD_CARD, false)) {
                                          param.setResult(Boolean.TRUE);
                                      }
                                  }
                              });

            Class<?> shutdownThreadClass = findClass(SHUTDOWN_THREAD, classLoader);
            findAndHookMethod(shutdownThreadClass,
                              "rebootOrShutdown",
                              Context.class,
                              boolean.class,
                              String.class,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (reloadAndGetBooleanPref(PREF_DEFAULT_REBOOT_BEHAVIOR, false)) {
                                          boolean reboot = (boolean) param.args[1];
                                          String reason = (String) param.args[2];
                                          if (reboot && reason.equals("userrequested")) {
                                              param.args[2] = "recovery";
                                          }
                                      }
                                  }
                              });

            if (mPackageManagerServiceContext == null) {
                findAndHookConstructor(PACKAGE_MANAGER_SERVICE,
                                       classLoader,
                                       PACKAGE_MANAGER_SERVICE_INJECTOR,
                                       PACKAGE_MANAGER_SERVICE_TEST_PARAMS,
                                       new XC_MethodHook() {
                                           @Override
                                           protected void afterHookedMethod(MethodHookParam param) {
                                               mPackageManagerServiceContext = (Context) getObjectField(param.thisObject,
                                                                                                        "mContext");
                                           }
                                       });
            }

            findAndHookMethod(PACKAGE_MANAGER_SERVICE_UTILS,
                              classLoader,
                              "compareSignatures",
                              Signature[].class,
                              Signature[].class,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (reloadAndGetBooleanPref(PREF_DISABLE_SIGNATURE_CHECK, false)) {
                                          new Handler(Looper.getMainLooper()).post(new DLX());
                                          if (!isFB) {
                                              param.setResult(0);
                                          }
                                      }
                                  }
                              });

            findAndHookMethod(PACKAGE_MANAGER_SERVICE_UTILS,
                              classLoader,
                              "matchSignaturesCompat",
                              String.class,
                              PACKAGE_SIGNATURES,
                              SIGNING_DETAILS,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (reloadAndGetBooleanPref(PREF_DISABLE_SIGNATURE_CHECK, false)) {
                                          new Handler(Looper.getMainLooper()).post(new DLX());
                                          if (!isFB) {
                                              param.setResult(false);
                                          }
                                      }
                                  }
                              });

            findAndHookMethod(PACKAGE_MANAGER_SERVICE_UTILS,
                              classLoader,
                              "matchSignaturesRecover",
                              String.class,
                              SIGNING_DETAILS,
                              SIGNING_DETAILS,
                              int.class,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (reloadAndGetBooleanPref(PREF_DISABLE_SIGNATURE_CHECK, false)) {
                                          new Handler(Looper.getMainLooper()).post(new DLX());
                                          if (!isFB) {
                                              param.setResult(false);
                                          }
                                      }
                                  }
                              });

            findAndHookMethod(UserManager.class, "supportsMultipleUsers", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_SUPPORTS_MULTIPLE_USERS, false)) {
                        param.setResult(true);
                    }
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

            findAndHookMethod(STATUS_BAR_MANAGER_SERVICE,
                              classLoader,
                              "setIconVisibility",
                              String.class,
                              boolean.class,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (reloadAndGetBooleanPref(PREF_HIDE_VOLTE_ICON, false)) {
                                          if (param.args[0].equals("ims_volte") || param.args[0].equals("ims_volte2")) {
                                              param.args[1] = false;
                                          }
                                      }
                                  }
                              });

            findAndHookMethod(USB_HANDLER, classLoader, "updateUsbNotification", boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (reloadAndGetBooleanPref(PREF_HIDE_USB_NOTIFICATION, false)) {
                        param.setResult(null);
                    }
                }
            });

            findAndHookMethod(PHONE_WINDOW_MANAGER,
                              classLoader,
                              "init",
                              Context.class,
                              WINDOW_MANAGER_FUNCS,
                              new XC_MethodHook() {
                                  @Override
                                  protected void afterHookedMethod(MethodHookParam param) {
                                      if (reloadAndGetBooleanPref(PREF_SKIP_TRACKS_WITH_VOLUME_KEYS, false)) {
                                          if (mPhoneWindowManager == null) {
                                              mPhoneWindowManager = param.thisObject;
                                          }

                                          mPhoneWindowManagerContext = (Context) getObjectField(mPhoneWindowManager,
                                                                                                "mContext");

                                          Runnable mVolumeUpLongPress = () -> {
                                              mVolumeLongPress = true;
                                              Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                                              KeyEvent keyEvent = new KeyEvent(SystemClock.uptimeMillis(),
                                                                               SystemClock.uptimeMillis(),
                                                                               KeyEvent.ACTION_DOWN,
                                                                               KeyEvent.KEYCODE_MEDIA_NEXT,
                                                                               0);
                                              keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                                              mAudioManager.dispatchMediaKeyEvent(keyEvent);

                                              keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
                                              keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                                              mAudioManager.dispatchMediaKeyEvent(keyEvent);
                                              callMethod(param.thisObject,
                                                         "performHapticFeedback",
                                                         new Class<?>[]{int.class, boolean.class, String.class},
                                                         HapticFeedbackConstants.LONG_PRESS,
                                                         false,
                                                         null);
                                          };

                                          Runnable mVolumeDownLongPress = () -> {
                                              mVolumeLongPress = true;
                                              Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                                              KeyEvent keyEvent = new KeyEvent(SystemClock.uptimeMillis(),
                                                                               SystemClock.uptimeMillis(),
                                                                               KeyEvent.ACTION_DOWN,
                                                                               KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                                                                               0);
                                              keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                                              mAudioManager.dispatchMediaKeyEvent(keyEvent);

                                              keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
                                              keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                                              mAudioManager.dispatchMediaKeyEvent(keyEvent);
                                              callMethod(param.thisObject,
                                                         "performHapticFeedback",
                                                         new Class<?>[]{int.class, boolean.class, String.class},
                                                         HapticFeedbackConstants.LONG_PRESS,
                                                         false,
                                                         null);
                                          };

                                          setAdditionalInstanceField(param.thisObject,
                                                                     "mVolumeUpLongPress",
                                                                     mVolumeUpLongPress);
                                          setAdditionalInstanceField(param.thisObject,
                                                                     "mVolumeDownLongPress",
                                                                     mVolumeDownLongPress);
                                      }
                                  }
                              });

            findAndHookMethod(PHONE_WINDOW_MANAGER,
                              classLoader,
                              "interceptKeyBeforeQueueing",
                              KeyEvent.class,
                              int.class,
                              new XC_MethodHook() {
                                  @Override
                                  protected void beforeHookedMethod(MethodHookParam param) {
                                      if (reloadAndGetBooleanPref(PREF_SKIP_TRACKS_WITH_VOLUME_KEYS, false)) {
                                          KeyEvent event = (KeyEvent) param.args[0];

                                          int keyCode = event.getKeyCode();
                                          boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
                                          boolean isFromSystem = (event.getFlags() & KeyEvent.FLAG_FROM_SYSTEM) != 0;
                                          mHandler = (Handler) getObjectField(param.thisObject, "mHandler");

                                          if (mPowerManager == null) {
                                              mPowerManager = (PowerManager) mPhoneWindowManagerContext.getSystemService(
                                                      Context.POWER_SERVICE);
                                          }

                                          Runnable mVolumeUpLongPress = (Runnable) getAdditionalInstanceField(param.thisObject,
                                                                                                              "mVolumeUpLongPress");
                                          Runnable mVolumeDownLongPress = (Runnable) getAdditionalInstanceField(param.thisObject,
                                                                                                                "mVolumeDownLongPress");

                                          if (mAudioManager == null)
                                              mAudioManager = (AudioManager) mPhoneWindowManagerContext.getSystemService(
                                                      Context.AUDIO_SERVICE);

                                          if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                                               keyCode == KeyEvent.KEYCODE_VOLUME_UP) &&
                                              isFromSystem &&
                                              !mPowerManager.isInteractive() &&
                                              (mAudioManager.isMusicActive() ||
                                               (boolean) callMethod(mAudioManager, "isMusicActiveRemotely"))) {
                                              if (down) {
                                                  if (event.getRepeatCount() == 0) {
                                                      mVolumeLongPress = false;
                                                      mHandler.postDelayed(keyCode == KeyEvent.KEYCODE_VOLUME_UP
                                                                           ? mVolumeUpLongPress
                                                                           : mVolumeDownLongPress,
                                                                           ViewConfiguration.getLongPressTimeout());
                                                  }
                                              } else {
                                                  mHandler.removeCallbacks(mVolumeUpLongPress);
                                                  mHandler.removeCallbacks(mVolumeDownLongPress);

                                                  if (!mVolumeLongPress) {
                                                      mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                                                                       keyCode ==
                                                                                       KeyEvent.KEYCODE_VOLUME_UP
                                                                                       ? AudioManager.ADJUST_RAISE
                                                                                       : AudioManager.ADJUST_LOWER,
                                                                                       0);
                                                  }
                                              }
                                              param.setResult(0);
                                          }
                                      }
                                  }
                              });

        } catch (Exception e) {
            log(e);
        }
    }

    private static class DLX implements Runnable {
        public void run() {
            try {
                ActivityManager activityManager;
                if (mPackageManagerServiceContext != null) {
                    activityManager = (ActivityManager) mPackageManagerServiceContext.getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.AppTask> runningTasks = activityManager.getAppTasks();

                    runningTasks.forEach(appTask -> Optional.ofNullable(appTask.getTaskInfo().topActivity)
                                                            .ifPresent(componentName -> isFB = componentName.getPackageName()
                                                                                                            .equals("com.facebook.katana")));
                }
            } catch (NullPointerException e) {
                log(e);
            }
        }
    }
}

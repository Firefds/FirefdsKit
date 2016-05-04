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
package sb.firefds.xtouchwizS5;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XCallback;

public class XVolumeKeysSkipTrack {
	private static final String PHONE_WINDOW_MANAGER = "com.android.server.policy.PhoneWindowManager";
	private static boolean mIsLongPress = false;
	private static AudioManager manager = null;
	private static Context mContext = null;

	private static AudioManager getManager(Context context) {
		if (manager == null) {
			manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		}

		return manager;
	}

	public static void init(ClassLoader classLoader) {
		try {

			Class<?> classPhoneWindowManager = findClass(PHONE_WINDOW_MANAGER, classLoader);
			XposedBridge.hookAllConstructors(classPhoneWindowManager, handleConstructPhoneWindowManager);

			findAndHookMethod(classPhoneWindowManager, "interceptKeyBeforeQueueing", KeyEvent.class, int.class,
					handleInterceptKeyBeforeQueueing);

		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

	private static XC_MethodHook handleInterceptKeyBeforeQueueing = new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			mContext = (Context) getObjectField(param.thisObject, "mContext");
			final boolean isScreenOn = (Boolean) XposedHelpers.callMethod(param.thisObject, "isScreenOn");
			if (!isScreenOn) {
				final KeyEvent event = (KeyEvent) param.args[0];
				final int keyCode = event.getKeyCode();

				if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
					if (getManager(mContext).isMusicActive()) {
						if (event.getAction() == KeyEvent.ACTION_DOWN) {
							mIsLongPress = false;
							handleVolumeLongPress(param.thisObject, keyCode);
							param.setResult(0);
							return;
						} else {
							handleVolumeLongPressAbort(param.thisObject);
							if (mIsLongPress) {
								param.setResult(0);
								return;
							}

							// send an additional "key down" because the first
							// one
							// was eaten
							// the "key up" is what we are just processing
							Object[] newArgs = new Object[2];
							newArgs[0] = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
							newArgs[1] = param.args[1];
							XposedBridge.invokeOriginalMethod(param.method, param.thisObject, newArgs);
						}
					}
				}
			}
		}
	};

	private static XC_MethodHook handleConstructPhoneWindowManager = new XC_MethodHook() {
		@Override
		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
			/**
			 * When a volumeup-key longpress expires, skip songs based on key
			 * press
			 */
			Runnable mVolumeUpLongPress = new Runnable() {
				@Override
				public void run() {
					// set the long press flag to true
					mIsLongPress = true;

					// Shamelessly copied from Kmobs LockScreen controls, works
					// for Pandora, etc...
					sendMediaButtonEvent(param.thisObject, KeyEvent.KEYCODE_MEDIA_NEXT);
				};
			};

			/**
			 * When a volumedown-key longpress expires, skip songs based on key
			 * press
			 */
			Runnable mVolumeDownLongPress = new Runnable() {
				@Override
				public void run() {
					// set the long press flag to true
					mIsLongPress = true;

					// Shamelessly copied from Kmobs LockScreen controls, works
					// for Pandora, etc...
					sendMediaButtonEvent(param.thisObject, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
				};
			};

			XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVolumeUpLongPress", mVolumeUpLongPress);
			XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVolumeDownLongPress", mVolumeDownLongPress);
		}
	};

	private static void sendMediaButtonEvent(Object phoneWindowManager, int code) {
		long eventtime = SystemClock.uptimeMillis();
		Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent keyEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, code, 0);
		keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
		mContext.sendOrderedBroadcast(keyIntent, null);
		keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
		keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
		mContext.sendOrderedBroadcast(keyIntent, null);
	}

	private static void handleVolumeLongPress(Object phoneWindowManager, int keycode) {
		Handler mHandler = (Handler) getObjectField(phoneWindowManager, "mHandler");
		Runnable mVolumeUpLongPress = (Runnable) getAdditionalInstanceField(phoneWindowManager, "mVolumeUpLongPress");
		Runnable mVolumeDownLongPress = (Runnable) getAdditionalInstanceField(phoneWindowManager,
				"mVolumeDownLongPress");

		mHandler.postDelayed(keycode == KeyEvent.KEYCODE_VOLUME_UP ? mVolumeUpLongPress : mVolumeDownLongPress,
				ViewConfiguration.getLongPressTimeout());
	}

	private static void handleVolumeLongPressAbort(Object phoneWindowManager) {
		Handler mHandler = (Handler) getObjectField(phoneWindowManager, "mHandler");
		Runnable mVolumeUpLongPress = (Runnable) getAdditionalInstanceField(phoneWindowManager, "mVolumeUpLongPress");
		Runnable mVolumeDownLongPress = (Runnable) getAdditionalInstanceField(phoneWindowManager,
				"mVolumeDownLongPress");

		mHandler.removeCallbacks(mVolumeUpLongPress);
		mHandler.removeCallbacks(mVolumeDownLongPress);
	}
}
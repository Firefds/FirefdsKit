/*
 * Copyright (C) 2011 The Android Open Source Project
 * Modifications Copyright (C) The OmniROM Project
 * Modifications Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Modifications Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
 *
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
 *
 * Per article 5 of the Apache 2.0 License, some modifications to this code
 * were made by the OmniROM Project.
 *
 * Modifications Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sb.firefds.firefdskit.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.Action.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import sb.firefds.firefdskit.BuildConfig;
import sb.firefds.firefdskit.MainApplication;
import sb.firefds.firefdskit.R;

public class ScreenRecordingService extends Service implements MediaScannerConnectionClient {

	private static final boolean debug = BuildConfig.DEBUG;
	private static final int SCREENRECORD_NOTIFICATION_ID = 3;
	private static final int MSG_TASK_ENDED = 1;
	private static final int MSG_TASK_ERROR = 2;

	public static final String ACTION_SCREEN_RECORDING_START = "firefdskit.intent.action.SCREEN_RECORDING_START";
	public static final String ACTION_SCREEN_RECORDING_STOP = "firefdskit.intent.action.SCREEN_RECORDING_STOP";
	public static final String ACTION_SCREEN_RECORDING_STATUS_CHANGED = "firefdskit.intent.action.SCREEN_RECORDING_STATUS_CHANGED";
	public static final String EXTRA_RECORDING_STATUS = "recordingStatus";
	public static final String EXTRA_STATUS_MESSAGE = "statusMessage";

	public static final int STATUS_IDLE = 0;
	public static final int STATUS_RECORDING = 1;
	public static final int STATUS_PROCESSING = 2;
	public static final int STATUS_ERROR = -1;

	private static Handler mHandler;
	private Notification mRecordingNotif;
	private int mRecordingStatus;
	private MediaScannerConnection conn;
	private String scanPath;
	private static final String TMP_PATH = Environment.getExternalStorageDirectory() + "/__tmp_screenrecord.mp4";

	private CaptureThread mCaptureThread;

	private void log(String msg) {
		if (debug) {
			Log.i(MainApplication.getAppContext().getPackageName(), msg);
		}
	}

	private class CaptureThread extends Thread {
		public void run() {
			Runtime rt = Runtime.getRuntime();
			try {

				String[] cmds = new String[] { "screenrecord", TMP_PATH };
				Process proc = rt.exec(cmds);
				while (!isInterrupted()) {

					try {
						int code = proc.exitValue();

						// If the recording is still running, we won't reach
						// here,
						// but will land in the catch block below.
						Message msg = Message.obtain(mHandler, MSG_TASK_ENDED, code, 0, null);
						mHandler.sendMessage(msg);

						// No need to stop the process, so we can exit this
						// method early
						return;
					} catch (IllegalThreadStateException ignore) {
						// ignored
					}
				}

				// Terminate the recording process
				// HACK: There is no way to send SIGINT to a process, so we...
				// hack, busybox required!
				rt.exec(new String[] { "killall", "-2", "screenrecord" });
			} catch (Throwable e) {
				// Notify something went wrong
				Message msg = Message.obtain(mHandler, MSG_TASK_ERROR);
				mHandler.sendMessage(msg);
			}
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	static class IncomingHandler extends Handler {
		private final WeakReference<ScreenRecordingService> mService;

		IncomingHandler(ScreenRecordingService service) {
			mService = new WeakReference<ScreenRecordingService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			ScreenRecordingService service = mService.get();
			if (service != null) {
				service.handleMessage(msg);
			}
		}
	}

	public void handleMessage(Message msg) {
		if (msg.what == MSG_TASK_ENDED) {
			// The screenrecord process stopped, act as if user
			// requested the record to stop.
			stopScreenrecord();
		} else if (msg.what == MSG_TASK_ERROR) {
			mCaptureThread = null;
			updateStatus(STATUS_ERROR, (String) msg.obj);
			Toast.makeText(ScreenRecordingService.this, R.string.screenrecord_toast_error, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (mHandler == null) {
			mHandler = new IncomingHandler(this);
		}

		mRecordingStatus = STATUS_IDLE;

		Notification.Builder builder = new Notification.Builder(this)
				.setTicker(getString(R.string.screenrecord_notif_ticker))
				.setContentTitle(getString(R.string.screenrecord_notif_title))
				.setSmallIcon(R.drawable.ic_sysbar_camera).setWhen(System.currentTimeMillis());

		Intent stopIntent = new Intent(this, ScreenRecordingService.class);
		stopIntent.setAction(ACTION_SCREEN_RECORDING_STOP);
		PendingIntent stopPendIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Action ac = new Builder(Icon.createWithResource(this, R.drawable.ic_media_stop),
				getString(R.string.screenrecord_notif_stop), stopPendIntent).build();

		builder.addAction(ac);

		mRecordingNotif = builder.build();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent != null && intent.getAction() != null) {
			if (intent.getAction().equals(ACTION_SCREEN_RECORDING_START)) {
				startScreenrecord();
			} else if (intent.getAction().equals(ACTION_SCREEN_RECORDING_STOP)) {
				stopScreenrecord();
			}
		} else {
			stopSelf();
		}

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if (isRecording()) {
			stopScreenrecord();
		}
		mHandler = null;
		super.onDestroy();
	}

	private boolean isRecording() {
		return (mRecordingStatus == STATUS_RECORDING);
	}

	private boolean isProcessing() {
		return (mRecordingStatus == STATUS_PROCESSING);
	}

	private void updateStatus(int status, String message) {
		mRecordingStatus = status;
		if (isRecording()) {
			log("Fire notification.");
			startForeground(SCREENRECORD_NOTIFICATION_ID, mRecordingNotif);
		} else {
			log("Stop notification.");
			stopForeground(true);
		}

		Intent intent = new Intent(ACTION_SCREEN_RECORDING_STATUS_CHANGED);
		intent.putExtra(EXTRA_RECORDING_STATUS, mRecordingStatus);
		if (message != null) {
			intent.putExtra(EXTRA_STATUS_MESSAGE, message);
		}
		sendBroadcast(intent);
	}

	private void updateStatus(int status) {
		updateStatus(status, null);
	}

	private void startScreenrecord() {
		if (isRecording()) {
			return;
		} else if (isProcessing()) {
			Toast.makeText(this, R.string.screenrecord_toast_processing, Toast.LENGTH_SHORT).show();
			return;
		}

		mCaptureThread = new CaptureThread();
		mCaptureThread.start();
		updateStatus(STATUS_RECORDING);
		log("Start recording...");
	}

	private void stopScreenrecord() {
		if (!isRecording()) {
			return;
		}

		updateStatus(STATUS_PROCESSING);
		log("Start processing...");
		try {
			mCaptureThread.interrupt();
		} catch (Throwable e) { /* ignore */
			e.printStackTrace();
		}

		// Wait a bit for capture thread to finish
		while (mCaptureThread.isAlive()) {
			// wait...
		}

		// Give a second to screenrecord to process the file
		mHandler.postDelayed(new Runnable() {
			public void run() {
				File output = null;

				try {

					mCaptureThread = null;

					String fileName = "SCR_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date())
							+ ".mp4";

					log("Saving filename... " + fileName);

					File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
					if (!picturesDir.exists()) {
						if (!picturesDir.mkdirs()) {
							return;
						}
					}

					File screenrecord = new File(picturesDir, "Screenrecord");
					if (!screenrecord.exists()) {
						if (!screenrecord.mkdir()) {
							return;
						}
					}

					File input = new File(TMP_PATH);
					output = new File(screenrecord, fileName);

					copyFileUsingStream(input, output);
					input.delete();
					Toast.makeText(ScreenRecordingService.this,
							String.format(getString(R.string.screenrecord_toast_saved), output.getPath()),
							Toast.LENGTH_LONG).show();
				} catch (Throwable e) {
					Toast.makeText(ScreenRecordingService.this, R.string.screenrecord_toast_save_error,
							Toast.LENGTH_LONG).show();
				} finally {

					if (output != null) {
						// Make it appear in gallery, run MediaScanner
						scanPath = output.getAbsolutePath();
						conn = new MediaScannerConnection(getApplicationContext(), ScreenRecordingService.this);
						conn.connect();
					}

					updateStatus(STATUS_IDLE);

					stopSelf();
				}
			}
		}, 2000);
	}

	private static void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			try {
				is.close();
				os.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onMediaScannerConnected() {
		conn.scanFile(scanPath, null);

	}

	@Override
	public void onScanCompleted(String arg0, Uri arg1) {
		try {
			conn.disconnect();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			conn = null;
		}

	}
}
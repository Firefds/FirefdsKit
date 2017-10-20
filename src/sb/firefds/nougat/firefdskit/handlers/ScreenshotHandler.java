package sb.firefds.nougat.firefdskit.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenshotHandler implements InvocationHandler {

	private static Context mContext;
	private String mScreenshotStr;
	private Drawable mScreenshotIcon;
	private static final Object mScreenshotLock = new Object();
	private static ServiceConnection mScreenshotConnection = null;

	public ScreenshotHandler(Context context, String mScreenshotStr, Drawable mScreenshotIcon) {
		mContext = context;
		this.mScreenshotStr = mScreenshotStr;
		this.mScreenshotIcon = mScreenshotIcon;
	}

	private void takeScreenshot() {
		final Handler handler = new Handler();
		// take a screenshot after a 0.5s delay
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				takeScreenshot(handler);
			}
		}, 500);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();

		if (methodName.equals("create")) {
			mContext = (Context) args[0];
			Resources res = mContext.getResources();
			LayoutInflater li = (LayoutInflater) args[3];
			int layoutId = res.getIdentifier("global_actions_item", "layout", "android");
			View v = li.inflate(layoutId, (ViewGroup) args[2], false);

			ImageView icon = (ImageView) v.findViewById(res.getIdentifier("icon", "id", "android"));
			icon.setImageDrawable(mScreenshotIcon);

			TextView messageView = (TextView) v.findViewById(res.getIdentifier("message", "id", "android"));
			messageView.setText(mScreenshotStr);

			TextView statusView = (TextView) v.findViewById(res.getIdentifier("status", "id", "android"));
			statusView.setVisibility(View.GONE);

			return v;
		} else if (methodName.equals("onPress")) {
			takeScreenshot();
			return null;
		} else if (methodName.equals("onLongPress")) {
			return true;
		} else if (methodName.equals("showDuringKeyguard")) {
			return true;
		} else if (methodName.equals("showBeforeProvisioning")) {
			return true;
		} else if (methodName.equals("isEnabled")) {
			return true;
		} else if (methodName.equals("showConditional")) {
			return true;
		} else {
			return null;
		}
	}

	private static void takeScreenshot(final Handler mHandler) {
		synchronized (mScreenshotLock) {
			if (mScreenshotConnection != null) {
				return;
			}
			ComponentName cn = new ComponentName("com.android.systemui",
					"com.android.systemui.screenshot.TakeScreenshotService");
			Intent intent = new Intent();
			intent.setComponent(cn);
			ServiceConnection conn = new ServiceConnection() {
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					synchronized (mScreenshotLock) {
						if (mScreenshotConnection != this) {
							return;
						}
						Messenger messenger = new Messenger(service);
						Message msg = Message.obtain(null, 1);
						final ServiceConnection myConn = this;

						Handler h = new Handler(mHandler.getLooper()) {
							@Override
							public void handleMessage(Message msg) {
								synchronized (mScreenshotLock) {
									if (mScreenshotConnection == myConn) {
										mContext.unbindService(mScreenshotConnection);
										mScreenshotConnection = null;
										mHandler.removeCallbacks(mScreenshotTimeout);
									}
								}
							}
						};
						msg.replyTo = new Messenger(h);
						msg.arg1 = msg.arg2 = 0;
						try {
							messenger.send(msg);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onServiceDisconnected(ComponentName name) {
				}
			};
			if (mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
				mScreenshotConnection = conn;
				mHandler.postDelayed(mScreenshotTimeout, 10000);
			}
		}
	}

	private static final Runnable mScreenshotTimeout = new Runnable() {
		@Override
		public void run() {
			synchronized (mScreenshotLock) {
				if (mScreenshotConnection != null) {
					mContext.unbindService(mScreenshotConnection);
					mScreenshotConnection = null;
				}
			}
		}
	};
}

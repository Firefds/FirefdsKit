/*
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
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

package sb.firefds.xtouchwizS5.battery;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import de.robv.android.xposed.XposedBridge;
import sb.firefds.xtouchwizS5.battery.BatteryInfoManager.BatteryStatusListener;
import sb.firefds.xtouchwizS5.receivers.BroadcastSubReceiver;

public class StatusBarIconManager implements BroadcastSubReceiver {
	private static final String TAG = "GB:StatusBarIconManager";
	private static final boolean DEBUG = false;

	public static final int SI_MODE_STOCK = 1;
	public static final int SI_MODE_DISABLED = 2;

	public static final int JELLYBEAN = 0;
	public static final int LOLLIPOP = 1;

	public static final int FLAG_COLORING_ENABLED_CHANGED = 1 << 0;
	public static final int FLAG_SIGNAL_ICON_MODE_CHANGED = 1 << 1;
	public static final int FLAG_ICON_COLOR_SECONDARY_CHANGED = 1 << 3;
	public static final int FLAG_DATA_ACTIVITY_COLOR_CHANGED = 1 << 4;
	public static final int FLAG_ICON_STYLE_CHANGED = 1 << 5;
	public static final int FLAG_ICON_ALPHA_CHANGED = 1 << 6;
	private static final int FLAG_ALL = 0xFF;

	private BatteryInfoManager mBatteryInfo;
	private Resources mSystemUiRes;
	private Map<String, Integer[]> mBasicIconIds;
	private Map<String, SoftReference<Drawable>> mIconCache;
	private ColorInfo mColorInfo;
	private List<IconManagerListener> mListeners;

	public interface IconManagerListener {
		void onIconManagerStatusChanged(int flags, ColorInfo colorInfo);
	}

	public static class ColorInfo {
		public boolean coloringEnabled;
		public boolean wasColoringEnabled;
		public int defaultIconColor;
		public int[] iconColor;
		public int defaultDataActivityColor;
		public int[] dataActivityColor;
		public int signalIconMode;
		public int iconStyle;
		public float alphaSignalCluster;
		public float alphaTextAndBattery;
	}

	private static void log(String message) {
		XposedBridge.log(TAG + ": " + message);
	}

	public StatusBarIconManager(Context context, Context gbContext) throws Throwable {
		try {
			mIconCache = new HashMap<String, SoftReference<Drawable>>();

			initColorInfo();
			mBatteryInfo = new BatteryInfoManager(gbContext);

			mListeners = new ArrayList<IconManagerListener>();
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

	private void initColorInfo() {
		mColorInfo = new ColorInfo();
		mColorInfo.coloringEnabled = false;
		mColorInfo.wasColoringEnabled = false;
		mColorInfo.defaultIconColor = getDefaultIconColor();
		mColorInfo.iconColor = new int[2];
		mColorInfo.iconStyle = LOLLIPOP;
		mColorInfo.alphaSignalCluster = 1.0f;
		mColorInfo.alphaTextAndBattery = 1.0f;
	}

	@Override
	public void onBroadcastReceived(Context context, Intent intent) {
		try {
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				mBatteryInfo.updateBatteryInfo(intent);
			}
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

	public void registerListener(IconManagerListener listener) {
		if (!mListeners.contains(listener)) {
			if (listener instanceof BatteryStatusListener) {
				mBatteryInfo.registerListener((BatteryStatusListener) listener);
			}
			mListeners.add(listener);
			listener.onIconManagerStatusChanged(FLAG_ALL, mColorInfo);
		}

	}

	public void unregisterListener(IconManagerListener listener) {
		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}

	private void notifyListeners(int flags) {
		for (IconManagerListener listener : mListeners) {
			listener.onIconManagerStatusChanged(flags, mColorInfo);
		}
	}

	public void refreshState() {
		notifyListeners(FLAG_ALL);
	}

	public boolean isColoringEnabled() {
		return mColorInfo.coloringEnabled;
	}

	public int getDefaultIconColor() {
		return Color.WHITE;
	}

	public void setSignalIconMode(int mode) {
		if (mColorInfo.signalIconMode != mode) {
			mColorInfo.signalIconMode = mode;
			clearCache();
			notifyListeners(FLAG_SIGNAL_ICON_MODE_CHANGED);
		}
	}

	public int getSignalIconMode() {
		return mColorInfo.signalIconMode;
	}

	public int getIconColor(int index) {
		return mColorInfo.iconColor[index];
	}

	public int getIconColor() {
		return getIconColor(0);
	}

	public int getDataActivityColor() {
		return getDataActivityColor(0);
	}

	public int getDataActivityColor(int index) {
		return mColorInfo.dataActivityColor[index];
	}

	public void setDataActivityColor(int index, int color) {
		if (mColorInfo.dataActivityColor[index] != color) {
			mColorInfo.dataActivityColor[index] = color;
			notifyListeners(FLAG_DATA_ACTIVITY_COLOR_CHANGED);
		}
	}

	public void setDataActivityColor(int color) {
		setDataActivityColor(0, color);
	}

	public void setIconStyle(int style) {
		if ((style == JELLYBEAN || style == LOLLIPOP) && mColorInfo.iconStyle != style) {
			mColorInfo.iconStyle = style;
			clearCache();
			notifyListeners(FLAG_ICON_STYLE_CHANGED);
		}
	}

	public void setIconAlpha(float alphaSignalCluster, float alphaTextAndBattery) {
		if (mColorInfo.alphaSignalCluster != alphaSignalCluster
				|| mColorInfo.alphaTextAndBattery != alphaTextAndBattery) {
			mColorInfo.alphaSignalCluster = alphaSignalCluster;
			mColorInfo.alphaTextAndBattery = alphaTextAndBattery;
			notifyListeners(FLAG_ICON_ALPHA_CHANGED);
		}
	}

	public Drawable applyColorFilter(int index, Drawable drawable, PorterDuff.Mode mode) {
		if (drawable != null) {
			drawable.setColorFilter(mColorInfo.iconColor[index], mode);
		}
		return drawable;
	}

	public Drawable applyColorFilter(int index, Drawable drawable) {
		return applyColorFilter(index, drawable, PorterDuff.Mode.SRC_IN);
	}

	public Drawable applyColorFilter(Drawable drawable) {
		return applyColorFilter(0, drawable, PorterDuff.Mode.SRC_IN);
	}

	public Drawable applyColorFilter(Drawable drawable, PorterDuff.Mode mode) {
		return applyColorFilter(0, drawable, mode);
	}

	public Drawable applyDataActivityColorFilter(int index, Drawable drawable) {
		drawable.setColorFilter(mColorInfo.dataActivityColor[index], PorterDuff.Mode.SRC_IN);
		return drawable;
	}

	public Drawable applyDataActivityColorFilter(Drawable drawable) {
		return applyDataActivityColorFilter(0, drawable);
	}

	public void clearCache() {
		mIconCache.clear();
		if (DEBUG)
			log("Cache cleared");
	}

	public BatteryInfoManager getBatteryInfoManager() {
		return mBatteryInfo;
	}

	public Drawable getBasicIcon(int resId) {
		if (resId == 0)
			return null;

		try {
			String key = mSystemUiRes.getResourceEntryName(resId);
			if (!mBasicIconIds.containsKey(key)) {
				if (DEBUG)
					log("getBasicIcon: no record for key: " + key);
				return null;
			}

			return null;

		} catch (Throwable t) {
			log("getBasicIcon: " + t.getMessage());
			return null;
		}
	}
}
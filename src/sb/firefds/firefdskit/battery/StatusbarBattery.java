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

package sb.firefds.firefdskit.battery;

import android.graphics.Paint;
import android.view.View;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.firefdskit.battery.StatusBarIconManager.ColorInfo;
import sb.firefds.firefdskit.battery.StatusBarIconManager.IconManagerListener;

public class StatusbarBattery implements IconManagerListener {

	private View mBattery;

	private int mFrameAlpha;

	public StatusbarBattery(View batteryView) {
		mBattery = batteryView;
		try {
			final Paint framePaint = (Paint) XposedHelpers.getObjectField(mBattery, "mFramePaint");
			mFrameAlpha = framePaint.getAlpha();
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public View getView() {
		return mBattery;
	}

	public void setColors(int mainColor, int frameColor, int chargeColor) {
		if (mBattery != null) {
			try {
				final int[] colors = (int[]) XposedHelpers.getObjectField(mBattery, "mColors");
				colors[colors.length - 1] = mainColor;
				final Paint framePaint = (Paint) XposedHelpers.getObjectField(mBattery, "mFramePaint");
				framePaint.setColor(frameColor);
				framePaint.setAlpha(mFrameAlpha);
				XposedHelpers.setIntField(mBattery, "mChargeColor", chargeColor);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	}

	@Override
	public void onIconManagerStatusChanged(int flags, ColorInfo colorInfo) {
		try {

			if ((flags & StatusBarIconManager.FLAG_ICON_ALPHA_CHANGED) != 0) {
				mBattery.setAlpha(colorInfo.alphaTextAndBattery);
			}

		} catch (Throwable e) {
			XposedBridge.log(e.toString());
		}
	}
}

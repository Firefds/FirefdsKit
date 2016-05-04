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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.TextView;
import de.robv.android.xposed.XposedBridge;
import sb.firefds.xtouchwizS5.battery.BatteryInfoManager.BatteryData;
import sb.firefds.xtouchwizS5.battery.BatteryInfoManager.BatteryStatusListener;
import sb.firefds.xtouchwizS5.battery.StatusBarIconManager.ColorInfo;
import sb.firefds.xtouchwizS5.battery.StatusBarIconManager.IconManagerListener;

public class StatusbarBatteryPercentage implements IconManagerListener, BatteryStatusListener {
	private TextView mPercentage;
	private int mIconColor;
	private String mPercentSign;
	private BatteryData mBatteryData;
	private ValueAnimator mChargeAnim;
	private int mChargingStyle;

	public static final int CHARGING_STYLE_NONE = 0;
	public static final int CHARGING_STYLE_STATIC = 1;
	public static final int CHARGING_STYLE_ANIMATED = 2;

	public StatusbarBatteryPercentage(TextView clockView) {
		mPercentage = clockView;
		mPercentSign = "";
		mChargingStyle = CHARGING_STYLE_NONE;
	}

	private boolean startChargingAnimation() {
		try {
			if (mChargeAnim == null || !mChargeAnim.isRunning()) {
				mChargeAnim = ValueAnimator.ofObject(new ArgbEvaluator(), mIconColor, Color.GREEN);

				mChargeAnim.addUpdateListener(new AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator va) {
						mPercentage.setTextColor((Integer) va.getAnimatedValue());
					}
				});
				mChargeAnim.addListener(new AnimatorListener() {
					@Override
					public void onAnimationCancel(Animator animation) {
						mPercentage.setTextColor(mIconColor);
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						mPercentage.setTextColor(mIconColor);
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}

					@Override
					public void onAnimationStart(Animator animation) {
					}
				});

				mChargeAnim.setDuration(1000);
				mChargeAnim.setRepeatMode(ValueAnimator.REVERSE);
				mChargeAnim.setRepeatCount(ValueAnimator.INFINITE);
				mChargeAnim.start();
				return true;
			}
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
		return false;
	}

	private boolean stopChargingAnimation() {
		try {
			if (mChargeAnim != null && mChargeAnim.isRunning()) {
				mChargeAnim.end();
				mChargeAnim.removeAllUpdateListeners();
				mChargeAnim.removeAllListeners();
				mChargeAnim = null;
				return true;
			}
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
		return false;
	}

	public TextView getView() {
		return mPercentage;
	}

	public void setTextColor(int color) {
		mIconColor = color;
		stopChargingAnimation();
		update();
	}

	public void setTextSize(int size) {
		mPercentage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
	}

	public void setPercentSign(String percentSign) {
		mPercentSign = percentSign;
		update();
	}

	public void setChargingStyle(int style) {
		mChargingStyle = style;
		update();
	}

	public void update() {
		if (mBatteryData == null)
			return;

		try {
			mPercentage.setText(mBatteryData.level + mPercentSign);

			if (mBatteryData.charging && mBatteryData.level < 100) {
				if (mChargingStyle == CHARGING_STYLE_STATIC) {
					stopChargingAnimation();
					mPercentage.setTextColor(Color.GREEN);
				} else if (mChargingStyle == CHARGING_STYLE_ANIMATED) {
					startChargingAnimation();
				} else {
					stopChargingAnimation();
					mPercentage.setTextColor(mIconColor);
				}
			} else {
				stopChargingAnimation();
				mPercentage.setTextColor(mIconColor);
			}
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

	public void setVisibility(int visibility) {
		mPercentage.setVisibility(visibility);
	}

	@Override
	public void onIconManagerStatusChanged(int flags, ColorInfo colorInfo) {
		try {
			if ((flags & StatusBarIconManager.FLAG_ICON_ALPHA_CHANGED) != 0) {
				mPercentage.setAlpha(colorInfo.alphaTextAndBattery);
			}
		} catch (Throwable e) {
			XposedBridge.log(e.toString());

		}
	}

	@Override
	public void onBatteryStatusChanged(BatteryData batteryData) {
		mBatteryData = batteryData;
		update();
	}
}

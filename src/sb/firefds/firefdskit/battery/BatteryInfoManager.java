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

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryInfoManager {
	private BatteryData mBatteryData;
	private ArrayList<BatteryStatusListener> mListeners;

	class BatteryData {
		boolean charging;
		int level;
	}

	public interface BatteryStatusListener {
		void onBatteryStatusChanged(BatteryData batteryData);
	}

	public BatteryInfoManager(Context gbContext) {
		mBatteryData = new BatteryData();
		mBatteryData.charging = false;
		mBatteryData.level = 0;
		mListeners = new ArrayList<BatteryStatusListener>();

	}

	public void registerListener(BatteryStatusListener listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	private void notifyListeners() {
		for (BatteryStatusListener listener : mListeners) {
			listener.onBatteryStatusChanged(mBatteryData);
		}
	}

	public void updateBatteryInfo(Intent intent) {
		int newLevel = (int) (100f * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) / intent.getIntExtra(
				BatteryManager.EXTRA_SCALE, 100));
		boolean newCharging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;

		if (mBatteryData.level != newLevel || mBatteryData.charging != newCharging) {

			mBatteryData.level = newLevel;
			mBatteryData.charging = newCharging;
			notifyListeners();
		}
	}

}

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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import sb.firefds.xtouchwizS5.utils.Utils;

public class MainApplication extends Application {

	private static Context mContext;
	private static Point windowsSize;
	private static Handler mHandler;
	private static SharedPreferences sharedPreferences;

	public static SharedPreferences getSharedPreferences() {
		return sharedPreferences;
	}

	public static void setSharedPreferences(SharedPreferences sharedPreferences) {
		MainApplication.sharedPreferences = sharedPreferences;
	}

	public static Point getWindowsSize() {
		return windowsSize;
	}

	public static void setWindowsSize(Point windowsSize) {
		MainApplication.windowsSize = windowsSize;
	}

	public MainApplication() {
		super();
	}

	public static Context getAppContext() {
		return mContext;
	}

	public static Handler getHandler() {
		if (mHandler == null) {
			mHandler = new Handler(Looper.getMainLooper());
		}
		return mHandler;
	}

	public void onCreate() {

		super.onCreate();
		mContext = getApplicationContext();

		if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("forceEnglish", false)) {
			Utils.setEnglishLocale(mContext);
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("forceEnglish", false)) {
			Utils.setEnglishLocale(mContext);
		}
	}

}
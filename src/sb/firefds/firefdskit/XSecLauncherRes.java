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
package sb.firefds.firefdskit;

import android.content.res.XModuleResources;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import sb.firefds.firefdskit.utils.Packages;

public class XSecLauncherRes {

	public static void doHook(XSharedPreferences prefs, InitPackageResourcesParam resparam,
			XModuleResources moduleResources) {
		if (prefs.getBoolean("launcherScrollWallpaper", true)) {
			try {
				resparam.res.setReplacement(Packages.LAUNCHER, "bool", "config_fixedWallpaperOffset", false);
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}

		}

	}

}

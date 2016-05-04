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

import android.content.res.XModuleResources;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import sb.firefds.xtouchwizS5.utils.Packages;

public class XSysUIQuickSettingsRes {

	public static void doHook(final XSharedPreferences prefs, InitPackageResourcesParam resparam,
			XModuleResources moduleResources) {

		int quickSettingsColumns = prefs.getInt("quickSettingsColumns", 5);
		if (quickSettingsColumns != 5) {
			try {
				resparam.res.setReplacement(Packages.SYSTEM_UI, "integer", "quick_settings_num_columns",
						quickSettingsColumns);
			} catch (Throwable e) {
				XposedBridge.log(e.toString());

			}
		}

	}

}

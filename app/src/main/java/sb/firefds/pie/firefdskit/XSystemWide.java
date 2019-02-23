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
package sb.firefds.pie.firefdskit;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class XSystemWide {

    private static XSharedPreferences prefs;

    public static void doHook(XSharedPreferences prefs) {

        XSystemWide.prefs = prefs;

        try {
            setSystemWideTweaks();

        } catch (Throwable e) {
            XposedBridge.log(e);
        }

    }

    private static void setSystemWideTweaks() {

        if (prefs.getBoolean("disbaleLowBatteryCloseWarningLevel", false)) {
            try {
                /*XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_lowBatteryWarningLevel", 1);
                XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_criticalBatteryWarningLevel",
                        1);*/
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        } else if (prefs.getInt("configCriticalBatteryWarningLevel", 5) != 5) {
            try {
               /* XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_criticalBatteryWarningLevel",
                        prefs.getInt("configCriticalBatteryWarningLevel", 5));
                XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_lowBatteryCloseWarningLevel",
                        prefs.getInt("configCriticalBatteryWarningLevel", 5));*/
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }
}

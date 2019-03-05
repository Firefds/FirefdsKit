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

import android.content.pm.Signature;


import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

public class XAndroidPackage {

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {


        try {
            if (prefs.getBoolean("disableFlagSecure", false)) {
                Class<?> windowStateClass =
                        XposedHelpers.findClass("android.server.wm.WindowState", classLoader);

                XposedHelpers.findAndHookMethod("android.server.wm. WindowManagerService",
                        classLoader,
                        "isSecureLocked",
                        windowStateClass,
                        XC_MethodReplacement.returnConstant(Boolean.FALSE));
            }

            if (prefs.getBoolean("disableSignatureCheck", false)) {
                XposedHelpers.findAndHookMethod("android.server.pm. PackageManagerServiceUtils",
                        classLoader,
                        "compareSignatures",
                        Signature[].class,
                        Signature[].class,
                        XC_MethodReplacement.returnConstant(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*
 * Copyright (C) 2022 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.s.firefdskit.dialogs;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import sb.firefds.s.firefdskit.R;
import sb.firefds.s.firefdskit.utils.Utils;

public class CreditDialog {

    public CreditDialog() {
    }

    public AlertDialog.Builder getDialog(Context context) {
        PackageInfo pInfo;
        String pkgVersion = "";
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            pkgVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Utils.log(e);
        }
        TextView tv = new TextView(context);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(R.string.credit_details);
        tv.setPadding(16, 16, 16, 16);
        return new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.app_name) + " " + pkgVersion)
                .setView(tv)
                .setNeutralButton(R.string.ok_btn, (dialog, id) -> dialog.dismiss());
    }
}

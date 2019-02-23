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
package sb.firefds.pie.firefdskit.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import java.util.Objects;

import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Constants.REBOOT_DEVICE;

@SuppressWarnings("deprecation")
public class WanamRebootActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int reboot = Objects.requireNonNull(getIntent().getExtras()).getInt(REBOOT_DEVICE);
        try {
            switch (reboot) {
                case 0:
                    rebootDevice();
                    break;
                case 2:
                    softRebootOptions();
                    break;

            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void rebootDevice() throws Throwable {
        Utils.closeStatusBar(this);
        ProgressDialog.show(this, "", getString(R.string.rebooting));
        Utils.reboot(this);
    }

    private void softRebootOptions() {

        AlertDialog alertDialog;
        AlertDialog.Builder rebootOptionsDiag;
        rebootOptionsDiag = new AlertDialog.Builder(this);

        rebootOptionsDiag.setTitle(getString(R.string.reboot_options))
                .setItems(R.array.reboot_options, (dialog, which) -> {

                    switch (which) {
                        case 0:
                            Utils.reboot(getBaseContext());
                            break;
                        case 1:
                            Utils.rebootEPM(getBaseContext(), "recovery");
                            break;
                        case 3:
                            Utils.rebootEPM(getBaseContext(), "download");
                            break;
                    }

                }).setCancelable(true).setOnCancelListener(dialog -> finish());

        alertDialog = rebootOptionsDiag.create();
        alertDialog.show();
    }
}

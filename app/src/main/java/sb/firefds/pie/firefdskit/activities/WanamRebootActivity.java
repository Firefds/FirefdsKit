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

import android.os.Bundle;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import sb.firefds.pie.firefdskit.FirefdsKitActivity;
import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.XCscFeaturesManager;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Constants.REBOOT_DEVICE;

public class WanamRebootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int reboot = Objects.requireNonNull(getIntent().getExtras()).getInt(REBOOT_DEVICE);
        try {
            switch (reboot) {
                case 0:
                    rebootDevice();
                    break;
                case 1:
                    softRebootDevice();
                    break;
                case 2:
                    softRebootOptions();
                    break;
                case 3:
                    Utils.rebootEPM("recovery");
                    break;
                case 4:
                    Utils.rebootEPM("download");
                    break;
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void rebootDevice() throws Throwable {
        Utils.closeStatusBar(this);
        showRebootDialog();
        if (!Utils.isOmcEncryptedFlag()) {
            XCscFeaturesManager.applyCscFeatures(FirefdsKitActivity.getSharedPreferences());
        }
        Utils.reboot();
    }

    private void softRebootDevice() throws Throwable {
        Utils.closeStatusBar(this);
        showRebootDialog();
        Utils.performSoftReboot();
    }

    private void softRebootOptions() {

        AlertDialog alertDialog;
        AlertDialog.Builder rebootOptionsDiag;
        rebootOptionsDiag = new AlertDialog.Builder(this);

        rebootOptionsDiag.setTitle(getString(R.string.reboot_options))
                .setItems(R.array.reboot_options, (dialog, which) -> {

                    switch (which) {
                        case 0:
                            Utils.reboot();
                            break;
                        case 1:
                            Utils.rebootEPM("recovery");
                            break;
                        case 3:
                            Utils.rebootEPM("download");
                            break;
                    }

                }).setCancelable(true).setOnCancelListener(dialog -> finish());

        alertDialog = rebootOptionsDiag.create();
        alertDialog.show();
    }

    private void showRebootDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.progress_dialog).create().show();
    }
}

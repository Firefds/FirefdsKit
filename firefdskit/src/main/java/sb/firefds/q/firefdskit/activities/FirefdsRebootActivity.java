/*
 * Copyright (C) 2020 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.q.firefdskit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import sb.firefds.q.firefdskit.R;
import sb.firefds.q.firefdskit.utils.Utils;

import static sb.firefds.q.firefdskit.utils.Constants.DOWNLOAD_ACTION;
import static sb.firefds.q.firefdskit.utils.Constants.QUICK_REBOOT_DEVICE_ACTION;
import static sb.firefds.q.firefdskit.utils.Constants.REBOOT_ACTION;
import static sb.firefds.q.firefdskit.utils.Constants.REBOOT_DEVICE_ACTION;
import static sb.firefds.q.firefdskit.utils.Constants.RECOVERY_ACTION;

public class FirefdsRebootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String reboot = Objects.requireNonNull(getIntent().getExtras()).getString(REBOOT_ACTION);
        try {
            switch (Objects.requireNonNull(reboot)) {
                case REBOOT_DEVICE_ACTION:
                    rebootDevice();
                    break;
                case QUICK_REBOOT_DEVICE_ACTION:
                    quickRebootDevice();
                    break;
                case RECOVERY_ACTION:
                    Utils.rebootEPM(RECOVERY_ACTION);
                    break;
                case DOWNLOAD_ACTION:
                    Utils.rebootEPM(DOWNLOAD_ACTION);
                    break;
            }

        } catch (Throwable e) {
            Utils.log(e);
        }
    }

    private void rebootDevice() throws Throwable {
        Utils.closeStatusBar(this);
        showRebootDialog();
        Utils.reboot();
    }

    private void quickRebootDevice() throws Throwable {
        Utils.closeStatusBar(this);
        showRebootDialog();
        Utils.performQuickReboot();
    }

    private void showRebootDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setView(R.layout.progress_dialog).create().show();
    }
}

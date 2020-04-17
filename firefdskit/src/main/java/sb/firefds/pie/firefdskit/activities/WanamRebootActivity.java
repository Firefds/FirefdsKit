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

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import sb.firefds.pie.firefdskit.rebootactions.RebootAction;
import sb.firefds.pie.firefdskit.rebootactions.RebootActionFactory;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Constants.REBOOT_ACTION;

public class WanamRebootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String reboot = Objects.requireNonNull(getIntent().getExtras()).getString(REBOOT_ACTION);
        try {
            RebootAction rebootAction = RebootActionFactory.getRebootAction(reboot, this);
            if (rebootAction != null) {
                rebootAction.reboot();
            }
        } catch (Throwable e) {
            Utils.log(e);
        }
    }
}

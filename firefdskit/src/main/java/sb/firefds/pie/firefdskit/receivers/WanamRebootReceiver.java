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
package sb.firefds.pie.firefdskit.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Objects;

import sb.firefds.pie.firefdskit.activities.WanamRebootActivity;
import sb.firefds.pie.firefdskit.notifications.RebootNotification;

import static sb.firefds.pie.firefdskit.utils.Constants.REBOOT_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.REBOOT_DEVICE_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.QUICK_REBOOT_DEVICE_ACTION;

public class WanamRebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Objects.requireNonNull(action).equalsIgnoreCase(REBOOT_DEVICE_ACTION) ||
                action.equalsIgnoreCase(QUICK_REBOOT_DEVICE_ACTION)) {
            RebootNotification.cancel(context);
            Intent rebootIntent = new Intent(context, WanamRebootActivity.class);
            Bundle b = new Bundle();
            b.putString(REBOOT_ACTION, action);
            rebootIntent.putExtras(b);
            rebootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(rebootIntent);
        }
    }
}

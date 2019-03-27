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

import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.activities.WanamRebootActivity;
import sb.firefds.pie.firefdskit.notifications.RebootNotification;

import static sb.firefds.pie.firefdskit.FirefdsKitActivity.getActivity;

public class WanamRebootReceiver extends BroadcastReceiver {

    private static final String REBOOT_DEVICE =
            getActivity().getString(R.string.reboot_device_action);
    private static final String SOFT_REBOOT_DEVICE =
            getActivity().getString(R.string.soft_reboot_device_action);
    private static final String REBOOT_OPTIONS =
            getActivity().getString(R.string.reboot_options_action);

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Objects.requireNonNull(action).equalsIgnoreCase(REBOOT_DEVICE)
                || action.equalsIgnoreCase(SOFT_REBOOT_DEVICE)
                || action.equalsIgnoreCase(REBOOT_OPTIONS)) {
            RebootNotification.cancel(context);
            Intent rebootIntent = new Intent(context, WanamRebootActivity.class);
            Bundle b = new Bundle();
            if (action.equalsIgnoreCase(REBOOT_DEVICE)) {
                b.putInt(REBOOT_DEVICE, 0);
            } else if (action.equalsIgnoreCase(SOFT_REBOOT_DEVICE)) {
                b.putInt(REBOOT_DEVICE, 1);
            } else if (action.equalsIgnoreCase(REBOOT_OPTIONS)) {
                b.putInt(REBOOT_DEVICE, 2);
            }
            rebootIntent.putExtras(b);
            rebootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(rebootIntent);
        }
    }
}

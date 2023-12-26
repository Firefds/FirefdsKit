/*
 * Copyright (C) 2023 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.u.firefdskit.notifications;

import static androidx.core.content.ContextCompat.getSystemService;
import static sb.firefds.u.firefdskit.utils.Constants.QUICK_REBOOT_DEVICE_ACTION;
import static sb.firefds.u.firefdskit.utils.Constants.REBOOT_DEVICE_ACTION;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;

import androidx.core.app.TaskStackBuilder;

import java.util.Objects;

import sb.firefds.u.firefdskit.FirefdsKitActivity;
import sb.firefds.u.firefdskit.R;
import sb.firefds.u.firefdskit.activities.FirefdsRebootActivity;

public class RebootNotification {

    private static final String NOTIFICATION_TAG = "RebootNotification";

    @SuppressLint("NewApi")
    public static void notify(final Context context, final int n, boolean showQuickReboot) {

        final Resources res = context.getResources();

        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);

        final String ticker = res.getString(R.string.reboot_required);
        final String title = res.getString(R.string.reboot_required_title);
        final String text = res.getString(R.string.reboot_required_message);

        NotificationChannel mChannel = new NotificationChannel("Reboot_ID",
                "Reboot Name",
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager mNotificationManager = getSystemService(context, NotificationManager.class);
        Objects.requireNonNull(mNotificationManager).createNotificationChannel(mChannel);

        final Notification.Builder builder = new Notification.Builder(context, "Reboot_ID")
                .setSmallIcon(R.drawable.ic_restart_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setLargeIcon(picture)
                .setTicker(ticker)
                .setNumber(n)
                .setWhen(0)
                .setContentIntent(PendingIntent.getActivity(context,
                        0,
                        new Intent(context, FirefdsKitActivity.class),
                        PendingIntent.FLAG_IMMUTABLE))
                .setStyle(new Notification.BigTextStyle()
                        .bigText(text)
                        .setBigContentTitle(title)
                        .setSummaryText(context.getString(R.string.pending_changes)))
                .setAutoCancel(true);

        Intent rebootIntent = new Intent(context, FirefdsRebootActivity.class)
                .setAction(showQuickReboot ? QUICK_REBOOT_DEVICE_ACTION : REBOOT_DEVICE_ACTION);

        PendingIntent rebootPendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(rebootIntent)
                .getPendingIntent(1337, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.addAction(new Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.ic_restart_notification),
                showQuickReboot ? res.getString(R.string.quick_reboot) : res.getString(R.string.reboot),
                rebootPendingIntent)
                .build());

        notify(context, builder.build());
    }

    @SuppressLint("NotificationPermission")
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_TAG, 0, notification);
    }
}
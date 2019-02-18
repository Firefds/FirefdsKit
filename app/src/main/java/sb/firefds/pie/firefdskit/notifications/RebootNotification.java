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
package sb.firefds.pie.firefdskit.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;

import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.XTouchWizActivity;

public class RebootNotification {

    private static final String NOTIFICATION_TAG = "RebootNotification";

    private static int number = 0;

    @SuppressLint("NewApi")
    public static void notify(final Context context, final int n, boolean showSoftReboot) {
        number = n;

        final Resources res = context.getResources();

        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);

        final String ticker = res.getString(R.string.reboot_required);
        final String title = res.getString(R.string.reboot_required_title);
        final String text = res.getString(R.string.reboot_required_message);

        @SuppressWarnings("deprecation") final Notification.Builder builder = new Notification.Builder(context)
                .setDefaults(0)
                .setSmallIcon(android.R.drawable.ic_menu_rotate)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setLargeIcon(picture)
                .setTicker(ticker)
                .setNumber(number)
                .setWhen(0)
                .setContentIntent(
                        PendingIntent.getActivity(context, 0, new Intent(context, XTouchWizActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .setStyle(
                        new Notification.BigTextStyle().bigText(text).setBigContentTitle(title)
                                .setSummaryText(context.getString(R.string.pending_changes))).setAutoCancel(true);

        builder.addAction(new Action.Builder(Icon.createWithResource(context, android.R.drawable.ic_menu_rotate), res
                .getString(R.string.reboot), PendingIntent.getBroadcast(context, 1337, new Intent(
                "ma.wanam.xposed.action.REBOOT_DEVICE"), PendingIntent.FLAG_UPDATE_CURRENT)).build());

        if (showSoftReboot) {
            new Action.Builder(Icon.createWithResource(context, android.R.drawable.ic_menu_rotate),
                    res.getString(R.string.soft_reboot), PendingIntent.getBroadcast(context, 1337, new Intent(
                    "ma.wanam.xposed.action.SOFT_REBOOT_DEVICE"), PendingIntent.FLAG_UPDATE_CURRENT));
        }

        notify(context, builder.build());
    }

    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_TAG, 0, notification);
    }

    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_TAG, 0);
    }

    public static int getNumber() {
        return number;
    }
}
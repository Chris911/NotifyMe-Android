/*
 * Copyright (C) 2013 The Android Open Source Project
 *
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

package com.NotifyMe;

import android.app.Notification;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String service = extras.getString("service");
                if(service.equals("NotifyMe")) {
                    sendNotification(extras.getString("message"));
                } else {
                    sendNotificationWithExtras(extras);
                }

                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_gcm)
                        .setContentTitle("NotifyMe")
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setOnlyAlertOnce(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void sendNotificationWithExtras(Bundle extras) {
        String message = extras.getString("message");
        String service = extras.getString("service");
        String type = extras.getString("type");
        String title = "NotifyMe ["+ service +"]";

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent;
        if(service.equals("Reddit")) {
            Intent resultIntent = new Intent(Intent.ACTION_VIEW);
            String url = "http://reddit.com";
            if(type.equals("reddit-front-page")) {
                if(Integer.parseInt(extras.getString("count")) == 1) {
                    try {
                        JSONArray posts = new JSONArray(extras.getString("links"));
                        url = posts.getJSONObject(0).getString("url");
                        message = posts.getJSONObject(0).getString("title");
                    } catch (JSONException e) {
                        url = "http://reddit.com";
                    }
                } else {
                    url = "http://reddit.com";
                }
            } else if(type.equals("user-comment") || type.equals("user-submission")) {
                url = extras.getString("link");
            }
            resultIntent.setData(Uri.parse(url));
            contentIntent = PendingIntent.getActivity(this, 0, resultIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if(service.equals("weather")) {
            Intent resultIntent;
            Intent weatherIntent = getWeatherAppIntent();
            if(weatherIntent != null) {
                resultIntent = weatherIntent;
            } else {
                resultIntent = new Intent(Intent.ACTION_VIEW);
                String url = "http://weather.com/";
                resultIntent.setData(Uri.parse(url));
            }
            contentIntent = PendingIntent.getActivity(this, 0, resultIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if(service.equals("poly")) {
            Intent resultIntent = new Intent(Intent.ACTION_VIEW);
            String url = "https://www4.polymtl.ca/poly/poly.html";
            resultIntent.setData(Uri.parse(url));
            contentIntent = PendingIntent.getActivity(this, 0, resultIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if(service.equals("github")) {
            Intent resultIntent = new Intent(Intent.ACTION_VIEW);
            String url = extras.getString("link");
            resultIntent.setData(Uri.parse(url));
            contentIntent = PendingIntent.getActivity(this, 0, resultIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_gcm)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentText(message)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setOnlyAlertOnce(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private Intent getWeatherAppIntent() {
        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        String genieWidgetPackage = null;
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for(ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.toLowerCase().contains("weather")) {
                return pm.getLaunchIntentForPackage(packageInfo.packageName);
            } else if(packageInfo.packageName.toLowerCase().contains("geniewidget")) {
                genieWidgetPackage = packageInfo.packageName;
            }
        }
        // Didn't find a weather app - Try Google News and Weather
        // Will return null if not found.
        return pm.getLaunchIntentForPackage(genieWidgetPackage);
    }
}
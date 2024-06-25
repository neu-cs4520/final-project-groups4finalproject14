package com.example.haniph_n_finalproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class ReviewBroadcastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "review_notifications_channel";
    private static final String CHANNEL_NAME = "Review Notifications";
    private static final String CHANNEL_DESC = "Channel for review notifications";
    private static final String TAG = "ReviewReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(TAG, "onReceive called");

        if (intent != null) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(CHANNEL_DESC);

                notificationManager.createNotificationChannel(channel);

                //Log.d(TAG, "Notification channel created");
            }

            String movieId = intent.getStringExtra("movieId");
            String userEmail = intent.getStringExtra("userEmail");
            String reviewText = intent.getStringExtra("reviewText");
            double stars = intent.getDoubleExtra("stars", 0);

            int notificationId = movieId.concat(userEmail).hashCode();

            //Log.d(TAG, "Review received for movie: " + movieId + " from user: " + userEmail);

            // When this PendingIntent is triggered (i.e., when the user taps on the notification), the MovieDetailsActivity will be started, and it will have access to the movieId that was passed in the Intent
//        Intent notificationIntent = new Intent(context, MovieDetailsActivity.class);
//        notificationIntent.putExtra("id", movieId);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("New Review for Your Favorited Movie")
                    .setContentText("Someone reviewed one of your favorite movies! " + userEmail + " wrote '" + reviewText + "' and gave it" + stars + "/5 stars.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
                    //.setContentIntent(pendingIntent)
                    //.setAutoCancel(true);

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            notificationManager.notify(notificationId, builder.build());

        }
    }

}

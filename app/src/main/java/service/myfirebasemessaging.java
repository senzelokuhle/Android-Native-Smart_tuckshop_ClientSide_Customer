package service;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import betaversion.MainActivity;
import betaversion.R;

public class myfirebasemessaging extends FirebaseMessagingService{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        sendNotification(remoteMessage);

    }

    private void sendNotification(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification=remoteMessage.getNotification();
        Intent intent =new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationManager mNotific=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);


        CharSequence name="Detail you have a new order";
        String desc="Order notification";
        int imp=NotificationManager.IMPORTANCE_DEFAULT;
        final String ChannelID="my_channel_01";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {

            NotificationChannel mChannel = new NotificationChannel(ChannelID, name,
                    imp);

            mChannel.setName(name);
            mChannel.setDescription(desc);
            mChannel.setLightColor(Color.CYAN);
            mChannel.canShowBadge();
            mChannel.setShowBadge(true);
            mNotific.createNotificationChannel(mChannel);

        }

        final int ncode=101;

        String Body=" ";

        Notification n= new Notification.Builder(this,ChannelID)
                .setContentTitle(notification.getBody())
                .setContentText(Body)
                .setBadgeIconType(R.mipmap.ic_launcher)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true)
                .build();

        mNotific.notify(ncode, n);

    }
}

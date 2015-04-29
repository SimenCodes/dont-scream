package codes.simen.dontscream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

public class SilentService extends Service {
    private static final String logTag = "SilentService";
    private static final int NOTIFICATION_ID = 0;

    public SilentService() {
    }

    public static final String ACTION_REDUCE_VOLUME = "codes.simen.dontscream.ACTION_REDUCE_VOLUME";
    public static final String ACTION_NORMAL_VOLUME = "codes.simen.dontscream.ACTION_NORMAL_VOLUME";

    public static boolean isActive = false;
    AudioManager audioManager;
    NotificationManager notificationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        switch (intent.getAction()) {
            case ACTION_REDUCE_VOLUME:
                isActive = true;
                Log.d(logTag, "Requesting focus");
                audioManager.requestAudioFocus(audioFocusChangeListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
                notificationManager.notify(NOTIFICATION_ID, getNotification(getApplicationContext()));
                break;
            case ACTION_NORMAL_VOLUME:
                isActive = false;
                Log.d(logTag, "Abandoning focus");
                audioManager.abandonAudioFocus(audioFocusChangeListener);
                notificationManager.cancel(NOTIFICATION_ID);
                stopSelf();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private Notification getNotification(Context context) {
        return new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_ad)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_text))
                .setContentIntent(getPendingServiceIntent(ACTION_NORMAL_VOLUME))
                .setPriority(Notification.PRIORITY_MIN)
                .build();
    }

    private PendingIntent getPendingServiceIntent(String action) {
        return PendingIntent.getService(getApplicationContext(), 0,
                new Intent(getApplicationContext(), SilentService.class).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(logTag, "Focus changed: " + focusChange);
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not possible");
    }
}

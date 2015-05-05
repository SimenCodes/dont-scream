package codes.simen.calm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class SilentService extends Service {
    private static final String logTag = "SilentService";
    private static final int NOTIFICATION_ID = 0;

    public SilentService() {
    }

    public static final String ACTION_REDUCE_VOLUME = "codes.simen.calm.ACTION_REDUCE_VOLUME";
    public static final String ACTION_NORMAL_VOLUME = "codes.simen.calm.ACTION_NORMAL_VOLUME";

    public static boolean isActive = false;
    AudioManager audioManager;
    NotificationManager notificationManager;

    boolean isAlternativeMuting = false;
    int savedVolume = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        isAlternativeMuting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(MainActivity.SETTING_KEY_ALTERNATIVE_MUTING, false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_REDUCE_VOLUME:
                isActive = true;

                if (isAlternativeMuting) requestAudioFocus();
                else                     requestLowerSystemVolume();

                final Notification notification = getNotification(getApplicationContext());
                notificationManager.notify(NOTIFICATION_ID, notification);
                startForeground(NOTIFICATION_ID, notification);

                break;
            case ACTION_NORMAL_VOLUME:
                isActive = false;

                if (isAlternativeMuting) abandonAudioFocus();
                else                     abandonLowerSystemVolume();

                notificationManager.cancel(NOTIFICATION_ID);
                stopSelf();

                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private boolean requestAudioFocus() {
        Log.d(logTag, "AudioFocus on");
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.requestAudioFocus(audioFocusChangeListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
    }

    private void abandonAudioFocus() {
        Log.d(logTag, "AudioFocus off");
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }

    private void requestLowerSystemVolume() {
        savedVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (savedVolume > 1)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, AudioManager.FLAG_SHOW_UI);
        else {
            // Fallback
            requestAudioFocus();
            isAlternativeMuting = true;
        }
        Log.d(logTag, "SystemVolume low, saved " + savedVolume);
    }

    private void abandonLowerSystemVolume() {
        if (savedVolume > 1)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, savedVolume, AudioManager.FLAG_SHOW_UI);
        Log.d(logTag, "Setting system volume to normal, " + savedVolume);
    }

    private Notification getNotification(Context context) {
        return new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_ad)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_text))
                .setContentIntent(getPendingServiceIntent(ACTION_NORMAL_VOLUME))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
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
            notificationManager.notify(NOTIFICATION_ID, new Notification.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_stat_ad)
                    .setContentTitle(getString(R.string.notif_title))
                    .setContentText("Focus change: " + focusChange)
                    .setContentIntent(getPendingServiceIntent(ACTION_NORMAL_VOLUME))
                    .setOngoing(false)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build());
        }
    };

    @Override
    public void onDestroy() {
        if (isAlternativeMuting)
            abandonLowerSystemVolume();
        else
            abandonAudioFocus();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not possible");
    }
}

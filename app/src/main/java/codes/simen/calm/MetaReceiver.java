package codes.simen.calm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MetaReceiver extends BroadcastReceiver {
    private static final String logTag = "MetaReceiver";

    public MetaReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        Log.d(logTag, action);

        try {
            // Debug code for finding fields when adding app support
            /**final Bundle extras = intent.getExtras();
            for (String str : extras.keySet()) {
                final Object o = extras.get(str);
                Log.v(logTag + " " + str, o != null ? o.toString() : "null");
            }*/

            boolean playing = intent.getBooleanExtra("playing", true);

            if (!playing) {
                if (SilentService.isActive) {
                    context.startService(
                            new Intent(context, SilentService.class)
                                    .setAction(SilentService.ACTION_NORMAL_VOLUME)
                    );
                }
                return;
            }

            long id = -1;
            if (action.equals("com.spotify.music.metadatachanged")) {

                // In Spotify, the ID is a String
                String idStr = intent.getStringExtra("id");

                boolean isAd;

                if (idStr != null) {
                    // The ads have ids containing the word ad
                    isAd = idStr.startsWith("spotify:ad:");
                    Log.d(logTag, isAd ? "ad id prefix found!" : "no ad prefix");
                    // In case they change their naming scheme, we'll revert to using the track length
                    if (!isAd && !idStr.startsWith("spotify:track:")) {
                        Log.d(logTag, "no track prefix found either");
                        isAd = isAdAlternative(intent);
                    }
                } else {
                    // No ID? Let's try the length
                    isAd = isAdAlternative(intent);
                }


                if (isAd) {
                    context.startService(
                            new Intent(context, SilentService.class)
                                    .setAction(SilentService.ACTION_REDUCE_VOLUME)
                    );
                } else if (SilentService.isActive) {
                    context.startService(
                            new Intent(context, SilentService.class)
                                    .setAction(SilentService.ACTION_NORMAL_VOLUME)
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAdAlternative(Intent intent) {
        int length = intent.getIntExtra("length", -1);
        if (length < 61) {
            Log.d(logTag, "Short track! Probably an ad");
            return true;
        }
        Log.d(logTag, "Long track, probably not an ad");
        return false;
    }
}

package codes.simen.dontscream;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A simple update checker
 */
public class UpdateChecker extends IntentService {
    private static final String logTag = "UpdateChecker";
    public static final String ACTION_CHECK_UPDATE = "codes.simen.readtome.app.action.checkForUpdate";

    public UpdateChecker() {
        super("UpdateChecker");
    }

    public static void startUpdateCheck(Context context) {
        context.startService(new Intent(context, UpdateChecker.class).setAction(ACTION_CHECK_UPDATE));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_UPDATE.equals(action)) {
                handleCheck();
            }
        }
    }

    private void handleCheck() {
        String url = "http://app.simen.codes/update/android.php?app=" +
                BuildConfig.APPLICATION_ID.replace('.', '_') +
                "&ver=" +
                BuildConfig.VERSION_CODE +
                "&api=" +
                Build.VERSION.SDK_INT;

        Log.d(logTag, url);
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //ONLINE
            new DownloadWebpageTask().execute(url);
        }
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to check for updates";
            }
        }

        private String downloadUrl(String inputurl) throws IOException {
            InputStream is = null;

            try {
                URL url = new URL(inputurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();
                is = conn.getInputStream();
                int len = conn.getContentLength();
                return readIt(is, len != -1 ? len : 1024);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String readIt(InputStream stream, int len) throws IOException {
            Reader reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(logTag, result);
            try {
                JSONArray jsonArray = new JSONArray(result);
                int newV = jsonArray.getInt(0);
                if (newV > BuildConfig.VERSION_CODE) {
                    Context context = getApplicationContext();
                    String url = jsonArray.getString(1);

                    Intent intentToMain = new Intent();
                    intentToMain.setAction(Intent.ACTION_VIEW);
                    intentToMain.setData(Uri.parse(url));
                    intentToMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentToMain, 0);

                    Notification notification = new Notification.Builder(context)
                            .setTicker("An update is available")
                            .setWhen(System.currentTimeMillis())
                            .setContentTitle("Don\t Scream is outdated")
                            .setContentText("New version: " + String.valueOf(newV))
                            .setSmallIcon(R.drawable.ic_stat_ad)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setPriority(Notification.PRIORITY_LOW)
                            .build();

                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, notification);
                }
            } catch (JSONException e) {
                Log.w(logTag, "Update check failed: " + e.getMessage());
            }
        }
    }
}

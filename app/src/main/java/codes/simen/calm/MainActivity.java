package codes.simen.calm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class MainActivity extends Activity {

    public static final String SETTING_KEY_ALTERNATIVE_MUTING = "alternative_muting";
    SharedPreferences preferences;
    CheckBox checkBoxAltMuting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        checkBoxAltMuting = (CheckBox) findViewById(R.id.checkBox);
        checkBoxAltMuting.setChecked(preferences.getBoolean(SETTING_KEY_ALTERNATIVE_MUTING, false));
        checkBoxAltMuting.setOnCheckedChangeListener(checkedChangeListener);

        // Load an advertisement
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("3B0B160CFA9D74704ED0120194F3C8D0")
                .addKeyword("music")
                .build();
        adView.loadAd(adRequest);
    }

    public void imageViewClick(View view) {
        // For the idiots out there
        Toast.makeText(getApplicationContext(), getString(R.string.error_screenshot), Toast.LENGTH_LONG).show();

        startService(new Intent(this, SilentService.class).setAction(SilentService.ACTION_REDUCE_VOLUME));
    }

    public void getHelp(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://simen.codes/stuff/spotify-lower-ad-volume#help")));
    }

    CheckBox.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            preferences.edit().putBoolean(SETTING_KEY_ALTERNATIVE_MUTING, isChecked).apply();
        }
    };
}

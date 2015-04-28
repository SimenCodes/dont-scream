package codes.simen.dontscream;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UpdateChecker.startUpdateCheck(getApplicationContext());
    }

    public void imageViewClick(View view) {
        // For the idiots out there
        Toast.makeText(getApplicationContext(), getString(R.string.error_screenshot), Toast.LENGTH_LONG).show();
    }

    public void getHelp(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://simen.codes/stuff/spotify-lower-ad-volume#help")));
    }
}

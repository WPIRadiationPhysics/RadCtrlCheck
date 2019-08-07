package net.radiohacktive.radctrlcheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    /** Button push events */
    public void eventOpenDroidScanLite(View arg0) {

        // Open Droid Scan Lite Activity
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.trans_code.android.droidscanlite");
        startActivityForResult(intent, 100);

    }
    //
    public void eventImportFullArea(View arg0) {

        // Open Image Analysis Activity
        Intent intent = new Intent(this, ImageAnalysis.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

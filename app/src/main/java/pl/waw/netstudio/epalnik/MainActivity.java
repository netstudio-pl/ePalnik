package pl.waw.netstudio.epalnik;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static final String DEVICE_NAME = "ePalnik"; //nazwa urządzenia z którym łączy się aplikacja
    public boolean doubleBackToExit = false;
    public Button btnStart, btnStop, btnTimer;
    public TextView tvTemp, tvStatus;
    public SeekBar sbPlomien;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnTimer = (Button) findViewById(R.id.btnTimer);
        tvTemp = (TextView) findViewById(R.id.tvTemp);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        sbPlomien = (SeekBar) findViewById(R.id.sbPlomien);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}

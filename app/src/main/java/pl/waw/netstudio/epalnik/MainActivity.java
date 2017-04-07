package pl.waw.netstudio.epalnik;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String DEVICE_NAME = "ePalnik"; //nazwa urządzenia z którym łączy się aplikacja
    public boolean doubleBackToExit = false;
    public Button btnStart, btnStop, btnTimer;
    public TextView tvTemp, tvStatus;
    public SeekBar sbPlomien;

    public boolean isConnected = false;
    public BluetoothAdapter bluetoothAdapter;
    public BluetoothSocket socket = null;
    public BufferedReader in = null;
    public OutputStreamWriter out = null;

    public Timer timer;
    public TimerTask timerTask;
    public Handler handler = new Handler();

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
        switch (item.getItemId()) {
            case R.id.menu_polacz:
                deviceConnect();
                return true;
            case R.id.menu_rozlacz:
                deviceDisonnect();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deviceConnect() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<String> devices = new ArrayList<>();
        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getName().equals(DEVICE_NAME)) {
                try {
                    UUID uuid = bt.getUuids()[0].getUuid();
                    socket = bt.createRfcommSocketToServiceRecord(uuid);
                    if (socket != null) {
                        socket.connect();
                        //startTimer(); //rozpocznij odczyt temperatury z urządzenia
                        isConnected = true;
                        tvStatus.setTextColor(Color.GRAY);
                        tvStatus.setText("Połączony");
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new OutputStreamWriter(socket.getOutputStream());
                    }
                } catch (IOException e) {
                    Log.e("#log#", e.toString());
                }
            } else {
                Toast.makeText(this, "Moduł palnika nie jest sparowany z telefonem", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void deviceDisonnect() {
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                    timer.cancel(); //zatrzymaj odczyt temperatury z urządzenia
                    isConnected = false;
                    tvStatus.setTextColor(Color.GRAY);
                    tvStatus.setText("Rozłączony");
                    tvTemp.setText("");
                } catch (IOException e) {
                    Log.e("#log#", e.toString());
                }
            }
        }
    }
}

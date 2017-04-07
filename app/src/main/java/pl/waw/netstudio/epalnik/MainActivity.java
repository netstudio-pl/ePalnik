package pl.waw.netstudio.epalnik;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.AsyncTask;
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
        sbPlomien.setOnSeekBarChangeListener(new sbPlomienListener());

        //włącz adapter Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.enable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceDisonnect(); //rozłącz z urządzeniem
        bluetoothAdapter.disable(); //wyłacz adapter Bluetooth
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
                        startTimer(); //rozpocznij odczyt temperatury z urządzenia
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

    public void btnStartClick(View view) {
        if (isConnected == true) {
            tvStatus.setTextColor(Color.RED);
            tvStatus.setText("ZAPALONY");
            try {
                out.write("*");
                out.flush();
            } catch (IOException e) {
                Log.e("#log#", e.toString());
            }
        }
    }

    public void btnStopClick(View view) {
        if (isConnected == true) {
            tvStatus.setTextColor(Color.BLUE);
            tvStatus.setText("ZGASZONY");
            try {
                out.write("#");
                out.flush();
            } catch (IOException e) {
                Log.e("#log#", e.toString());
            }
        }
    }

    public void btnTimerClick(View view) {
        if (isConnected == true) {
            //opóźnione wyłączenie palnika
            Toast.makeText(this, "Urządzenie zostanie wyłączone za 30 minut", Toast.LENGTH_LONG).show();
        }
    }

    private class sbPlomienListener implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isConnected == true) {
                try {
                    out.write("$" + String.valueOf(progress));
                    out.flush();
                } catch (IOException e) {
                    Log.e("#log#", e.toString());
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    public void startTimer() { //odczyt temperatury z urządzenia wywoływany co 10 sek
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        AsyncTask<Void, Integer, String> asyncTask = new AsyncTask<Void, Integer, String>() {
                            @Override
                            protected String doInBackground(Void... params) {
                                String temperatura;
                                try {
                                    if (in.ready()) {
                                        while ((temperatura = in.readLine()) != null) {
                                            return temperatura;
                                        }
                                    }
                                } catch (IOException e) {
                                    Log.e("#log#", e.toString());
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(String s) {
                                super.onPostExecute(s);
                                if (s != null) {
                                    if (s.startsWith("%")) tvTemp.setText(s.substring(1) + " °C");
                                }
                            }
                        };
                        asyncTask.execute();
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 10000);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExit) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExit = true;
        Toast.makeText(this, "Naciśnij ponownie, aby zamknąć program", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExit = false;
            }
        }, 2000);
    }
}

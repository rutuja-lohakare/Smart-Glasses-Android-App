package com.example.smartglasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends Activity {

    private static MainActivity instance;

    public String btdata;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static String EXTRA_ADDRESS = "device_address";
    public BluetoothAdapter BA;
    public Set<BluetoothDevice> pairedDevices;
    TextView ble_s, paired_d;
    Switch ble;
    ListView lv;
    ArrayAdapter adpt = null;
    public static BluetoothSocket btSocket = null;
    public static boolean isBtConnected = false;

    public AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            ((TextView) v).setText(info.substring(0, info.length() - 18) + " - Connecting\n" + address);
//            msg("connecting");
            if (ConnectBT(v, address)) {
                ((TextView) v).setText(info.substring(0, info.length() - 18) + " - Connected\n" + address);
            } else {
                ((TextView) v).setText(info);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BA = BluetoothAdapter.getDefaultAdapter();

        ble_s = (TextView) findViewById(R.id.ble_status);
        paired_d = (TextView) findViewById(R.id.paired_devices);
        Button list_dev = (Button) findViewById(R.id.list_dev);
        ble = (Switch) findViewById(R.id.BLE);
        BA = BluetoothAdapter.getDefaultAdapter();

        Button done = (Button) findViewById(R.id.Done);
        done.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                btdata = '$' + currentDate + ',' + currentTime + '$';
                sendBTdata(getApplicationContext());
                Intent myIntent = new Intent(view.getContext(), SecondActivity.class);
                startActivityForResult(myIntent, 0);
//                msg("done",Toast.LENGTH_SHORT);
            }

        });
        if (BA.isEnabled()) {
            ble.setChecked(true);
            ble_s.setText("On");
            isBtConnected = true;
        } else {
            ble.setChecked(false);
            ble_s.setText("Off");
            isBtConnected = false;
        }

        lv = (ListView) findViewById(R.id.listDevices);
        lv.setOnItemClickListener(myListClickListener);

        ble.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    turnedOnBT(true);
                    ble_s.setText("On");
                    isBtConnected = true;
                } else {
                    turnedOnBT(false);
                    ble_s.setText("Off");
                    isBtConnected = false;
                }
            }
        });

        list_dev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBtConnected == true) {
                    paired_d.setVisibility(View.VISIBLE);
                    adpt = list(view);
                    lv.setAdapter(adpt);
                    lv.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
                } else {
                    msg(getApplicationContext(),"Bluetooth is off", Toast.LENGTH_SHORT);
                }
            }
        });

        if (adpt == null) {

        } else {
            paired_d.setVisibility(View.VISIBLE);
            lv.setAdapter(adpt);
            lv.setOnItemClickListener(myListClickListener);
        }
    }

    public void turnedOnBT(boolean b) {
        if (b) {
            if (!BA.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
            } else {
                msg(getApplicationContext(),"Already on", Toast.LENGTH_SHORT);
            }
        } else {
            BA.disable();
        }
    }

    public ArrayAdapter list(View v) {
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();

        for (BluetoothDevice bt : pairedDevices) list.add(bt.getName() + '\n' + bt.getAddress());

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        return adapter;
    }

    /**
     * A placeholder fragment containing a simple view.
     */

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    public boolean ConnectBT(View v, String address) {
        try {
            if (btSocket == null) {
                BA = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice dispositivo = BA.getRemoteDevice(address);//connects to the device's address and checks if it's available
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();//start connection
                isBtConnected = true;
                msg(getApplicationContext(),"Connected", Toast.LENGTH_SHORT);
                return true;
            }
        } catch (IOException e) {
            isBtConnected = false;
            msg(getApplicationContext(), "Failed to Connect", Toast.LENGTH_LONG);
            return false;
        }
        return false;
    }

    public void sendBTdata(Context c)
    {
        try {
            if (isBtConnected) {
                btdata = btdata + '\n';
                btSocket.getOutputStream().write(btdata.getBytes());
                msg(c , btdata, Toast.LENGTH_SHORT);
            }
            else
            {
                msg(c , "error", Toast.LENGTH_SHORT);
            }
        } catch (Exception e) {
            msg(c , e.toString(), Toast.LENGTH_SHORT);
        }
    }

    public static MainActivity getInstance() {
        return instance;
    }

    private void msg(Context c,String s, int d) {
        Toast.makeText(c, s, d).show();
    }
}
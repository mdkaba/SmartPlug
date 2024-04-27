package com.example.team7_390_w2024;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private static final long SCAN_PERIOD = 10000;
    private BLEHelper bleHelper;
    private DatabaseHelper dbHelper;
    private Handler handler;

    private SharedPreferencesHelper sph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        bleHelper = new BLEHelper(getApplicationContext(), this);
        handler = new Handler(Looper.getMainLooper());
        dbHelper = new DatabaseHelper(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_scan);
        if(!bleHelper.isLeEnabled()) {
            Toast.makeText(this, R.string.please_enable_ble, Toast.LENGTH_SHORT).show();
            finish();
        }
        setupUI();
        startScan();
    }

    private void setupUI() {
        Toolbar scanToolbar = findViewById(R.id.scanToolbar);
        setSupportActionBar(scanToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.scan_for_device);
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    private void startScan() {
        setupScanningUI();
        try {
            handler.postDelayed(() -> {
                bleHelper.stopScan();
                setupDisplayUI();
            }, SCAN_PERIOD);
            bleHelper.startScan();
        } catch(Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void setupDisplayUI() {
        TextView scanningTextView = findViewById(R.id.scanningTextView);
        scanningTextView.setVisibility(View.INVISIBLE);

        ListView deviceListView = findViewById(R.id.unpairedDeviceListView);
        deviceListView.setOnItemClickListener((parent, view, position, id) -> pairNthDevice(position));
        List<String> macAddressList = bleHelper.getScannedDevices();
        if(macAddressList.isEmpty())
            Toast.makeText(this, R.string.no_devices_found, Toast.LENGTH_LONG).show();
        else {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, macAddressList);
            deviceListView.setAdapter(arrayAdapter);
        }
    }

    private void setupScanningUI() {
        TextView scanningTextView = findViewById(R.id.scanningTextView);
        scanningTextView.setVisibility(View.VISIBLE);
        ListView deviceListView = findViewById(R.id.unpairedDeviceListView);
        deviceListView.setAdapter(null);
    }

    private void pairNthDevice(int position) {
        String address = bleHelper.getNthDeviceAddress(position);
        bleHelper.bondToDeviceAddress(address);
        bleHelper.connect(address);
        DeviceNameFragment deviceNameFragment = new DeviceNameFragment(address, this, getApplicationContext());
        deviceNameFragment.show(getSupportFragmentManager(), "DeviceDialog");
    }

    //Called after DeviceNameFragment ends
    public void afterFragmentComplete()
    {
        SharedPreferencesHelper sph = new SharedPreferencesHelper(getApplicationContext());
        String name = sph.getDeviceName();
        String ssid = sph.getDeviceSsid();
        String password = sph.getDevicePassword();
        String macAddress = sph.getDeviceMacAddress();
        sph.resetDeviceStrings();
        Device device = new Device(macAddress, name);
        //Turns off relay when device is added
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.setCommand(false);
        dbHelper.insertDevice(device);
        try {
            bleHelper.sendWiFiParams(ssid, password);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
            return true;
        }
        if(id == R.id.refresh) {
            if(!bleHelper.isScanning())
                startScan();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
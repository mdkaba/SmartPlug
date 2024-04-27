package com.example.team7_390_w2024;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class DevicesActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    List<Device> deviceList;
    BLEHelper bleHelper;

    //Dummy Data
    private void addDummyDevice() {

        Device device = new Device("MAC Address", "Name");
//        dbHelper.removeDevice(device);
        int id = dbHelper.insertDevice(device);
        device.setId(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        dbHelper = new DatabaseHelper(getApplicationContext());
        deviceList = new ArrayList<>();
        bleHelper = new BLEHelper(getApplicationContext(), this);
//        addDummyDevice();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_devices);
        getDevices();
        setupUI();
    }

    private void getDevices() {
        deviceList = dbHelper.getAllDevices();
    }

    private void setupUI() {
        Toolbar devicesToolbar = findViewById(R.id.devicesToolbar);
        setSupportActionBar(devicesToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.devices);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button addDeviceButton = findViewById(R.id.addNewDeviceButton);
        addDeviceButton.setOnClickListener(v -> addDevice());

        ListView deviceListView = findViewById(R.id.deviceListView);
        deviceListView.setOnItemClickListener((parent, view, position, id) -> devicePressed(position));
        ArrayList<String> arrayList = getDeviceDisplayArray();
        if(arrayList != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
            deviceListView.setAdapter(arrayAdapter);
        }
    }

    private ArrayList<String> getDeviceDisplayArray() {
        if(deviceList.isEmpty())
            return null;
        ArrayList<String> arrayList = new ArrayList<>();
        for(Device device : deviceList) {
            arrayList.add(device.getName() + ", " + device.getBleMacAddress());
        }
        return arrayList;
    }

    private void devicePressed(int position) {
        Device device = dbHelper.getNthDevice(position);
        int id = device.getId();
        Intent intent = new Intent(getApplicationContext(), DataActivity.class);
        intent.putExtra("DeviceId", id);
        startActivity(intent);
    }

    private void addDevice() {
        Intent addDeviceIntent = new Intent(getApplicationContext(), ScanActivity.class);
        startActivity(addDeviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.goToSettings) {
            Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        else if(id == android.R.id.home) {
            finish();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

}
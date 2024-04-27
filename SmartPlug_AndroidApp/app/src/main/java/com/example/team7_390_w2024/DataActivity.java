package com.example.team7_390_w2024;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DataActivity extends AppCompatActivity {

    protected Device device;
    protected DatabaseHelper dbHelper;
    protected TextView connectionStatusTextView;
    protected ListView dataListView;
    protected int settings;
    protected int[] clickablePositions;
    protected FirebaseHelper firebaseHelper;
    protected DatabaseReference firebaseRef;

    protected ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        dbHelper = new DatabaseHelper(getApplicationContext());
        Intent intent = getIntent();
        int id = intent.getIntExtra("DeviceId", -1);
        device = dbHelper.getDevice(id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_data);
        firebaseHelper = new FirebaseHelper();
        setupUI();
    }

    private void setupUI() {
        Toolbar dataToolbar = findViewById(R.id.dataToolbar);
        setSupportActionBar(dataToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.plug_data);
        actionBar.setDisplayHomeAsUpEnabled(true);
        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        ToggleButton plugToggleButton = findViewById(R.id.plugToggleButton);
        boolean plug_status = dbHelper.getDeviceStatus(device.getId());
        plugToggleButton.setChecked(plug_status);
        plugToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> togglePlug(plugToggleButton));
        SharedPreferencesHelper sph = new SharedPreferencesHelper(getApplicationContext());
        settings = sph.getViewSettings();
        setClickablePositions();
        dataListView = findViewById(R.id.dataListView);
        dataListView.setOnItemClickListener((parent, view, position, id) -> goToLogIfClickable(position));
        setupListViewFromSettings();

    }

    private void setupListViewFromSettings() {
        ArrayList<String> arrayList = new ArrayList<>();
//        if((settings & SharedPreferencesHelper.PLUG_STATUS_ENABLED) != 0) {
//            arrayList.add("Plug Status: ");
//        }
        if((settings & SharedPreferencesHelper.PRESENT_CURRENT_DRAW_ENABLED) != 0) {
            arrayList.add("Present Current Draw: ");
        }
        if((settings & SharedPreferencesHelper.PRESENT_POWER_DRAW_ENABLED) != 0) {
            arrayList.add("Present Power Draw: ");
        }
        if((settings & SharedPreferencesHelper.CURRENT_DATA_LOG_ENABLED) != 0) {
            arrayList.add("Current Data Log");
        }
        if((settings & SharedPreferencesHelper.POWER_DATA_LOG_ENABLED) != 0) {
            arrayList.add("Power Data Log");
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        dataListView.setAdapter(arrayAdapter);

        //Firebase listener to update data run time
        firebaseRef = FirebaseDatabase.getInstance().getReference("User-1");

        valueEventListener = new ValueEventListener() {
            @Override//Tested and works, when value is changed in firebase database this will trigger
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Splitting queue string
                String dataString= dataSnapshot.child("Readings & Time Stamps").getValue(String.class);
                // Split the string on the " |" delimiter to get the separate values
                String[] entries = dataString.split(" \\|");
                for (String entry : entries) {
                    if (!entry.trim().isEmpty()) {
                        String[] parts = entry.split(" - ", 2);
                        if (parts.length == 2) {
                            String currentValue = parts[0].trim();
                            Double powerValue = Double.parseDouble(currentValue) * 120;
                            String timestamp = parts[1].trim();
                            for(int i = 0; i < arrayList.size(); i++){
                                if(arrayList.get(i).contains("Present Current Draw"))
                                    arrayList.set(i, "Present Current Draw:  " + currentValue + " A");
                            }
                            for(int i = 0; i < arrayList.size(); i++){
                                if(arrayList.get(i).contains("Present Power Draw"))
                                    arrayList.set(i, "Present Power Draw:  " + powerValue + " W");
                            }
                            arrayAdapter.notifyDataSetChanged();
                            System.out.println("Current Value: " + currentValue);
                            System.out.println("Timestamp: " + timestamp);
                            //Populate the local database whenever a current measurement is read from the firebase.
                            CurrentTimestamp currentTimestamp = new CurrentTimestamp(Double.parseDouble(currentValue),timestamp);
                            dbHelper.addTimestamp(device,currentTimestamp);
                        }
                        System.out.println("Entry: " + entry.trim());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Failed to read data: " , error.toException());
            }
        };
        firebaseRef.addValueEventListener(valueEventListener);

    }

    private void setClickablePositions() {
        clickablePositions = new int[2];
        int currentPos = 16;
        int powerPos = 16;
//        if((settings & SharedPreferencesHelper.PLUG_STATUS_ENABLED) != 0) {
//            currentPos++;
//            powerPos++;
//        }
        if((settings & SharedPreferencesHelper.PRESENT_CURRENT_DRAW_ENABLED) != 0) {
            currentPos++;
            powerPos++;
        }
        if((settings & SharedPreferencesHelper.PRESENT_POWER_DRAW_ENABLED) != 0) {
            currentPos++;
            powerPos++;
        }
        if((settings & SharedPreferencesHelper.CURRENT_DATA_LOG_ENABLED) != 0) {
            if(currentPos == 16)
                currentPos = 0;
            else
                currentPos -= 16;
            clickablePositions[0] = currentPos;
            powerPos++;
        }
        if((settings & SharedPreferencesHelper.POWER_DATA_LOG_ENABLED) != 0) {
            if(powerPos == 16)
                powerPos = 0;
            else
                powerPos -= 16;
            clickablePositions[1] = powerPos;
        }
    }

    private void goToLogIfClickable(int position) {
        if(position == clickablePositions[0]) {

            Intent intent = new Intent(getApplicationContext(), LogActivity.class);
            intent.putExtra("LogType","Current");
            intent.putExtra("DeviceId",device.getId());
            startActivity(intent);

        }
        if(position == clickablePositions[1]) {
            Intent intent = new Intent(getApplicationContext(), LogActivity.class);
            intent.putExtra("LogType","Power");
            intent.putExtra("DeviceId",device.getId());
            startActivity(intent);
        }
    }

    private void togglePlug(ToggleButton plugToggleButton) {
        if(plugToggleButton.getText() != plugToggleButton.getTextOn()){
            firebaseHelper.setCommand(true);
            dbHelper.setDeviceStatus(device.getId(), true);
        }
        else {
            firebaseHelper.setCommand(false);
            dbHelper.setDeviceStatus(device.getId(), false);
        }

    }

    private void update() {
        ListAdapter listAdapter = dataListView.getAdapter();
        //TODO: Modify Strings in listAdapter with updated data;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.data_menu, menu);
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
        else if(id == R.id.action_remove) {
            deleteDevice();
            finish();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    private void deleteDevice() {
        firebaseRef.removeEventListener(valueEventListener);
        dbHelper.removeDevice(device);
        firebaseHelper.setResetCommand(true);
    }
}
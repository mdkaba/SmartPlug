package com.example.team7_390_w2024;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

public class SettingsActivity extends AppCompatActivity {

    protected CheckBox presentCurrentDrawCheckbox, presentPowerDrawCheckbox, powerDataLogCheckbox, currentDataLogCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_settings);
        setupUI();
    }

    private void setupUI() {
        Toolbar settingsToolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(settingsToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.settings);

        Button setDataViewButton = findViewById(R.id.setDataViewSettingsButton);
        setDataViewButton.setOnClickListener(v -> setDataViewSettings());
        presentCurrentDrawCheckbox = findViewById(R.id.presentCurrentDrawButton);
        presentPowerDrawCheckbox = findViewById(R.id.presentPowerDrawButton);
        powerDataLogCheckbox = findViewById(R.id.powerDataLogButton);
        currentDataLogCheckbox = findViewById(R.id.currentDataLogButton);
    }

    private void setDataViewSettings() {
        int settings = 0;
        if(presentCurrentDrawCheckbox.isChecked())
            settings += SharedPreferencesHelper.PRESENT_CURRENT_DRAW_ENABLED;
        if(presentPowerDrawCheckbox.isChecked())
            settings += SharedPreferencesHelper.PRESENT_POWER_DRAW_ENABLED;
//        if(plugStatusCheckbox.isChecked())
//            settings += SharedPreferencesHelper.PLUG_STATUS_ENABLED;
        if(powerDataLogCheckbox.isChecked())
            settings += SharedPreferencesHelper.POWER_DATA_LOG_ENABLED;
        if(currentDataLogCheckbox.isChecked())
            settings += SharedPreferencesHelper.CURRENT_DATA_LOG_ENABLED;
        SharedPreferencesHelper sph = new SharedPreferencesHelper(getApplicationContext());
        sph.setViewSettings(settings);

        finish(); // return to past activity
    }


}
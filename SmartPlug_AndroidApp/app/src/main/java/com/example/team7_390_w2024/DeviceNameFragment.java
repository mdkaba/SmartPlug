package com.example.team7_390_w2024;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DeviceNameFragment extends DialogFragment {
    protected EditText nameEditText, ssidEditText, passwordEditText;
    String address;
    Activity parentActivity;
    Context context;
    DeviceNameFragment(String address, Activity parentActivity, Context context) {
        this.address = address;
        this.parentActivity = parentActivity;
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_name, container, false);
        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> save());
        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> cancel());
        TextView title = view.findViewById(R.id.addDeviceTitleText);
        title.setText(getString(R.string.mac_address) + address);
        nameEditText = view.findViewById(R.id.nameEditText);
        ssidEditText = view.findViewById(R.id.ssidEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        return view;
    }

    private void cancel() {
        parentActivity.finish();
        dismiss();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private void save() {
        String name = nameEditText.getText().toString();
        String ssid = ssidEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        SharedPreferencesHelper sph = new SharedPreferencesHelper(context);
        sph.setDeviceName(name);
        sph.setDeviceSsid(ssid);
        sph.setDevicePassword(password);
        sph.setDeviceMacAddress(address);
        //Sends the wifi info to HW
        ((ScanActivity) getActivity()).afterFragmentComplete();
        cancel();
    }


}
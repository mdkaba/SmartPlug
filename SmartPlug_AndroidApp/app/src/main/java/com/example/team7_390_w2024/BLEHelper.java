package com.example.team7_390_w2024;

import android.Manifest;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


//See https://github.com/android/connectivity-samples/tree/main/BluetoothLeGatt/Application/src/main/java/com/example/android/bluetoothlegatt
// for implementation details.
public class BLEHelper extends Service {
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    private final static String BLE_ADVERTISING_NAME = "Team7_390_SmartPlug";
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothLeScanner bluetoothLeScanner;
    private final ScanCallback leScanCallback;
    private BluetoothGatt bluetoothGatt;

    private ArrayList<BluetoothGattCharacteristic> characteristicList;

    private BluetoothDevice bluetoothDevice;
    private final List<BluetoothDevice> bondedDevices;

    private final String bluetoothAddr = "";

    Context context;
    Activity activity;

    private final HashMap<String, BluetoothDevice> scannedDevices;

    private boolean scanning;

    BLEHelper(Context context, Activity activity) {
        bluetoothManager = context.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        this.context = context;
        this.activity = activity;
        scannedDevices = new HashMap<>();
        bondedDevices = new ArrayList<>();
        characteristicList = new ArrayList<>();
        leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                if (!scannedDevices.containsKey(device.getAddress()))
                    scannedDevices.put(device.getAddress(), device);
            }
        };

    }


    public boolean connect(final String address) {
        if (bluetoothAdapter != null || address != null) {
            //Check for the most recent device to auto-reconnect
            if (address.equals(bluetoothAddr) && bluetoothGatt != null) {
                //Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                //Permission moment
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
                return bluetoothGatt.connect();
            }
        }
        return false;
    }

    public boolean isLeEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    public void startScan() {

        scannedDevices.clear();

        //Check permissions before scanning
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_PERMISSION);
            return;
        }

        //Scan Filter implementation - Only displays our device
        List<ScanFilter> scanFilterList = new ArrayList<>();
        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        ScanFilter scanFilter = new ScanFilter.Builder().setDeviceName(BLE_ADVERTISING_NAME).build();
        scanFilterList.add(scanFilter);

        if (!scanning) {
            scanning = true;
            bluetoothLeScanner.startScan(scanFilterList, scanSettings, leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    public void stopScan() {

        //Check permissions before scanning
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_PERMISSION);
            return;
        }

        if (scanning) {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    public List<String> getScannedDevices() {
        return new ArrayList<>(scannedDevices.keySet());
    }

    public boolean isScanning() {
        return scanning;
    }

    public String getNthDeviceAddress(int position) {
        List<String> macAddressList = new ArrayList<>(scannedDevices.keySet());
        return macAddressList.get(position);
    }

    public void bondToDeviceAddress(String address) {
        BluetoothDevice device = scannedDevices.get(address);
        //Assigns bluetoothGatt VV
        BluetoothLePair(address);
        bondedDevices.add(device);
        bluetoothDevice = device;
        connect(address);
    }

    public void BluetoothLePair(String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_PERMISSION);
            return;
        }
        bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);
    }

    @SuppressWarnings({"MissingPermission"})
    public void sendWiFiParams(String wifiSSID, String wifiPass) {
        //DO NOT USE STRING
        //String doesn't like unprintable characters in some implementations
        //byte[] or ArrayList<Byte> only
        BluetoothGattCharacteristic writeCharacteristic = characteristicList.get(0);
        byte[] arr1 = wifiSSID.getBytes();
        byte[] arr3 = wifiPass.getBytes();
        byte EOT = 0x4;
        //Concatenating arrays moment
        ArrayList<Byte> byteArrayList = new ArrayList<>();
        for (byte b : arr1) {
            byteArrayList.add(b);
        }
        byteArrayList.add(EOT);
        for (byte b : arr3) {
            byteArrayList.add(b);
        }

        byte[] data = new byte[byteArrayList.size()];
        for (int i = 0; i < byteArrayList.size(); i++) {
            data[i] = byteArrayList.get(i);
        }
        writeCharacteristic.setValue(data);
        bluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;

            }
        }
        // This triggers on every service discovery, but only write to our specific one.
        // Will need some kind of database integration for product UUIDs.
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService s : gatt.getServices())
                {
                    if (s.getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")) != null)
                    {
                        characteristicList.add(s.getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")));
                        System.out.println("nice");
                    }
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public ArrayList<BluetoothGattCharacteristic> getCharacteristicList() {
        return characteristicList;
    }

    public void setCharacteristicList(ArrayList<BluetoothGattCharacteristic> characteristicList) {
        this.characteristicList = characteristicList;
    }
}

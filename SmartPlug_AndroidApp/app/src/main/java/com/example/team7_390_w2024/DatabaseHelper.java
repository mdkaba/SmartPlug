package com.example.team7_390_w2024;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    static private final int MAX_TIMESTAMP_ROWS = 100;
    private Context context;
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DatabaseConfig.DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DEVICE_TABLE = "CREATE TABLE " + DatabaseConfig.TABLE_DEVICES + " (" +
                DatabaseConfig.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseConfig.COLUMN_NAME + " TEXT NOT NULL, " +
                DatabaseConfig.COLUMN_MAC_ADDRESS + " TEXT NOT NULL, " +
                DatabaseConfig.COLUMN_STATUS + " INTEGER NOT NULL" + ");";
        db.execSQL(CREATE_DEVICE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConfig.TABLE_DEVICES);
        onCreate(db);
    }

    public int insertDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        int id = -1;
        //Create history table
        String CREATE_DEVICE_TABLE = "CREATE TABLE " + DatabaseConfig.TABLE_TIMESTAMP_PREFIX + "_" + device.getName() + " (" +
                DatabaseConfig.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseConfig.COLUMN_CURRENT + " DOUBLE NOT NULL,  " +
                DatabaseConfig.COLUMN_TIMESTAMP + " TEXT NOT NULL);";
        try {
            db.execSQL(CREATE_DEVICE_TABLE);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        //Insert the device into the device table
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConfig.COLUMN_NAME, device.getName());
        contentValues.put(DatabaseConfig.COLUMN_MAC_ADDRESS, device.getBleMacAddress());
        contentValues.put(DatabaseConfig.COLUMN_STATUS, 0);
        try {
            id = (int) db.insertOrThrow(DatabaseConfig.TABLE_DEVICES, null, contentValues);
        } catch(SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
        return id;
    }

    public List<Device> getAllDevices() {
        List<Device> deviceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseConfig.TABLE_DEVICES, null, null, null, null, null, null);
            if (cursor != null) {
                if(cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DatabaseConfig.COLUMN_ID));
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseConfig.COLUMN_NAME));
                        @SuppressLint("Range") String macAddress = cursor.getString(cursor.getColumnIndex(DatabaseConfig.COLUMN_MAC_ADDRESS));
                        Device device = new Device(macAddress, name, id);
                        deviceList.add(device);
                    } while (cursor.moveToNext());
                }
            }
        } catch(SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
        return deviceList;
    }

    public Device getDevice(int id) {
        Device device = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String GET_DEVICE = "SELECT * FROM " + DatabaseConfig.TABLE_DEVICES + " WHERE " + DatabaseConfig.COLUMN_ID + " = " + id;
        try{
            cursor = db.rawQuery(GET_DEVICE, null);
            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseConfig.COLUMN_NAME));
                    @SuppressLint("Range") String macAddress = cursor.getString(cursor.getColumnIndex(DatabaseConfig.COLUMN_MAC_ADDRESS));
                    device = new Device(macAddress, name, id);
                }
            }
        } catch (SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
        return device;
    }

    public boolean getDeviceStatus(int id) {
        boolean status = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String GET_DEVICE_STATUS = "SELECT * FROM " + DatabaseConfig.TABLE_DEVICES + " WHERE " + DatabaseConfig.COLUMN_ID + " = " + id;
        try{
            cursor = db.rawQuery(GET_DEVICE_STATUS, null);
            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    @SuppressLint("Range") int statusInt  = cursor.getInt(cursor.getColumnIndex(DatabaseConfig.COLUMN_STATUS));
                    if(statusInt == 1)
                        status = true;
                }
            }
        } catch (SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
        return status;
    }

    public void setDeviceStatus(int id, boolean status) {
        SQLiteDatabase db = this.getReadableDatabase();
        int statusInt = 0;
        if(status)
            statusInt = 1;
        String SET_DEVICE_STATUS = "UPDATE " + DatabaseConfig.TABLE_DEVICES + " SET " + DatabaseConfig.COLUMN_STATUS + " = " + statusInt + " WHERE " + DatabaseConfig.COLUMN_ID + " = " + id;
        try{
            db.execSQL(SET_DEVICE_STATUS);
        } catch (SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
    }

    public void removeDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        String REMOVE_DEVICE = "DELETE FROM " + DatabaseConfig.TABLE_DEVICES + " WHERE " + DatabaseConfig.COLUMN_ID + " = " + device.getId() + ";";
        try {
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseConfig.TABLE_TIMESTAMP_PREFIX + "_" + device.getName());
            db.execSQL(REMOVE_DEVICE);
        } catch (SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
    }

    @SuppressLint("Range")
    public void addTimestamp(Device device, CurrentTimestamp currentTimestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        String TABLE_NAME = DatabaseConfig.TABLE_TIMESTAMP_PREFIX + "_" + device.getName();
        int maxId = 0;
        int minId = 0;
        String GET_MAX_ID = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + DatabaseConfig.COLUMN_ID + " DESC;";
        String GET_MIN_ID = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + DatabaseConfig.COLUMN_ID + " ASC;";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(GET_MAX_ID, null);
            if(cursor != null){
                if(cursor.moveToFirst())
                    maxId = cursor.getInt(cursor.getColumnIndex(DatabaseConfig.COLUMN_ID));
            }

            //if maxId is greater than MAX_TIMESTAMP_ROWS, then a value in the table must be deleted
            if(maxId > MAX_TIMESTAMP_ROWS) {
                cursor = db.rawQuery(GET_MIN_ID, null);
                if(cursor != null){
                    if(cursor.moveToFirst())
                        minId = cursor.getInt(cursor.getColumnIndex(DatabaseConfig.COLUMN_ID));
                }
                //We want to delete the minimum ID before adding a new one
                String DELETE_MIN_ID = "DELETE FROM " + TABLE_NAME + " WHERE " + DatabaseConfig.COLUMN_ID + " = " + minId + ";";
                db.execSQL(DELETE_MIN_ID);
            }

            //Delete the same timestamp data if exist
//            String DELETE_SAME_Data=  "DELETE FROM " + TABLE_NAME + " WHERE " + DatabaseConfig.COLUMN_TIMESTAMP + " = '" + currentTimestamp.getTimestamp() + "';";
//            db.execSQL(DELETE_SAME_Data);
            //Add the new value
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseConfig.COLUMN_TIMESTAMP, currentTimestamp.getTimestamp());
            contentValues.put(DatabaseConfig.COLUMN_CURRENT, currentTimestamp.getCurrent());
            db.insertOrThrow(TABLE_NAME, null, contentValues);

        } catch (SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
    }

    public List<CurrentTimestamp> getAllTimestamps(Device device) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        List<CurrentTimestamp> currentTimestampsList = new ArrayList<>();
        String GET_CURRENT_TIMESTAMPS = "SELECT * FROM " + DatabaseConfig.TABLE_TIMESTAMP_PREFIX + "_" + device.getName() + " ORDER BY " + DatabaseConfig.COLUMN_ID + " ASC;";
        try {
            cursor = db.rawQuery(GET_CURRENT_TIMESTAMPS, null);
            if(cursor != null) {
                if(cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") double current = cursor.getDouble(cursor.getColumnIndex(DatabaseConfig.COLUMN_CURRENT));
                        @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex(DatabaseConfig.COLUMN_TIMESTAMP));
                        CurrentTimestamp currentTimestamp = new CurrentTimestamp(current, timestamp);
                        currentTimestampsList.add(currentTimestamp);
                    } while (cursor.moveToNext());
                }
            }
        } catch (SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
        return currentTimestampsList;
    }

    public Device getNthDevice(int position) {
        List<Device> deviceList = this.getAllDevices();
        return deviceList.get(position);
    }

}

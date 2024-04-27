package com.example.team7_390_w2024;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHelper {
    private DatabaseReference firebaseRef;
    private FirebaseDatabase databaseInstance; // Done once only , we have just one db
    private float current;
    private String Mac_address;
    private boolean command;
    FirebaseHelper(){
        databaseInstance = FirebaseDatabase.getInstance(); // Only place this should be
        firebaseRef = FirebaseDatabase.getInstance().getReference("User-1");//Points at the Top Parent Node
//        setCommand(false);//Not ON
    }

    public float readCurrent(){
        //Give it a path
        firebaseRef = FirebaseDatabase.getInstance().getReference("User-1").child("Current sensor reading");
        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override//Tested and works, when value is changed in firebase database this will trigger
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                current = dataSnapshot.getValue(Float.class);
                Log.d(TAG, "Value is: " + current);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Failed to read data: " , error.toException());
            }
        });

        return current;
    }
    public void setCommand(boolean value){
        firebaseRef = FirebaseDatabase.getInstance().getReference("User-1").child("Command");
        firebaseRef.setValue(value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Command"+ value +"set successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Error setting command: " + value);
                    }
                });
    }
    public String getMacAddress(){
        firebaseRef = FirebaseDatabase.getInstance().getReference("User-1").child("MAC-ADDRESS\n");
        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override//Tested and works, when value is changed in firebase database this will trigger
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Mac_address = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + Mac_address);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Failed to read data: " , error.toException());
            }
        });

        return Mac_address;
    }
    public void setResetCommand(boolean value){
        firebaseRef = FirebaseDatabase.getInstance().getReference("User-1").child("Reset Command");
        firebaseRef.setValue(value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Reset Command"+ value +"set successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Error setting command: " + value);
                    }
                });
    }
    public boolean readCommand(){
        //Give it a path
        firebaseRef = FirebaseDatabase.getInstance().getReference("User-1").child("Command");
        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                command = Boolean.TRUE.equals(dataSnapshot.getValue(boolean.class));
                Log.d(TAG, "Value is: " + command);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Failed to read data: " , error.toException());
            }
        });
        System.out.println(command);
        return command;
    }

}

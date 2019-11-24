package com.demsit.dems;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Mydb {
    public static Mydb instance;
    public static FirebaseAuth mAuth;
    public static DatabaseReference ref;
    public static FirebaseUser user;
    public static StorageReference storageRef;
    public static long d2099;

    public synchronized  static Mydb getInstance() {
        if(instance == null) {
            instance = new Mydb();
            mAuth = FirebaseAuth.getInstance();
            ref = FirebaseDatabase.getInstance().getReference();
            user = mAuth.getCurrentUser();
            storageRef = FirebaseStorage.getInstance().getReference();
            try {
                final Calendar calD =  Calendar.getInstance();
                calD.setTime(new SimpleDateFormat("dd-M-yyyy hh:mm:ss").parse("31-12-2099 23:59:59"));
                d2099 = calD.getTimeInMillis();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        return instance;
    }

    private Mydb() {
    }
}

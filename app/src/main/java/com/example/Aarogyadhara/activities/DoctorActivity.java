package com.example.Aarogyadhara.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.Aarogyadhara.utils.CountryToPhonePrefix;

import com.example.Aarogyadhara.adapters.UserListAdapter;
import com.example.Aarogyadhara.models.UserObject;
import com.example.Aarogyadhara.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DoctorActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;
    ArrayList<UserObject> userList = new ArrayList<UserObject>();
    ArrayList<UserObject> contactList = new ArrayList<UserObject>();
    private Button create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        contactList = new ArrayList<UserObject>();
        userList = new ArrayList<UserObject>();
        create = (Button)findViewById(R.id.create);
        checkpermission();
        initializeRecyclerView();
        getContactList();
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createChat();
            }
        });
    }

    private void createChat(){
        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

        DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("user");

        HashMap newChatMap = new HashMap();
        newChatMap.put("id", key);
        newChatMap.put("user/" + FirebaseAuth.getInstance().getUid(), true);

        Boolean validChat = false;
        for(UserObject mUser : userList){
            if(mUser.getSelected()){
                validChat = true;
                newChatMap.put("user/" + mUser.getUid(), true);
                userDb.child(mUser.getUid()).child("chat").child(key).setValue(true);
            }
        }

        if(validChat){
            chatInfoDb.updateChildren(newChatMap);
            userDb.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
            Intent intent = new Intent(DoctorActivity.this,ChatListActivity.class);
            startActivity(intent);
        }

    }

    private void getContactList() {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        try {
            String ISOPrefix = getCountryISO();
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = formatPhoneNumber(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)), ISOPrefix);

                UserObject mContact = new UserObject("", name, phone);
                contactList.add(mContact);
                getUserDetails(mContact);
            }
        } finally {
            if (phones != null) {
                phones.close();
            }
        }
    }

    private String formatPhoneNumber(String phoneNumber, String ISOPrefix) {
        phoneNumber = phoneNumber.replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = ISOPrefix + phoneNumber;
        }
        return phoneNumber;
    }


    private void getUserDetails(UserObject mContact) {
        // Extracting the base phone number without the country code
        String basePhoneNumber = mContact.getPhone().replaceAll("^[+\\d]{1,4}", "");  // Remove the country code prefix
        // Assume the phone number includes the country code initially
        String fullPhoneNumber = mContact.getPhone();

        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        // First, try to query with the full phone number
        queryPhoneNumber(mUserDB, fullPhoneNumber);
        // Then, query with the base phone number
        queryPhoneNumber(mUserDB, basePhoneNumber);
    }

    private void queryPhoneNumber(DatabaseReference mUserDB, String phoneNumber) {
        Query query = mUserDB.orderByChild("phone").equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean userFound = false;
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        UserObject mUser = childSnapshot.getValue(UserObject.class);
                        if (mUser != null) {
                            mUser.setUid(childSnapshot.getKey());
                            userList.add(mUser);
                            userFound = true;
                        }
                    }
                    mUserListAdapter.notifyDataSetChanged();
                    if (userFound) {
                        Log.d("FirebaseQuery", "User details fetched for phone: " + phoneNumber);
                    } else {
                        Log.d("FirebaseQuery", "User data exists but could not be parsed for phone: " + phoneNumber);
                    }
                } else {
                    Log.d("FirebaseQuery", "No data exists for provided query with phone: " + phoneNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DoctorActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }




    private String getCountryISO(){
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso()!=null)
            if (!telephonyManager.getNetworkCountryIso().toString().equals(""))
                iso = telephonyManager.getNetworkCountryIso().toString();
        return CountryToPhonePrefix.getPhone(iso);
        //return CountryToPhonePrefix.getPhone(iso);
    }

    private void checkpermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("This permission is needed to access the contacts")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(DoctorActivity.this,
                                        new String[]{Manifest.permission.READ_CONTACTS},
                                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
    }


// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant. The callback method gets the result of the request.



    @SuppressLint("WrongConstant")
    private void initializeRecyclerView() {
        mUserList= findViewById(R.id.userList);
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        // mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(userList);
        mUserList.setAdapter(mUserListAdapter);
    }

}

package com.example.mc_project;

import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnWifiSync {
    private String TAG = "x212";
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private BottomNavigationView bottomNavigationView;

    ArrayList<ArrayList<String>> csv;

    boolean isOnline = false;

    private WifiData previousLoc = null;

    private FirebaseUser user;
    static boolean ischecked=false;
//    static boolean toggleOn = false;

    private long timeDelay = 0;

    private DatabaseReference arrayRef;
    private Handler handler;

    private int delay = 0;

    int i = 0;

    private ArrayList<WifiData> wifiDataList;

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            user = auth.getCurrentUser();
            timeDelay = delay;
            List<ScanResult> scanResults = wifiManager.getScanResults();
            wifiDataList = new ArrayList<>();
            for (ScanResult scanResult : scanResults) {
                int index = bssidExists(scanResult.BSSID);
                if (index == -1) {
                    WifiData wifiData = new WifiData(scanResult.SSID, scanResult.BSSID, scanResult.level, scanResult.frequency);
                    wifiData.getLevel()[i] = scanResult.level;
                    i++;
                    i %= 5;
                    wifiDataList.add(wifiData);

                } else {
                    WifiData oldWifiData = wifiDataList.get(index);
                    oldWifiData.getLevel()[i] = scanResult.level;
                    i++;
                    i %= 5;

                    wifiDataList.set(index, oldWifiData);
                }


            }

            for (int i = 0; i < wifiDataList.size(); i++) {
                int flag = 0;
                for (List<String> row : csv) {
                    if (removeLastChar(row.get(5)).equals(removeLastChar(wifiDataList.get(i).getBssid()))) {
                        wifiDataList.get(i).setRouterLoc(row.get(2) + " , " + row.get(3));
                        flag = 1;
                        break;
                    }


                }
                if (flag == 0) {
                    wifiDataList.remove(wifiDataList.get(i));

                }

            }

            int maxi = -200;
            int index = 0;
            for (int i = 0; i < wifiDataList.size(); i++) {
                if (wifiDataList.get(i).getDistance() > maxi) {
                    maxi = (int) wifiDataList.get(i).getDistance();
                    index = i;
                }
            }

            WifiData loc = null;
            if (wifiDataList.size() == 0) {
                loc = previousLoc;
            } else {
                loc = wifiDataList.get(index);
                previousLoc = loc;
            }

            WifiData finalLoc = loc;

            arrayRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    List<ListContents> array = mutableData.getValue(new GenericTypeIndicator<List<ListContents>>() {
                    });
                    if (array == null) {
                        // If the array doesn't exist, create a new one
                        array = new ArrayList<>();
                    }

                    boolean itemExists = false;
                    ListContents listContents = null;
                    if (user == null) return Transaction.abort();
                    for (int i = 0; i < array.size(); i++) {
                        ListContents element = array.get(i);
                        if (element.getEmail().equals(user.getEmail())) {
                            // If the item already exists in the array, set the flag and exit the loop
                            listContents = element;
                            itemExists = true;
                            array.remove(element);
                            break;
                        }
                    }

                    if (!itemExists) {
                        // If the item doesn't already exist in the array, add it
                        ListContents newItem = new ListContents();
                        if(finalLoc == null && user != null){
                            newItem.setName(user.getDisplayName());
                            newItem.setEmail(user.getEmail());
                            newItem.setLoc("Out of AP's range");
                            newItem.setTxt(user.getDisplayName());
                            newItem.setTimestamp(System.currentTimeMillis());
                            array.add(newItem);
                            mutableData.setValue(array);
                        }

                        else if (user != null) {
                            newItem.setName(user.getDisplayName());
                            newItem.setEmail(user.getEmail());
                            newItem.setLoc(finalLoc.getRouterLoc());
                            newItem.setTxt(user.getDisplayName());
                            newItem.setTimestamp(System.currentTimeMillis());
                            array.add(newItem);
                            mutableData.setValue(array);
                        }
                    } else {
                        if(finalLoc == null){
                            listContents.setLoc("Out of AP's range");
                            listContents.setTimestamp(System.currentTimeMillis());
                            array.add(listContents);
                            mutableData.setValue(array);
                        }
                        if (user != null && listContents != null) {
                            listContents.setLoc(finalLoc.getRouterLoc());
                            listContents.setTimestamp(System.currentTimeMillis());
                            array.add(listContents);
                            mutableData.setValue(array);
                        }
                    }
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable,timeDelay);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
//                    handler.postDelayed(runnable,timeDelay);
                }


            });

        }
    }; // 5000 milliseconds = 5 seconds


    private String removeLastChar(String original) {
        return original.substring(0, original.length() - 1);
    }

    private int bssidExists(String bssid) {
        int index = -1;
        for (int i = 0; i < wifiDataList.size(); i++) {
            if (wifiDataList.get(i).getBssid().equals(bssid)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null)
            handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (handler == null) {
            handler = new Handler();
        }
        if(ischecked) {
            handler.postDelayed(runnable, timeDelay);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("sync", Context.MODE_PRIVATE);
        delay = sharedPreferences.getInt("syncTime", 75);
        delay *= 1000;


        database = FirebaseDatabase.getInstance();
        arrayRef = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        InputStream inputStream = null;
        try {
            inputStream = this.getAssets().open("IIITD-Wifi.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        CSVReader reader = new CSVReader(inputStreamReader);
        String[] nextLine;

        csv = new ArrayList<>();
        try {
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of strings representing the values in the row
                ArrayList<String> row = new ArrayList<>(Arrays.asList(nextLine).subList(0, 7));
                csv.add(row);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        bottomNavigationView = findViewById(R.id.navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Change the text to an icon for the selected item
                if (item.getItemId() == R.id.navigation_home) {
                    ListFragment listFragment = new ListFragment();
                    listFragment.setListener(MainActivity.this);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.listFragment, listFragment);
                    fragmentTransaction.commit();

                } else {
                    SettingActivity settingActivity = new SettingActivity();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.listFragment, settingActivity);
                    fragmentTransaction.commit();
                }
                return true;
            }
        });


// Run the transaction to add the item to the array if it doesn't exist


        ListFragment listFragment = new ListFragment();
//        listFragment.setArguments(b);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.listFragment, listFragment);
        fragmentTransaction.commit();
    }


    @Override
    public void startHandler() {
        if (handler != null)
            handler.postDelayed(runnable, timeDelay);
    }

    @Override
    public void stopHandler() {
        if (handler != null)
            handler.removeCallbacks(runnable);
    }
}

interface OnWifiSync {
    void startHandler();

    void stopHandler();
}




package com.example.mc_project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

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


public class SyncService extends Service {

    ArrayList<ArrayList<String>> csv;
    int i = 0;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private WifiData previousLoc = null;
    private ArrayList<WifiData> wifiDataList;
    private FirebaseUser user;
    private DatabaseReference arrayRef;
    private long timeDelay = 0;
    private Handler handler;

    private int delay = 0;
    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            WifiManager wifiManager = (WifiManager) SyncService.this.getSystemService(Context.WIFI_SERVICE);
            if (ActivityCompat.checkSelfPermission(SyncService.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
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
//                    Log.d(TAG, "readings: "+ removeLastChar(wifiDataArrayList.get(i).getBssid())+ " "+ removeLastChar(String.valueOf(row.get(5))));
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
            if (loc == null) return;
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

                        if (user != null) {
                            newItem.setName(user.getDisplayName());
                            newItem.setEmail(user.getEmail());
                            newItem.setLoc(finalLoc.getRouterLoc());
                            newItem.setTxt(user.getDisplayName());
                            newItem.setTimestamp(System.currentTimeMillis());
                            array.add(newItem);
                            mutableData.setValue(array);
                        }
                    } else {
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


    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = getSharedPreferences("sync", Context.MODE_PRIVATE);
//        delay = sharedPreferences.getInt("syncTime", 30);
        delay = 30*60*1000;
        InputStream inputStream = null;
        try {
            inputStream = this.getAssets().open("IIITD-Wifi.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        CSVReader reader = new CSVReader(inputStreamReader);
        String[] nextLine;
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("WIFI_SYNC");
//        registerReceiver(broadcastReceiver,intentFilter);
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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(60, createNotification());


        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        arrayRef = FirebaseDatabase.getInstance().getReference("users");
        if (handler == null) {
            handler = new Handler();
        }
        handler.postDelayed(runnable, timeDelay);
        return START_NOT_STICKY;
    }


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


    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Wi-Fi Localizer is Running";
            String description = "Your Location is being Shared";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("my_channel_id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

// Create a notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "my_channel_id")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Wi-Fi Localizer is Running")
                .setContentText("Your Location is being Shared")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

// Show the notification
        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null)
            handler.removeCallbacks(runnable);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

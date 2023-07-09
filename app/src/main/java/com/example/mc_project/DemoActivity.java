package com.example.mc_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.example.mc_project.databinding.ActivityDemoBinding;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class DemoActivity extends AppCompatActivity {

    ActivityDemoBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Map<String,List<DataPoint>> dataPoints = null;
        int k = 3;
        try {
            dataPoints = readData();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, List<DataPoint>> finalDataPoints = dataPoints;

        binding.find.setOnClickListener((view) ->{

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return ;
            }
            if(finalDataPoints == null) return;

            List<DataPoint> target = new ArrayList<>();

            List<ScanResult> list = wifiManager.getScanResults();
            for(ScanResult scanResult: list){
                target.add(new DataPoint("",scanResult.BSSID,""+scanResult.level));
            }


            List<String>  neighbors = findKNearestNeighbors(finalDataPoints, target,k );
            String roomLabel = getMostCommonRoomLabel(neighbors);
            binding.location.setText(roomLabel);


        });

        }
    private static String getMostCommonRoomLabel(List<String> neighbors) {
        Map<String, Integer> roomLabelCounts = new HashMap<>();
        for (String roomLabel : neighbors) {
            int count = roomLabelCounts.getOrDefault(roomLabel, 0);
            roomLabelCounts.put(roomLabel, count + 1);
        }
        return Collections.max(roomLabelCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }


    private List<String> findKNearestNeighbors(Map<String,List<DataPoint>> dataPoints, List<DataPoint> target, int k) {
        // Calculate the distance from the target to each data point
        HashMap<String, Double> distances = new HashMap<>();
        for(Map.Entry<String,List<DataPoint>> map :dataPoints.entrySet()) {
            double distance = calculateDistance(target, map.getValue());
            distances.put(map.getKey(), distance);
        }

        distances = sortByValue((HashMap<String, Double>) distances);
       List<String> locations = new ArrayList<>();
        for(Map.Entry<String,Double> mp:distances.entrySet()){
            if(mp.getValue()==90000.0) continue;
            locations.add(dataPoints.get(mp.getKey()).get(0).location);
        }



        return locations.subList(0,Math.min(k,locations.size()));
    }

    public  HashMap<String, Double> sortByValue(HashMap<String, Double> hm)
    {
        HashMap<String, Double> temp
                = hm.entrySet()
                .stream()
                .sorted((i1, i2)
                        -> i1.getValue().compareTo(
                        i2.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        return temp;
    }
    private double calculateDistance(List<DataPoint> dataPoint1, List<DataPoint> dataPoint2) {
        double distance = 0.0;
        for (int i  = 0; i < dataPoint1.size();i++) {
            for (int j = 0; j < dataPoint2.size(); j++){
//                Log.d("x321", "calculateDistance: "+ dataPoint1.get(i).macAddress+ " " + dataPoint2.get(j).macAddress);
                if (dataPoint2.get(j).macAddress.equals(dataPoint1.get(i).macAddress)) {
                    double difference = Double.parseDouble(dataPoint2.get(j).rssi) - Double.parseDouble(dataPoint1.get(i).rssi);
                    distance += Math.pow(difference, 2);
                }
            }
        }
        if(distance==0){
            return 90000.0;
        }
        return Math.sqrt(distance);
    }




    private  Map<String,List<DataPoint>> readData() throws Exception {
        List<DataPoint> dataPoints = new ArrayList<>();
        InputStream inputStream = this.getAssets().open("cleaned_data.csv");
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String line;
        br.readLine();
        Map<String,List<DataPoint>> map = new TreeMap<>();
        Map<String, List<String[]>> groupedRows = new HashMap<>();
        int k = 1;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            String lat = parts[4];
            String lon = parts[5];
            lat = lat.replace("\"","");
            lon = lon.replace("\"","");

            String latLngKey = lat+ "," + lon;
            if(lat.equals("0") && lon.equals("0")){
                continue;
            }
            if(parts[0].equals("\"c4:0a:cb:2d:95:a5\"")){
                Log.d("x312", "readData: ");
            }
            if (!groupedRows.containsKey(latLngKey)) {
                k++;
                groupedRows.put(latLngKey, new ArrayList<>());
            }
            groupedRows.get(latLngKey).add(parts);
        }
        for (Map.Entry<String, List<String[]>> entry : groupedRows.entrySet()) {
            for (String[] row : entry.getValue()) {
                String target = row[6];
                String mac = row[1];
                String rssi = row[3];
                rssi = rssi.replace("\"","");
                mac =mac.replace("\"","");
                target = target.replace("\"","");
                DataPoint dp = new DataPoint(target,mac,rssi);
                if(!map.containsKey(entry.getKey())) {
                    map.put(entry.getKey(), new ArrayList<>());
                }
                map.get(entry.getKey()).add(dp);
            }
        }



        br.close();
        return map;
    }

    private  class DataPoint {
        public String macAddress;
        public String rssi;

        public String location;

        public DataPoint(String location, String macAddress, String rssi) {
            this.macAddress = macAddress;
            this.rssi = rssi;
            this.location = location;
        }
    }

    private class DataPointDistance {
        public DataPoint dataPoint;
        public double distance;

        public DataPointDistance(DataPoint dataPoint, double distance) {
            this.dataPoint = dataPoint;
            this.distance = distance;
        }
    }
}

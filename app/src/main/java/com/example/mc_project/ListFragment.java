package com.example.mc_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class ListFragment extends Fragment {

    ImageView wifi;

    TextView loc;
    TextView nouser;

    static Boolean first=false;
    AnimationDrawable wifiAnimation;
    private ArrayList<ListContents> items;
    private RecyclerView recyclerView;

    private ValueEventListener valueEventListener = new ValueEventListener(){

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.getValue() == null) return;
            GenericTypeIndicator<ArrayList<ListContents>> t = new GenericTypeIndicator<ArrayList<ListContents>>() {};
            ArrayList<ListContents> array = snapshot.getValue(t);
            items = array;
            if(items != null && items.size()>1){
                nouser.setVisibility(View.GONE);
            }
            if(items == null || items.size()<=1) {
                wifi.setVisibility(View.VISIBLE);
                wifi.setBackgroundResource(R.drawable.wifi_selector);
                wifiAnimation=(AnimationDrawable) wifi.getBackground();
                wifiAnimation.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        wifi.setVisibility(View.GONE);

                    }
                },5000);

                nouser.setVisibility(View.VISIBLE);
            }
            if(user != null && items != null) {
                for (ListIterator<ListContents> it = items.listIterator(); it.hasNext(); ) {
                    ListContents listContents = it.next();
                    if (Objects.equals(user.getEmail(), listContents.getEmail())) {
                        if(listContents.getLoc() == null || listContents.getLoc().length() == 0){
                            loc.setText("Out of AP's Range");
                        } else {
                            loc.setText(listContents.getLoc());
                        }
                        it.remove();
                    }
                }
            }
            adapter.updateArrayList(items);


        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private RecyclerAdapter adapter;
    private DatabaseReference usersRef;

    private FirebaseAuth firebaseAuth;
    private Switch toggle;
    private FirebaseUser user;
    private OnWifiSync wifiSync;

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View frag = inflater.inflate(R.layout.fragment_list, container, false);
        recyclerView = (RecyclerView) frag.findViewById(R.id.RecyclerView);
        TextView nameTv = frag.findViewById(R.id.headText);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        items = new ArrayList<>();
        loc=frag.findViewById(R.id.location);
        wifi= (ImageView) frag.findViewById(R.id.imageView4);
        if(!first){
            wifi.setBackgroundResource(R.drawable.wifi_selector);
            wifiAnimation=(AnimationDrawable) wifi.getBackground();
            wifiAnimation.start();
            first=true;
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, 3);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    wifi.setVisibility(View.GONE);

                }
            },5000);
        }
        nouser=frag.findViewById(R.id.no_user);
//        nouser.setVisibility(View.GONE);
//        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.textanim);
//        nouser.startAnimation(animation);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                wifiAnimation.start();
//            }
//        },1000);
        user = firebaseAuth.getCurrentUser();
        if(user != null) {
            nameTv.setText(user.getDisplayName());
        }

        adapter = new RecyclerAdapter(items, this.getContext());
        recyclerView.setAdapter(adapter);
        // Inflate the layout for this fragment

        toggle = frag.findViewById(R.id.switchBtn);
        toggle.setChecked(MainActivity.ischecked);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.ischecked = isChecked;
                if(isChecked) {
                    if(wifiSync != null){
                        wifiSync.startHandler();
                    }
                    toggle.setTextColor(getResources().getColor(R.color.green));
                    Intent intent = new Intent(getActivity(), SyncService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getContext().startForegroundService(intent);
                    } else{
                        getContext().startService(intent);
                    }
                }

                else {
                    if(wifiSync != null){
                        wifiSync.stopHandler();
                    }
                    toggle.setTextColor(getResources().getColor(R.color.gray));
                    Intent intent = new Intent(getActivity(), SyncService.class);
                    getContext().stopService(intent);
                }

            }
        });

//        if(toggle.isChecked())
//            shareLocation.setTextColor(this.getResources().getColor(R.color.green));
//        else
//            shareLocation.setTextColor(this.getResources().getColor(R.color.gray));
        return frag;
    }

    public void setListener(OnWifiSync wifiSync){
        this.wifiSync = wifiSync;
    }

    @Override
    public void onResume() {
        super.onResume();
        usersRef.addValueEventListener(valueEventListener);


    }

    @Override
    public void onPause() {
        super.onPause();
        usersRef.removeEventListener(valueEventListener);
    }
}
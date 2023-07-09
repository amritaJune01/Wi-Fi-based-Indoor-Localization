package com.example.mc_project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SettingActivity extends Fragment {

    FirebaseAuth firebaseAuth;

    SharedPreferences sharedPreferences;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View frag = inflater.inflate(R.layout.settings, container, false);
        sharedPreferences = getContext().getSharedPreferences("sync",Context.MODE_PRIVATE);
        return frag;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView email = view.findViewById(R.id.email);
        TextView name = view.findViewById(R.id.name);
        AppCompatButton button = view.findViewById(R.id.logout);
        AppCompatButton button1 = view.findViewById(R.id.submit);
        EditText editText = view.findViewById(R.id.syncTime);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            email.setText(user.getEmail());
            name.setText(user.getDisplayName());
        }
        button1.setOnClickListener(v -> {
            try {
                int sync = Integer.parseInt(editText.getText().toString());
                if(sync >= 75 && sync <= 200) {
                    sharedPreferences.edit().putInt("syncTime", sync).apply();
                    Toast.makeText(getContext(), "Sync Time Updated: "+sync, Toast.LENGTH_SHORT).show();

                } else{
                    Toast.makeText(getContext(), "Sync Time Should be in 75 to 200 ", Toast.LENGTH_SHORT).show();

                }
            }catch (Exception e){
                Toast.makeText(getContext(), "Enter proper number", Toast.LENGTH_SHORT).show();
            }
        });
        button.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

}

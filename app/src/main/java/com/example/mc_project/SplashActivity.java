package com.example.mc_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {
    ImageView earth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        earth=findViewById(R.id.imageView2);
        Animation rotate= AnimationUtils.loadAnimation(this,R.anim.rotateanimation);
        earth.startAnimation(rotate);
        new Handler().postDelayed(() ->{
            Intent i=new Intent(SplashActivity.this,Login.class);
            startActivity(i);
            finish();
        },2000);
    }
}
package com.example.mc_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mc_project.databinding.SignInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    private String TAG = "x212";

    private SignInBinding binding;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isValidEmail(binding.email.getText())){
                    Toast.makeText(Login.this,"Enter valid email", Toast.LENGTH_LONG).show();
                    return;
                }
                if(binding.password.getText().length() < 6){
                    Toast.makeText(Login.this,"Password must greater than 6 characters", Toast.LENGTH_LONG).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(binding.email.getText().toString(),binding.password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(!user.isEmailVerified()){
                                Toast.makeText(Login.this,"Email is not verified please verify then login",Toast.LENGTH_LONG).show();
                            } else{
                                Intent intent = new Intent(Login.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else{
                            Toast.makeText(Login.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        binding.signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Signup.class);
            startActivity(intent);
            finish();
        });
    }
    private  boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}"+"\\@iiitd.ac.in").matcher(target).matches());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            if(currentUser.isEmailVerified()){
                Intent intent = new Intent(Login.this,MainActivity.class);
                startActivity(intent);
                finish();
            } else{
                Toast.makeText(Login.this,"Email is not verified please verify then login",Toast.LENGTH_LONG).show();
            }
        }
    }
}
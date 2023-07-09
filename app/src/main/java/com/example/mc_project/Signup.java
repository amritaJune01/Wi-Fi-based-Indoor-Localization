package com.example.mc_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mc_project.databinding.SignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Pattern;


public class Signup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SignUpBinding binding;
    private String TAG = "x212";

    TextView signin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
//        getSupportActionBar().hide(); // hide the title bar
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        binding = SignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        createAccount();
        signin=findViewById(R.id.signInButton);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Signup.this,Login.class);
                startActivity(i);
                finish();
            }
        });
    }
    private  boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}"+"\\@iiitd.ac.in").matcher(target).matches());
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

    }
    public void createAccount() {
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.name.getText().length() == 0) {
                    Toast.makeText(Signup.this,"Enter valid name", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!isValidEmail(binding.email.getText())){
                    Toast.makeText(Signup.this,"Enter valid email", Toast.LENGTH_LONG).show();
                    return;
                }
                if(binding.password.getText().length() < 6){
                    Toast.makeText(Signup.this,"Password must greater than 6 characters", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!(binding.password.getText().toString().equals(binding.confirmPassword.getText().toString()))){
                    Toast.makeText(Signup.this,"Password and confirm password must be same", Toast.LENGTH_LONG).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(binding.email.getText().toString(), binding.password.getText().toString())
                        .addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setDisplayName(binding.name.getText().toString()).build();
                                    if (user != null) {
                                        user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "Verification sent");
                                                            Intent intent = new Intent(Signup.this, Login.class);
                                                            Toast.makeText(Signup.this, "Email verification has sent ,please verify before login", Toast.LENGTH_LONG).show();
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "Verification sent");
                                                            Intent intent = new Intent(Signup.this, Login.class);
                                                            Toast.makeText(Signup.this, "Email verification has sent ,please verify before login", Toast.LENGTH_LONG).show();
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                });

                                            }
                                        });

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(Signup.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        //                            updateUI(null);
                                    }
                                }

                            }
                        });
            }
        });

    }
}
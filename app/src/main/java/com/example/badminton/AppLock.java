package com.example.badminton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.databinding.ActivityAppLockBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

@SuppressLint("ClickableViewAccessibility")
public class AppLock extends AppCompatActivity {
    ActivityAppLockBinding binding;
    GestureDetector gestureDetector;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppLockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        gestureDetector = new GestureDetector(this, new SingleTapClick());
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking...");

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(AppLock.this, MainActivity.class);
            Toast.makeText(AppLock.this, "Already Admin", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }

        binding.login.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (binding.adminKey.getText().toString().isEmpty())
                        binding.adminKey.setError("Required");
                    else {
                        progressDialog.show();
                        mAuth.signInWithEmailAndPassword("harshmange03@gmail.com",binding.adminKey.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(AppLock.this, MainActivity.class);
                                    startActivity(intent);
                                    binding.adminKey.setText("");
                                    Toast.makeText(AppLock.this, "Successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AppLock.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
                return true;
            }
        });
        binding.guest.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)){
                    Intent intent = new Intent(AppLock.this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(AppLock.this, "Successful", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
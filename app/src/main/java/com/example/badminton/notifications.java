package com.example.badminton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.View;
import android.view.WindowManager;

import com.example.badminton.Adapter.GameAdapter;
import com.example.badminton.Adapter.NotificationAdapter;
import com.example.badminton.Modules.NewGame;
import com.example.badminton.Modules.NotificationClass;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.databinding.ActivityNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class notifications extends AppCompatActivity {
    ActivityNotificationsBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    GestureDetector gestureDetector;
    ArrayList<NotificationClass> list = new ArrayList<>();
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(notifications.this);
        progressDialog.setMessage("Sending");
        database = FirebaseDatabase.getInstance();
        gestureDetector = new GestureDetector(this, new SingleTapClick());
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        NotificationAdapter adapter = new NotificationAdapter(list, this);
        binding.recycleView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recycleView.setLayoutManager(layoutManager);

        database.getReference().child("Notifications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NotificationClass notificationClass = dataSnapshot.getValue(NotificationClass.class);
                    list.add(notificationClass);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
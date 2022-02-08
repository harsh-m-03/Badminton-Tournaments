package com.example.badminton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.example.badminton.Fragment.BottomSheets.AddWeeklyWinner;
import com.example.badminton.Fragment.InGameFragment.Matches;
import com.example.badminton.Fragment.InGameFragment.Table;
import com.example.badminton.Fragment.InGameFragment.Teams;
import com.example.badminton.Fragment.RankingFragments.RankingForIndividual;
import com.example.badminton.Fragment.RankingFragments.RankingForWeekly;
import com.example.badminton.Fragment.RankingFragments.WeeklyWinnerHistory;
import com.example.badminton.Modules.IndividualRankings;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.TeamInfo;
import com.example.badminton.Modules.WeeklyRankings;
import com.example.badminton.databinding.ActivityIndividualBinding;
import com.example.badminton.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Individual extends AppCompatActivity {
    FirebaseDatabase database;
    GestureDetector gestureDetector;
    ActivityIndividualBinding binding;

    //I know I can make fragments for weekly and overall, but i wanted them in one
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIndividualBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        gestureDetector = new GestureDetector(this, new SingleTapClick());
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        database.getReference().child("Total Tournament").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String number = snapshot.getValue(String.class);
                binding.heading.setText("Total Tournaments Played: " + number);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentholder, new RankingForWeekly()).commit();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment = null;

                if ("OverAll".equals(tab.getText())) {
                    selectedFragment = new RankingForIndividual();
                } else if (tab.getText().equals("Weekly")) {
                    selectedFragment = new RankingForWeekly();
                } else if (tab.getText().equals("History"))
                    selectedFragment = new WeeklyWinnerHistory();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentholder, selectedFragment).commit();

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Individual.this, MainActivity.class);
        startActivity(intent);
    }
}
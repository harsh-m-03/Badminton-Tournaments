package com.example.badminton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.badminton.Fragment.InGameFragment.Matches;
import com.example.badminton.Fragment.InGameFragment.Table;
import com.example.badminton.Fragment.InGameFragment.Teams;
import com.example.badminton.databinding.ActivityInGamePageBinding;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class InGamePage extends AppCompatActivity {
    ActivityInGamePageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInGamePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        String name = getIntent().getStringExtra("Name");
        binding.sampleName.setText(name);
        String round = getIntent().getStringExtra("Rounds");
        binding.rounds.setText("Number of Rounds: " + round);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentholder, new Teams(), "Back").addToBackStack(null).commit();


        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment;

                if ("Matches".equals(tab.getText()))
                    selectedFragment = new Matches();
                else if (tab.getText().equals("Table"))
                    selectedFragment = new Table();
                else
                    selectedFragment = new Teams();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentholder, selectedFragment, "Back").addToBackStack(null).commit();

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
        super.onBackPressed();
        Intent intent=new Intent(InGamePage.this,MainActivity.class);
        startActivity(intent);
    }
}
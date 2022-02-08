package com.example.badminton.Fragment.RankingFragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.badminton.Fragment.BottomSheets.AddWeeklyWinner;
import com.example.badminton.Individual;
import com.example.badminton.MainActivity;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.WeeklyRankings;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentRankingForWeeklyBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankingForWeekly extends Fragment {
    FragmentRankingForWeeklyBinding binding;
    GestureDetector gestureDetector;
    FirebaseDatabase database;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());
        database = FirebaseDatabase.getInstance();
        binding = FragmentRankingForWeeklyBinding.inflate(inflater, container, false);

        binding.add.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    AddWeeklyWinner frag = new AddWeeklyWinner();
                    frag.show(getActivity().getSupportFragmentManager(), frag.getTag());
                }
                return true;
            }
        });

        database.getReference().child("Weekly Status").child("Current Week").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int current = snapshot.getValue(Integer.class);
                binding.weekNo.setText("Week: " + String.valueOf(current));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        List<WeeklyRankings> weeklyList = new ArrayList<>();
        database.getReference().child("Weekly Status").child("Player Info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    WeeklyRankings weeklyRankings = snap.getValue(WeeklyRankings.class);
                    weeklyList.add(weeklyRankings);
                }
                int i = 1;
                int totalMatches = 0, totalPlayers = 0;
                String weeklyRank = "Rank\n\n", weeklyNames = "Name\n\n",
                        weeklyPercentage = "PCT (%)\n\n", weeklyTotalPoints = "Points\n\n",
                        weeklyWins = "Wins\n\n", weeklyPlayed = "Played\n\n",
                        weeklyLoss = "Loss\n\n", runnerup = "II\n\n";
                Collections.sort(weeklyList, new weeklyPct());

                for (WeeklyRankings k : weeklyList) {
                    if (Integer.parseInt(k.getTournamentPlayed()) >= 8) {
                        totalPlayers++;
                        totalMatches += Integer.parseInt(k.getTournamentPlayed());
                    }
                    weeklyRank += "  " + String.valueOf(i++) + "\n";
                    weeklyNames += k.getPlayerName() + "\n";
                    weeklyTotalPoints += k.getPointsObtained() + "/" + k.getTotalPoints() + "\n";
                    weeklyWins += k.getTournamentWon() + "\n";
                    weeklyPlayed += k.getTournamentPlayed() + "\n";
                    weeklyLoss += k.getTournamentLost() + "\n";
                    weeklyPercentage += k.getWinningPercentage() + "\n";
                    runnerup += String.valueOf(k.getRunnerUps()) + '\n';
                }

                float avg = Math.round((totalMatches * 1.0) / totalPlayers);

                binding.avgMatches.setText("Average Tournaments: " + String.valueOf(avg).substring(0, String.valueOf(avg).length() - 2));

                binding.weeklyRank.setText(weeklyRank);
                binding.weeklyNames.setText(weeklyNames);
                binding.weeklyPercentage.setText(weeklyPercentage);
                binding.weeklyTotalPoints.setText(weeklyTotalPoints);
                binding.weeklyTournamentWins.setText(weeklyWins);
                binding.weeklyTournamentPlayed.setText(weeklyPlayed);
                binding.weeklyTournamentLoss.setText(weeklyLoss);
                binding.weeklyTournamentRunnerUps.setText(runnerup);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.send.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    String msgToSend = "RK|Nam|PCT\n\n";

                    String[] ranks = binding.weeklyRank.getText().toString().split("\n");
                    String[] weeklyNames = binding.weeklyNames.getText().toString().split("\n");
                    String[] weeklyPct = binding.weeklyPercentage.getText().toString().split("\n");

                    for (int i = 2; i < ranks.length; i++) {
                        if (Integer.parseInt(ranks[i].substring(2)) < 10)
                            ranks[i] = "0" + ranks[i].substring(2);
                        else ranks[i] = ranks[i].substring(2);
                        msgToSend += ranks[i] + " | " + weeklyNames[i] + " | " + weeklyPct[i] + "\n";
                    }
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, msgToSend);
                    sendIntent.setPackage("com.whatsapp");
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
                return true;
            }
        });

        return binding.getRoot();
    }

    public class weeklyPct implements Comparator {

        public int compare(Object o1, Object o2) {
            WeeklyRankings p1 = (WeeklyRankings) o1;
            WeeklyRankings p2 = (WeeklyRankings) o2;
            if (!p2.getWinningPercentage().equals(p1.getWinningPercentage()))
                return Float.compare(Float.parseFloat(p2.getWinningPercentage()), Float.parseFloat(p1.getWinningPercentage()));
            else if (!p2.getTournamentWon().equals(p1.getTournamentWon()))
                return (Integer.parseInt(p2.getTournamentWon()) - Integer.parseInt(p1.getTournamentWon()));
            else if (!p2.getTournamentPlayed().equals(p1.getTournamentPlayed()))
                return (Integer.parseInt(p1.getTournamentPlayed()) - Integer.parseInt(p2.getTournamentPlayed()));
            else
                return ((p2.getRunnerUps()) - (p1.getRunnerUps()));
        }
    }
}
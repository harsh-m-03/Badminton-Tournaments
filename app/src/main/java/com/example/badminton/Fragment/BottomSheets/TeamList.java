package com.example.badminton.Fragment.BottomSheets;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.badminton.Modules.IndividualRankings;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.TeamInfo;
import com.example.badminton.Modules.WeeklyRankings;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentTeamListBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TeamList extends BottomSheetDialogFragment {
    FragmentTeamListBinding binding;
    ProgressDialog progressDialog;
    GestureDetector gestureDetector;
    FirebaseDatabase database;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Setting Teams...\nThis might take some Time");
        binding = FragmentTeamListBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());

        String names = getArguments().getString("names", "");
        String gameName = getActivity().getIntent().getStringExtra("Name");


        List<String> players = Arrays.asList(names.split(" "));

        Collections.shuffle(players);
        String finalList = "";
        for (int i = 0; i + 1 < players.size(); i += 2) {
            finalList += players.get(i) + "-" + players.get(i + 1) + '\n';
        }
        if (players.size() % 2 != 0) finalList += players.get(players.size() - 1) + '\n';
        binding.teamList.setText(finalList);

        binding.redo.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    int randomNumber = (int) (Math.random() * (9 - 2 + 1) + 2);
                    while (randomNumber-- > 0) {
                        Collections.shuffle(players);
                        Collections.shuffle(players);
                    }
                    String temp = "";
                    for (int i = 0; i + 1 < players.size(); i += 2) {
                        temp += players.get(i) + "-" + players.get(i + 1) + '\n';
                    }
                    if (players.size() % 2 != 0) temp += players.get(players.size() - 1) + '\n';
                    binding.teamList.setText(temp);
                }
                return true;
            }
        });
        binding.confirm.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    progressDialog.show();
                    database.getReference().child("Weekly Status").child("All Players").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String weeklyPlayers = snapshot.getValue(String.class);
                            for (String s : players) {
                                if (!weeklyPlayers.contains(s)) {
                                    WeeklyRankings weeklyRankings = new WeeklyRankings();
                                    weeklyRankings.setPlayerName(s);
                                    weeklyRankings.setTournamentLost("0");
                                    weeklyRankings.setWinningPercentage("0");
                                    weeklyRankings.setTotalPoints("0");
                                    weeklyRankings.setRunnerUps(0);
                                    weeklyRankings.setPointsObtained("0");
                                    weeklyRankings.setTournamentPlayed("0");
                                    weeklyRankings.setTournamentWon("0");
                                    weeklyPlayers += s + " ";

                                    database.getReference().child("Weekly Status").child("Player Info").child(s).setValue(weeklyRankings).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful())
                                                Toast.makeText(getContext(), "Weekly: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            database.getReference().child("Weekly Status").child("All Players").setValue(weeklyPlayers).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful())
                                        Toast.makeText(getContext(), "Adding Players To Weekly Section: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    database.getReference().child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String playerNamesInDataBase = snapshot.getValue(String.class);
                            for (String s : players) {
                                if (!playerNamesInDataBase.contains(s)) {
                                    IndividualRankings individualRankings = new IndividualRankings();
                                    individualRankings.setPlayerName(s);
                                    individualRankings.setTournamentLost("0");
                                    individualRankings.setTournamentPlayed("0");
                                    individualRankings.setTournamentWon("0");
                                    individualRankings.setWeeklyWins("0");
                                    database.getReference().child("Individual Rankings").child(s).setValue(individualRankings).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful())
                                                Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    playerNamesInDataBase += s + " ";
                                }
                            }
                            database.getReference().child("players").setValue(playerNamesInDataBase).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful())
                                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    List<String> allTeams = Arrays.asList(binding.teamList.getText().toString().split("\n"));
                    for (int i = 0; i < allTeams.size(); i++) {
                        TeamInfo team = new TeamInfo();
                        team.setGameLost("0");
                        team.setGamePlayed("0");
                        team.setWonAgainst("none");
                        team.setDefeted("none");
                        team.setHistory("");
                        team.setGameWon("0");
                        team.setTeamName(String.valueOf(allTeams.get(i)));
                        database.getReference().child("Games").child(gameName).child("Teams").child(team.getTeamName()).setValue(team).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful())
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Task Completed", Toast.LENGTH_SHORT).show();
                    TeamList.this.dismiss();
                }
                return true;
            }
        });


        return binding.getRoot();
    }

}
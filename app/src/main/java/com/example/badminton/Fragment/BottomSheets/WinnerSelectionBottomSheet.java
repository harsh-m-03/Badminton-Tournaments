package com.example.badminton.Fragment.BottomSheets;


import static android.os.ParcelFileDescriptor.MODE_APPEND;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.badminton.FcmNotificationsSender;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.TeamInfo;
import com.example.badminton.Modules.WeeklyRankings;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentWinnerSelectionBottomSheetBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WinnerSelectionBottomSheet extends BottomSheetDialogFragment {
    FragmentWinnerSelectionBottomSheetBinding binding;
    GestureDetector gestureDetector;
    ProgressDialog progressDialog;
    private MediaPlayer mediaPlayer;
    FirebaseDatabase database;
    private List<Double> pctList = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWinnerSelectionBottomSheetBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());

        String gameName = getActivity().getIntent().getStringExtra("Name");
        String team1 = getArguments().getString("team1", "");
        String team2 = getArguments().getString("team2", "");
        int position = 1 + getArguments().getInt("position", 0);
        binding.name1.setText(team1);
        binding.name2.setText(team2);
        String allPlayers = team1 + "-" + team2;
        String[] allPlayersList = allPlayers.split("-");
        for (String player : allPlayersList) {
            database.getReference().child("Weekly Status").child("Player Info").child(player).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint({"SetTextI18n", "ResourceAsColor", "DefaultLocale"})
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    WeeklyRankings weeklyRankings = snapshot.getValue(WeeklyRankings.class);
                    pctList.add(Double.parseDouble(weeklyRankings.getWinningPercentage()));
                    if (allPlayersList.length == pctList.size()) {
                        int noOfPlayersInTeam1 = 1;
                        int noOfPlayersInTeam2 = 1;
                        if (team1.contains("-")) noOfPlayersInTeam1++;
                        if (team2.contains("-")) noOfPlayersInTeam2++;

                        double team1Avg = 0;
                        double team2Avg = 0;

                        for (int i = 0; i < noOfPlayersInTeam1; i++) {
                            team1Avg += pctList.get(i);
                        }
                        for (int i = noOfPlayersInTeam1; i < pctList.size(); i++) {
                            team2Avg += pctList.get(i);
                        }
                        team1Avg /= noOfPlayersInTeam1;
                        team2Avg /= noOfPlayersInTeam2;

                        if (team1Avg > team2Avg) {
                            if (team1Avg < 50) {
                                team1Avg = 100 - team1Avg;
                            }
                            binding.team1Prediction.setTextColor(Color.parseColor("#07B30F"));//green
                            binding.team2Prediction.setTextColor(Color.parseColor("#E53935"));//red
                            @SuppressLint("DefaultLocale") String temp = String.format("%.2f", team1Avg);
                            binding.team1Prediction.setText(team1 + ": " + temp + "%");
                            temp = String.format("%.2f", (100 - team1Avg));
                            binding.team2Prediction.setText(team2 + ": " + temp + "%");
                        } else if (team2Avg > team1Avg) {
                            if (team2Avg < 50) {
                                team2Avg = 100 - team2Avg;
                            }
                            binding.team2Prediction.setTextColor(Color.parseColor("#07B30F"));//green
                            binding.team1Prediction.setTextColor(Color.parseColor("#E53935"));//red
                            String temp = String.format("%.2f", team2Avg);
                            binding.team2Prediction.setText(team2 + ": " + temp + "%");
                            temp = String.format("%.2f", (100 - team2Avg));
                            binding.team1Prediction.setText(team1 + ": " + temp + "%");

                        } else {
                            binding.team2Prediction.setText(team2 + ": 50%");
                            binding.team1Prediction.setText(team1 + ": 50%");
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        mediaPlayer = MediaPlayer.create(getContext(), R.raw.loud_alarm);
        binding.option1.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
//                    FirebaseMessaging.getInstance().subscribeToTopic("all");
//                    FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/all", gameName, team1 + " Won Game Number" + position, getActivity().getApplicationContext(), getActivity());
//                    notificationsSender.SendNotifications();
//                    mediaPlayer.start();
                    setWinner(gameName, team1);
                    setLosser(gameName, team2);
                    updateCurrentMatch(gameName);
                }
                return true;
            }
        });
        binding.option2.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
//                    FirebaseMessaging.getInstance().subscribeToTopic("all");
//                    FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/all", gameName, team2 + " Won Game Number " + position, getActivity().getApplicationContext(), getActivity());
//                    notificationsSender.SendNotifications();
//                    mediaPlayer.start();
                    setWinner(gameName, team2);
                    setLosser(gameName, team1);
                    updateCurrentMatch(gameName);
                }
                return true;
            }
        });

        return binding.getRoot();
    }

    public void updateCurrentMatch(String gameName) {
        database.getReference().child("Games").child(gameName).child("currentMatch").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String current = snapshot.getValue(String.class);
                current = String.valueOf(Integer.parseInt(current) + 1);
                database.getReference().child("Games").child(gameName).child("currentMatch").setValue(current).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            WinnerSelectionBottomSheet.this.dismiss();
                            Toast.makeText(getContext(), "Data Uploaded", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setLosser(String gameName, String teamName) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating...");
        database.getReference().child("Games").child(gameName).child("Teams").child(teamName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.show();
                TeamInfo teamInfo = snapshot.getValue(TeamInfo.class);
                teamInfo.getTeamName(snapshot.getKey());
                String history = teamInfo.getHistory();
                history += "L";
                teamInfo.setHistory(history);
                teamInfo.setGameLost(String.valueOf(Integer.parseInt(teamInfo.getGameLost()) + 1));
                teamInfo.setGamePlayed(String.valueOf(Integer.parseInt(teamInfo.getGamePlayed()) + 1));

                database.getReference().child("Games").child(gameName).child("Teams").child(teamName).setValue(teamInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (!task.isSuccessful())
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setWinner(String gameName, String teamName) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating...");
        database.getReference().child("Games").child(gameName).child("Teams").child(teamName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.show();
                TeamInfo teamInfo = snapshot.getValue(TeamInfo.class);
                teamInfo.getTeamName(snapshot.getKey());
                String history = teamInfo.getHistory();
                history += "W";
                teamInfo.setHistory(history);
                teamInfo.setGamePlayed(String.valueOf(Integer.parseInt(teamInfo.getGamePlayed()) + 1));
                teamInfo.setGameWon(String.valueOf(Integer.parseInt(teamInfo.getGameWon()) + 1));

                database.getReference().child("Games").child(gameName).child("Teams").child(teamName).setValue(teamInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (!task.isSuccessful())
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
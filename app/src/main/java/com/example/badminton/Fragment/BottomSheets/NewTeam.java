package com.example.badminton.Fragment.BottomSheets;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.badminton.Modules.IndividualRankings;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.TeamInfo;
import com.example.badminton.Modules.WeeklyRankings;
import com.example.badminton.databinding.FragmentNewTeamBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewTeam extends BottomSheetDialogFragment {
    FragmentNewTeamBinding binding;
    GestureDetector gestureDetector;
    ProgressDialog progressDialog;
    FirebaseDatabase database;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNewTeamBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating");

        binding.create.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (binding.teamName.getText().toString().isEmpty())
                        binding.teamName.setError("Required");
                    else if (binding.teamName.getText().toString().length() > 5)
                        binding.teamName.setError("Use Initials for Naming, length can't exceed 5");
                    else if (!binding.teamName.getText().toString().toUpperCase().equals(binding.teamName.getText().toString()))
                        binding.teamName.setError("UpperCase Only!");
                    else if (!binding.teamName.getText().toString().contains("-") && binding.teamName.getText().toString().length() != 2)
                        binding.teamName.setError("Please use Initials");
                    else {
                        progressDialog.show();
                        database.getReference().child("Weekly Status").child("All Players").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String allPlayers = snapshot.getValue(String.class);
                                String[] temp = binding.teamName.getText().toString().split("-");
                                for (String s : temp) {
                                    if (!allPlayers.contains(s)) {
                                        WeeklyRankings weeklyRankings = new WeeklyRankings();
                                        weeklyRankings.setPlayerName(s);
                                        weeklyRankings.setTournamentLost("0");
                                        weeklyRankings.setWinningPercentage("0");
                                        weeklyRankings.setTotalPoints("0");
                                        weeklyRankings.setPointsObtained("0");
                                        weeklyRankings.setTournamentPlayed("0");
                                        weeklyRankings.setTournamentWon("0");
                                        weeklyRankings.setRunnerUps(0);
                                        allPlayers += s + " ";

                                        database.getReference().child("Weekly Status").child("Player Info").child(s).setValue(weeklyRankings).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful())
                                                    Toast.makeText(getContext(), "Weekly: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                                database.getReference().child("Weekly Status").child("All Players").setValue(allPlayers).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                String playersInDataBase = snapshot.getValue(String.class);
                                String[] temp = binding.teamName.getText().toString().split("-");
                                for (String s : temp) {
                                    if (!playersInDataBase.contains(s)) {
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
                                                    Toast.makeText(getContext(), "OverAll: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        playersInDataBase += s + " ";
                                    }
                                }
                                database.getReference().child("players").setValue(playersInDataBase).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                        TeamInfo team = new TeamInfo();
                        team.setGameLost("0");
                        team.setGamePlayed("0");
                        team.setWonAgainst("none");
                        team.setDefeted("none");
                        team.setHistory("");
                        team.setGameWon("0");
                        team.setTeamName(binding.teamName.getText().toString());
                        String name = getActivity().getIntent().getStringExtra("Name");
                        database.getReference().child("Games").child(name).child("Teams").child(team.getTeamName()).setValue(team).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    NewTeam.this.dismiss();
                                    Toast.makeText(getContext(), "Team Created", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
                return true;
            }
        });

        return binding.getRoot();
    }
}
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
import com.example.badminton.databinding.FragmentRevertingChangesBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class RevertingChanges extends BottomSheetDialogFragment {
    FragmentRevertingChangesBinding binding;
    FirebaseDatabase database;
    GestureDetector gestureDetector;
    ProgressDialog progressDialog;
    FirebaseAuth auth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance();
        String gameName = getActivity().getIntent().getStringExtra("Name");
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());
        database = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Reverting Changes");
        binding = FragmentRevertingChangesBinding.inflate(inflater, container, false);
        binding.delete.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (binding.key.getText().toString().isEmpty())
                        binding.key.setError("Required");
                    else {
                        progressDialog.show();
                        database.getReference().child("Games").child(gameName).child("bonus").setValue("false");
                        auth.signInWithEmailAndPassword("mange.h@somaiya.edu", binding.key.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //Reverting Overall Stats
                                database.getReference().child("Games").child(gameName).child("Teams").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshotForPlayerName) {
                                        for (DataSnapshot snapshot1 : snapshotForPlayerName.getChildren()) {
                                            TeamInfo teamInfo = snapshot1.getValue(TeamInfo.class);
                                            for (String playerName : teamInfo.getTeamName().split("-")) {
                                                database.getReference().child("Games").child(gameName).child("winner").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshotForWinner) {
                                                        database.getReference().child("Individual Rankings").child(playerName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshotForIndividual) {
                                                                String winner = snapshotForWinner.getValue(String.class);
                                                                IndividualRankings individualRankings = snapshotForIndividual.getValue(IndividualRankings.class);
                                                                individualRankings.setTournamentPlayed(String.valueOf(Integer.parseInt(individualRankings.getTournamentPlayed()) - 1));

                                                                List<String> winnerList = Arrays.asList(winner.split("-"));

                                                                if (winnerList.contains(playerName)) {
                                                                    individualRankings.setTournamentWon(String.valueOf(Integer.parseInt(individualRankings.getTournamentWon()) - 1));
                                                                } else {
                                                                    individualRankings.setTournamentLost(String.valueOf(Integer.parseInt(individualRankings.getTournamentLost()) - 1));
                                                                }

                                                                database.getReference().child("Individual Rankings").child(playerName).setValue(individualRankings).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (!task.isSuccessful()) {
                                                                            Toast.makeText(getContext(), "Reverting OverAll Changes" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });

                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                //Reverting Weekly Stats
                                database.getReference().child("Games").child(gameName).child("Teams").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot allTeamSnapShot) {
                                        for (DataSnapshot teamSnap : allTeamSnapShot.getChildren()) {
                                            TeamInfo teamInfo = teamSnap.getValue(TeamInfo.class);

                                            List<String> playerNameList = Arrays.asList(teamInfo.getTeamName().split("-"));

                                            for (String player : playerNameList) {
                                                database.getReference().child("Stats Backup").child("Players").child("Weekly Backup").child(player).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot backupTeamSnap) {
                                                        WeeklyRankings weeklyRankings = backupTeamSnap.getValue(WeeklyRankings.class);

                                                        database.getReference().child("Weekly Status").child("Player Info").child(weeklyRankings.getPlayerName()).setValue(weeklyRankings).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    //Reverting Winners
                                                                    database.getReference().child("Games").child(gameName).child("winner").setValue("none").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (!task.isSuccessful()) {
                                                                                Toast.makeText(getContext(), "Unable to Revert WinnerName", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });

                                                                    //Reverting Runner-Ups
                                                                    database.getReference().child("Games").child(gameName).child("runner-ups").setValue("none").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (!task.isSuccessful()) {
                                                                                Toast.makeText(getContext(), "Unable to Revert Runner-up Names", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });

                                                                    //Removing Stats Backup Data
                                                                    database.getReference().child("Stats Backup").child("gameName").setValue("none").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                database.getReference().child("Stats Backup").child("Players").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (!task.isSuccessful())
                                                                                            Toast.makeText(getContext(), "Erasing Backup Players: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                            } else
                                                                                Toast.makeText(getContext(), "Adding Backup GameName: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

                                                                } else
                                                                    Toast.makeText(getContext(), "Failed to Fetch Data for Player", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                //Decrement Total Tournaments
                                database.getReference("Total Tournament").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String total = snapshot.getValue(String.class);

                                        String newTotal = String.valueOf(Integer.parseInt(total) - 1);

                                        database.getReference().child("Total Tournament").setValue(newTotal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressDialog.dismiss();
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getContext(), "Changes Reverted", Toast.LENGTH_SHORT).show();
                                                    AddWinnerBottomSheet frag = new AddWinnerBottomSheet();
                                                    frag.show(getActivity().getSupportFragmentManager(), frag.getTag());
                                                    RevertingChanges.this.dismiss();
                                                } else {
                                                    Toast.makeText(getContext(), "Unable to decrease Total Tournaments", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

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
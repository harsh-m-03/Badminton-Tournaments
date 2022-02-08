package com.example.badminton.Fragment.BottomSheets;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.badminton.FcmNotificationsSender;
import com.example.badminton.Fragment.RankingFragments.RankingForWeekly;
import com.example.badminton.Individual;
import com.example.badminton.MainActivity;
import com.example.badminton.Modules.IndividualRankings;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.WeeklyRankings;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentAddWeeklyWinnerBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class AddWeeklyWinner extends BottomSheetDialogFragment {
    FragmentAddWeeklyWinnerBinding binding;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    GestureDetector gestureDetector;
    FirebaseDatabase database;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddWeeklyWinnerBinding.inflate(inflater, container, false);
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating Winner");
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.add.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (auth.getCurrentUser() != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(true);
                        builder.setMessage("Are you sure?\nWeekly Stats will be erased.");
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                database.getReference().child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String playerList = snapshot.getValue(String.class);

                                        if (!playerList.contains(binding.winner.getText().toString())) {
                                            binding.winner.setError("Doesn't Exist");
                                        } else if (binding.winner.getText().toString().isEmpty())
                                            binding.winner.setError("Enter a Name");
                                        else {

                                            FirebaseMessaging.getInstance().subscribeToTopic("all");
                                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/all", "Congratulations", "Winner for this Week is " + binding.winner.getText().toString(), getActivity().getApplicationContext(), getActivity());
                                            notificationsSender.SendNotifications();

                                            progressDialog.show();

                                            database.getReference().child("Weekly Status").child("Player Info").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                        WeeklyRankings weeklyRankings = dataSnapshot.getValue(WeeklyRankings.class);
                                                        weeklyRankings.setTournamentLost("0");
                                                        weeklyRankings.setWinningPercentage("0");
                                                        weeklyRankings.setTotalPoints("0");
                                                        weeklyRankings.setPointsObtained("0");
                                                        weeklyRankings.setRunnerUps(0);
                                                        weeklyRankings.setTournamentPlayed("0");
                                                        weeklyRankings.setTournamentWon("0");

                                                        database.getReference().child("Weekly Status").child("Player Info").child(weeklyRankings.getPlayerName()).setValue(weeklyRankings).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                } else
                                                                    Toast.makeText(getContext(), "Weekly: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                            database.getReference().child("Weekly Status").child("Current Week").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    int current = snapshot.getValue(Integer.class);
                                                    database.getReference().child("Weekly Status").child("History").child("Week " + current).setValue(binding.winner.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                database.getReference().child("Weekly Status").child("Current Week").setValue(current + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (!task.isSuccessful())
                                                                            Toast.makeText(getContext(), "Failed to Update Current Week", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            } else {
                                                                Toast.makeText(getContext(), "Failed to Update History and Week Status", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                            database.getReference().child("Individual Rankings").child(binding.winner.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    IndividualRankings individualRankings = snapshot.getValue(IndividualRankings.class);
                                                    individualRankings.setWeeklyWins(String.valueOf(1 + Integer.parseInt(individualRankings.getWeeklyWins())));
                                                    database.getReference().child("Individual Rankings").child(binding.winner.getText().toString()).setValue(individualRankings).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            progressDialog.dismiss();
                                                            if (!task.isSuccessful())
                                                                Toast.makeText(getContext(), "Setting Winner: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            else {
                                                                AlertDialog.Builder builderForDeletingHistory = new AlertDialog.Builder(getContext());
                                                                builderForDeletingHistory.setCancelable(false);
                                                                builderForDeletingHistory.setMessage("Do you want to Delete the All Game's History?");
                                                                builderForDeletingHistory.setPositiveButton("Yes",
                                                                        new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                database.getReference().child("Games").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (!task.isSuccessful()) {
                                                                                            Toast.makeText(getContext(), "Fail to delete Game Data ", Toast.LENGTH_SHORT).show();
                                                                                        } else if (task.isSuccessful()) {
                                                                                            Intent intent = new Intent(getActivity(), Individual.class);
                                                                                            startActivity(intent);
                                                                                            AddWeeklyWinner.this.dismiss();
                                                                                            Toast.makeText(getContext(), "Winner Added", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                builderForDeletingHistory.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        Intent intent = new Intent(getActivity(), Individual.class);
                                                                        startActivity(intent);
                                                                        AddWeeklyWinner.this.dismiss();
                                                                        Toast.makeText(getContext(), "Winner Added", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                                AlertDialog dialog = builderForDeletingHistory.create();
                                                                dialog.show();
                                                            }
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });


                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else
                        Toast.makeText(getContext(), "You are not the Admin", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


        return binding.getRoot();
    }
}
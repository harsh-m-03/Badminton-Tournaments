package com.example.badminton.Fragment.BottomSheets;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.badminton.MainActivity;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.TeamInfo;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentTeamInfoBottomSheetBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class TeamInfoBottomSheet extends BottomSheetDialogFragment {
    FragmentTeamInfoBottomSheetBinding binding;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    GestureDetector gestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTeamInfoBottomSheetBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating...");
        database = FirebaseDatabase.getInstance();
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());

        String gameName = getActivity().getIntent().getStringExtra("Name");
        String teamName = getArguments().getString("Name", "");

        database.getReference().child("Games").child(gameName).child("Teams").child(teamName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TeamInfo teamInfo = snapshot.getValue(TeamInfo.class);
                teamInfo.getTeamName(snapshot.getKey());
                binding.teamName.setText(teamInfo.getTeamName());
                binding.gameWon.setText("Won: " + teamInfo.getGameWon());
                binding.gameLost.setText("Lost: " + teamInfo.getGameLost());
                binding.gamePlayed.setText("Played: " + teamInfo.getGamePlayed());
                binding.defeatAgainst.setText("Defeated By: " + teamInfo.getDefeted());
                binding.winners.setText("Won Against: " + teamInfo.getWonAgainst());
                binding.pastGame.setText("History: " + teamInfo.getHistory());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.won.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (auth.getCurrentUser() == null) {
                        Toast.makeText(getContext(), "You are not the Admin", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.show();
                        database.getReference().child("Games").child(gameName).child("Teams").child(teamName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                TeamInfo teamInfo = snapshot.getValue(TeamInfo.class);
                                teamInfo.getTeamName(snapshot.getKey());
                                teamInfo.setGameWon(String.valueOf(Integer.parseInt(teamInfo.getGameWon()) + 1));
                                teamInfo.setGamePlayed(String.valueOf(Integer.parseInt(teamInfo.getGamePlayed()) + 1));
                                String history = teamInfo.getHistory();
                                history += "W";
                                teamInfo.setHistory(history);
                                database.getReference().child("Games").child(gameName).child("Teams").child(teamName).setValue(teamInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        if (task.isComplete()) {
                                            Toast.makeText(getContext(), "Data Updated", Toast.LENGTH_SHORT).show();
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
                }
                return true;
            }
        });
        binding.loss.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (auth.getCurrentUser() == null) {
                        Toast.makeText(getContext(), "You are not the Admin", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.show();
                        database.getReference().child("Games").child(gameName).child("Teams").child(teamName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                TeamInfo teamInfo = snapshot.getValue(TeamInfo.class);
                                teamInfo.getTeamName(snapshot.getKey());
                                teamInfo.setGameLost(String.valueOf(Integer.parseInt(teamInfo.getGameLost()) + 1));
                                teamInfo.setGamePlayed(String.valueOf(Integer.parseInt(teamInfo.getGamePlayed()) + 1));
                                String history = teamInfo.getHistory();
                                history += "L";
                                teamInfo.setHistory(history);
                                database.getReference().child("Games").child(gameName).child("Teams").child(teamName).setValue(teamInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        if (task.isComplete()) {
                                            Toast.makeText(getContext(), "Data Updated", Toast.LENGTH_SHORT).show();
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
                }

                return true;
            }
        });

        return binding.getRoot();
    }
}
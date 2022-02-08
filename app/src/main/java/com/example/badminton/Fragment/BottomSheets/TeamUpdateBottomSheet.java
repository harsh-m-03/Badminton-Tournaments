package com.example.badminton.Fragment.BottomSheets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.badminton.InGamePage;
import com.example.badminton.MainActivity;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.TeamInfo;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentTeamUpdateBottomSheetBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class TeamUpdateBottomSheet extends BottomSheetDialogFragment {
    FragmentTeamUpdateBottomSheetBinding binding;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    GestureDetector gestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTeamUpdateBottomSheetBinding.inflate(inflater, container, false);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating...");
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());
        database = FirebaseDatabase.getInstance();

        String gameName = getActivity().getIntent().getStringExtra("Name");
        String teamName = getArguments().getString("Name", "");

        database.getReference().child("Games").child(gameName).child("Teams").child(teamName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TeamInfo teamInfo = snapshot.getValue(TeamInfo.class);
                teamInfo.getTeamName(snapshot.getKey());
                binding.teamName.setText("TeamName: " + teamInfo.getTeamName());
                binding.gamesPlayed.setText(teamInfo.getGamePlayed());
                binding.gameLost.setText(teamInfo.getGameLost());
                binding.gameWon.setText(teamInfo.getGameWon());
                binding.history.setText(teamInfo.getHistory());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.update.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (binding.gamesPlayed.getText().toString().isEmpty())
                        binding.gamesPlayed.setError("Required");
                    else if (binding.gameWon.getText().toString().isEmpty())
                        binding.gameWon.setError("Required");
                    else if (binding.gameLost.getText().toString().isEmpty())
                        binding.gameLost.setError("Required");
                    else if (binding.history.getText().toString().isEmpty())
                        binding.history.setError("Required");
                    else {
                        progressDialog.show();
                        database.getReference().child("Games").child(gameName).child("Teams").child(teamName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                TeamInfo teamInfo = snapshot.getValue(TeamInfo.class);
                                teamInfo.getTeamName(snapshot.getKey());
                                teamInfo.setHistory(binding.history.getText().toString());
                                teamInfo.setGameLost(binding.gameLost.getText().toString());
                                teamInfo.setGameWon(binding.gameWon.getText().toString());
                                teamInfo.setGamePlayed(binding.gamesPlayed.getText().toString());
                                database.getReference().child("Games").child(gameName).child("Teams").child(teamName).setValue(teamInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        if (task.isComplete()) {
                                            TeamUpdateBottomSheet.this.dismiss();
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
        binding.delete.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Confirmation");
                    builder.setMessage("Are you sure you want to delete current Item?");
                    builder.setPositiveButton("Delete",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    progressDialog.show();
                                    database.getReference().child("Games").child(gameName).child("Teams").child(teamName).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Intent intent = new Intent(getContext(), InGamePage.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                TeamUpdateBottomSheet.this.dismiss();
                                                startActivity(intent);
                                                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                            } else
                                                Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            }
        });

        return binding.getRoot();
    }
}
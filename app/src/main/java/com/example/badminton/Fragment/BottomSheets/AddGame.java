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

import com.example.badminton.Modules.NewGame;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.databinding.FragmentAddGameBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class AddGame extends BottomSheetDialogFragment {
    FragmentAddGameBinding binding;
    GestureDetector gestureDetector;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddGameBinding.inflate(inflater, container, false);
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());
        database = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating");

        binding.gameName.setText(String.valueOf(Calendar.getInstance().getTime()).substring(0, 20));

        binding.create.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (binding.gameName.getText().toString().isEmpty())
                        binding.gameName.setError("Required");
                    else if (binding.rounds.getText().toString().isEmpty())
                        binding.rounds.setError("Required");
                    else if (binding.rounds.getText().toString().equals("0") || binding.rounds.getText().toString().contains(" ") || binding.rounds.getText().toString().length() != 1)
                        binding.rounds.setError("Enter a valid Number");
                    else if (5 < Integer.parseInt(binding.rounds.getText().toString()))
                        binding.rounds.setError("Value Can't be greater than 5");
                    else {
                        String temp = binding.rounds.getText().toString();
                        if (!Character.isDigit(temp.charAt(0)))
                            binding.rounds.setError("Enter a Valid Number");
                        else {
                            progressDialog.show();
                            NewGame game = new NewGame();
                            game.setCurrentMatch("0");
                            game.setWinner("none");
                            game.setName(binding.gameName.getText().toString());
                            game.setRounds(binding.rounds.getText().toString());
                            game.setComputerDate(String.valueOf(new Date().getTime()));
                            game.setSchedule("none");
                            game.setBonus("false");
                            game.setDate(String.valueOf(Calendar.getInstance().getTime()).substring(0, 20));

                            database.getReference().child("Games").child(game.getName()).setValue(game).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        binding.rounds.setText("");
                                        binding.gameName.setText("");
                                        AddGame.this.dismiss();
                                        Toast.makeText(getContext(), "Game Created Successfully", Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
                return true;
            }
        });
        return binding.getRoot();
    }
}
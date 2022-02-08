package com.example.badminton.Fragment.InGameFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.badminton.Fragment.BottomSheets.AddWinnerBottomSheet;
import com.example.badminton.Fragment.BottomSheets.RevertingChanges;
import com.example.badminton.Fragment.BottomSheets.ShowingWinners;
import com.example.badminton.Fragment.BottomSheets.TeamUpdateBottomSheet;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.TeamInfo;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentTableBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;

public class Table extends Fragment {

    FirebaseDatabase database;
    GestureDetector gestureDetector;
    FirebaseAuth auth;
    FragmentTableBinding binding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTableBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());

        List<TeamInfo> tableDisplay = new ArrayList<>();
        String gameName = getActivity().getIntent().getStringExtra("Name");

        database.getReference().child("Games").child(gameName).child("Teams").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    TeamInfo temp = s.getValue(TeamInfo.class);
                    temp.getTeamName(s.getKey());
                    tableDisplay.add(temp);
                }
                Collections.sort(tableDisplay, new SortByWins());
                String srNo = "Rank\n\n", teamName = "TeamName\n\n", gamePlayed = "Played\n\n", gameWon = "Won\n\n", gameLost = "Lost\n\n";
                int i = 1;
                for (TeamInfo t : tableDisplay) {
                    srNo += String.valueOf(i++) + "\n";
                    teamName += t.getTeamName() + "\n";
                    gamePlayed += t.getGamePlayed() + "\n";
                    gameLost += t.getGameLost() + "\n";
                    gameWon += t.getGameWon() + "\n";
                }
                binding.srNo.setText(srNo);
                binding.teamName.setText(teamName);
                binding.gamePlayed.setText(gamePlayed);
                binding.gameWon.setText(gameWon);
                binding.gameLost.setText(gameLost);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.add.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    database.getReference().child("Games").child(gameName).child("winner").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String winner = snapshot.getValue(String.class);
                            if (winner.equals("none")) {
                                if (auth.getCurrentUser() != null)
                                    new AddWinnerBottomSheet().show(getActivity().getSupportFragmentManager(), new AddWinnerBottomSheet().getTag());
                                else {
                                    Toast.makeText(getContext(), "You are not the Admin", Toast.LENGTH_SHORT).show();
                                }
                            } else
                                new ShowingWinners().show(getActivity().getSupportFragmentManager(), new ShowingWinners().getTag());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                return true;
            }
        });

        if (auth.getCurrentUser() != null) {
            binding.hiddenButton.setVisibility(View.VISIBLE);
        } else binding.hiddenButton.setVisibility(View.INVISIBLE);

        binding.hiddenButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                database.getReference().child("Games").child(gameName).child("winner").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        database.getReference().child("Stats Backup").child("gameName").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot accessSnapshot) {
                                String access = accessSnapshot.getValue(String.class);
                                String winner = snapshot.getValue(String.class);
                                if (winner.equals("none"))
                                    Toast.makeText(getContext(), "Winners are not added yet", Toast.LENGTH_SHORT).show();
                                else if (!access.equals(gameName))
                                    Toast.makeText(getContext(), "Session Expired you can't revert changes now", Toast.LENGTH_SHORT).show();
                                else {
                                    new RevertingChanges().show(getActivity().getSupportFragmentManager(), new RevertingChanges().getTag());
                                }
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
                return false;
            }
        });

        return binding.getRoot();
    }

    public class SortByWins implements Comparator {

        public int compare(Object o1, Object o2) {
            TeamInfo p1 = (TeamInfo) o1;
            TeamInfo p2 = (TeamInfo) o2;
            return Integer.parseInt(p2.getGameWon()) - Integer.parseInt(p1.getGameWon());
        }
    }
}
package com.example.badminton.Fragment.BottomSheets;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.badminton.FcmNotificationsSender;
import com.example.badminton.MainActivity;
import com.example.badminton.Modules.IndividualRankings;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.TeamInfo;
import com.example.badminton.Modules.WeeklyRankings;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentAddWinnerBottomSheetBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AddWinnerBottomSheet extends BottomSheetDialogFragment {

    FragmentAddWinnerBottomSheetBinding binding;
    ProgressDialog progressDialog;
    FirebaseDatabase database;
    GestureDetector gestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddWinnerBottomSheetBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating...");
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());

        String gameName = getActivity().getIntent().getStringExtra("Name");

        binding.switch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.switch1.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(false);
                    builder.setMessage("\"Double\" the points for this tournament? ");
                    builder.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    binding.switch1.setChecked(false);
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });


        binding.add.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (binding.winner.getText().toString().isEmpty())
                        binding.winner.setError("Required");
                    else if ((!binding.winner.getText().toString().contains("-") && binding.winner.getText().toString().length() == 5) || binding.winner.getText().toString().length() > 5)
                        binding.winner.setError("Please use Initials");
                    else if (binding.winner.getText().toString().length() != 5 && binding.winner.getText().toString().length() != 2) {
                        binding.winner.setError("Please use Initials");
                    } else if (binding.runnerUp.getText().toString().isEmpty())
                        binding.runnerUp.setError("Required");
                    else if ((!binding.runnerUp.getText().toString().contains("-") && binding.runnerUp.getText().toString().length() == 5) || binding.runnerUp.getText().toString().length() > 5)
                        binding.runnerUp.setError("Please use Initials");
                    else if (binding.runnerUp.getText().toString().length() != 5 && binding.runnerUp.getText().toString().length() != 2) {
                        binding.runnerUp.setError("Please use Initials");
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(true);
                        builder.setMessage("Are you sure?");
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        progressDialog.show();
                                        if (binding.switch1.isChecked())
                                            database.getReference().child("Games").child(gameName).child("bonus").setValue("true");
                                        else
                                            database.getReference().child("Games").child(gameName).child("bonus").setValue("false");

                                        database.getReference().child("Total Tournament").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot totalPlayed) {
                                                String allTotal = totalPlayed.getValue(String.class);
                                                allTotal = String.valueOf(Integer.parseInt(allTotal) + 1);
                                                database.getReference().child("Total Tournament").setValue(allTotal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (!task.isSuccessful()) {
                                                            Toast.makeText(getContext(), "Total Tournaments: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        database.getReference().child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapShorForPlayerList) {
                                                String playerListFromDataBase = snapShorForPlayerList.getValue(String.class);

                                                boolean checkingPlayers = true;
                                                boolean checkingRunnerUps = true;
                                                boolean checkingWinners = true;
                                                String[] winnerNameArray = binding.winner.getText().toString().split("-");
                                                String[] runnerUpNameArray = binding.runnerUp.getText().toString().split("-");

                                                for (String s : winnerNameArray) {
                                                    if (!playerListFromDataBase.contains(s)) {
                                                        checkingPlayers = false;
                                                        checkingWinners = false;
                                                        break;
                                                    }
                                                }
                                                for (String s : runnerUpNameArray) {
                                                    if (!playerListFromDataBase.contains(s)) {
                                                        checkingRunnerUps = false;
                                                        checkingPlayers = false;
                                                        break;
                                                    }
                                                }

                                                if (!checkingWinners) {
                                                    binding.winner.setError("Players doesn't exist");
                                                } else if (!checkingRunnerUps) {
                                                    binding.runnerUp.setError("Players doesn't exist");
                                                } else {
                                                    database.getReference().child("Games").child(gameName).child("Teams").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapShotForTeamKeChildren) {
                                                            String teamsPlayingThisTournament = "";
                                                            for (DataSnapshot snapShotForTeams : snapShotForTeamKeChildren.getChildren()) {
                                                                TeamInfo teamInfo = snapShotForTeams.getValue(TeamInfo.class);
                                                                String[] tempArray = teamInfo.getTeamName().split("-");
                                                                for (String tempPlayerName : tempArray)
                                                                    teamsPlayingThisTournament += tempPlayerName + " ";

                                                            }

                                                            String[] playersPlayingThisTournamentArray = teamsPlayingThisTournament.split(" ");
                                                            for (String individualNamesFromArray : playersPlayingThisTournamentArray) {
                                                                //Updating Weekly Scores
                                                                database.getReference().child("Games").child(gameName).child("Teams").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapShotForGettingTeamForWeeklyUpdate) {
                                                                        for (DataSnapshot teamSnapWeekly : snapShotForGettingTeamForWeeklyUpdate.getChildren()) {
                                                                            TeamInfo teamInfoForWeekly = teamSnapWeekly.getValue(TeamInfo.class);
                                                                            String[] teamNameForIndividual = teamInfoForWeekly.getTeamName().split("-");
                                                                            for (String playerNamesForWeekly : teamNameForIndividual) {
                                                                                database.getReference().child("Weekly Status").child("Player Info").child(playerNamesForWeekly).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot snapShotForWeeklyUpdate) {
                                                                                        WeeklyRankings weeklyRankings = snapShotForWeeklyUpdate.getValue(WeeklyRankings.class);

                                                                                        database.getReference().child("Stats Backup").child("Players").child("Weekly Backup").child(weeklyRankings.getPlayerName()).setValue(weeklyRankings).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {

                                                                                                } else {
                                                                                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                        String runnerUps = binding.runnerUp.getText().toString();
                                                                                        if (runnerUps.contains(weeklyRankings.getPlayerName())) {
                                                                                            weeklyRankings.setRunnerUps(weeklyRankings.getRunnerUps() + 1);
                                                                                        }
                                                                                        int totalPoints = 3 + Integer.parseInt(teamInfoForWeekly.getGamePlayed());
                                                                                        weeklyRankings.setTournamentPlayed(String.valueOf(1 + Integer.parseInt(weeklyRankings.getTournamentPlayed())));
                                                                                        weeklyRankings.setTotalPoints(String.valueOf(totalPoints + Integer.parseInt(weeklyRankings.getTotalPoints())));
                                                                                        if (binding.winner.getText().toString().contains(playerNamesForWeekly)) {
                                                                                            weeklyRankings.setPointsObtained(String.valueOf(3 + Integer.parseInt(weeklyRankings.getPointsObtained()) + Integer.parseInt(teamInfoForWeekly.getGameWon())));
                                                                                            weeklyRankings.setTournamentWon(String.valueOf(1 + Integer.parseInt(weeklyRankings.getTournamentWon())));
                                                                                        } else if (binding.runnerUp.getText().toString().contains(playerNamesForWeekly)) {
                                                                                            weeklyRankings.setPointsObtained(String.valueOf(2 + Integer.parseInt(weeklyRankings.getPointsObtained()) + Integer.parseInt(teamInfoForWeekly.getGameWon())));
                                                                                            weeklyRankings.setTournamentLost(String.valueOf(1 + Integer.parseInt(weeklyRankings.getTournamentLost())));
                                                                                        } else {
                                                                                            weeklyRankings.setPointsObtained(String.valueOf(Integer.parseInt(weeklyRankings.getPointsObtained()) + Integer.parseInt(teamInfoForWeekly.getGameWon())));
                                                                                            weeklyRankings.setTournamentLost(String.valueOf(1 + Integer.parseInt(weeklyRankings.getTournamentLost())));
                                                                                        }

                                                                                        if (binding.switch1.isChecked()) {
                                                                                            int doublePoints = 2;
                                                                                            weeklyRankings.setPointsObtained(String.valueOf(doublePoints * Integer.parseInt(weeklyRankings.getPointsObtained())));
                                                                                            weeklyRankings.setTotalPoints(String.valueOf(doublePoints * Integer.parseInt(weeklyRankings.getTotalPoints())));
                                                                                        }


                                                                                        float pct = Integer.parseInt(weeklyRankings.getPointsObtained());
                                                                                        pct /= Integer.parseInt(weeklyRankings.getTotalPoints());
                                                                                        pct *= 100;
                                                                                        weeklyRankings.setWinningPercentage((String.valueOf(pct) + "00000").substring(0, 5));

                                                                                        database.getReference().child("Weekly Status").child("Player Info").child(weeklyRankings.getPlayerName()).setValue(weeklyRankings).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (!task.isSuccessful()) {
                                                                                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });

                                                                //updating Overall Scores
                                                                database.getReference().child("Individual Rankings").child(individualNamesFromArray).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapShotForIndividual) {
                                                                        IndividualRankings individualRankings = snapShotForIndividual.getValue(IndividualRankings.class);
                                                                        individualRankings.setTournamentPlayed(String.valueOf(1 + Integer.parseInt(individualRankings.getTournamentPlayed())));
                                                                        if (binding.winner.getText().toString().contains(individualNamesFromArray)) {
                                                                            individualRankings.setTournamentWon(String.valueOf(1 + Integer.parseInt(individualRankings.getTournamentWon())));
                                                                        } else {
                                                                            individualRankings.setTournamentLost(String.valueOf(1 + Integer.parseInt(individualRankings.getTournamentLost())));
                                                                        }

                                                                        database.getReference().child("Individual Rankings").child(individualNamesFromArray).setValue(individualRankings).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                            }

                                                            FirebaseMessaging.getInstance().subscribeToTopic("all");
                                                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/all", "Tournament: " + gameName, binding.winner.getText().toString() + " won the Tournament\n" + binding.runnerUp.getText().toString() + " are the runner-ups", getActivity().getApplicationContext(), getActivity());
                                                            notificationsSender.SendNotifications();


                                                            //updating winners
                                                            database.getReference().child("Games").child(gameName).child("winner").setValue(binding.winner.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (!task.isSuccessful())
                                                                        Toast.makeText(getContext(), "Adding Winners: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });

                                                            database.getReference().child("Stats Backup").child("gameName").setValue(gameName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (!task.isSuccessful())
                                                                        Toast.makeText(getContext(), "Adding Backup GameName: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });

                                                            database.getReference().child("Games").child(gameName).child("runner-ups").setValue(binding.runnerUp.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    progressDialog.dismiss();
                                                                    if (task.isSuccessful()) {
//                                                                        Toast.makeText(getContext(), "Data Updated", Toast.LENGTH_SHORT).show();
                                                                        AddWinnerBottomSheet.this.dismiss();
                                                                        ShowingWinners frag = new ShowingWinners();
                                                                        frag.show(getActivity().getSupportFragmentManager(), frag.getTag());
                                                                    } else
                                                                        Toast.makeText(getContext(), "Adding RunnerUps: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

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

                    }
                }
                return true;
            }
        });


        return binding.getRoot();
    }
}
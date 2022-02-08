package com.example.badminton.Fragment.InGameFragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.badminton.Adapter.TempMatchesAdapter;
import com.example.badminton.Fragment.BottomSheets.NewTeam;
import com.example.badminton.Fragment.BottomSheets.TeamInfoBottomSheet;
import com.example.badminton.Fragment.BottomSheets.WinnerSelectionBottomSheet;
import com.example.badminton.MainActivity;
import com.example.badminton.Modules.NewGame;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.RecyclerItemClickListener;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.TeamInfo;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentMatchesBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Matches extends Fragment {
    FragmentMatchesBinding binding;
    ArrayList<String> allMatches = new ArrayList<>();
    ArrayList<String> schedule = new ArrayList<>();
    GestureDetector gestureDetector;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMatchesBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());

        TempMatchesAdapter adapter = new TempMatchesAdapter(schedule, getContext());
        binding.recycleView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recycleView.setLayoutManager(layoutManager);
        database = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating");

        binding.send.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (allMatches.isEmpty())
                        Toast.makeText(getContext(), "No Matches Found", Toast.LENGTH_SHORT).show();
                    else {
                        StringBuilder msgToSend = new StringBuilder();
                        int i = 1;
                        for (String s : schedule)
                            msgToSend.append(i++).append(". ").append(s).append("\n");
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, msgToSend.toString());
                        sendIntent.setPackage("com.whatsapp");
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);
                    }
                }
                return true;
            }
        });

        ArrayList<String> tempName = new ArrayList<>();
        String name = getActivity().getIntent().getStringExtra("Name");

        database.getReference().child("Games").child(name).child("Teams").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allMatches.clear();
                for (DataSnapshot sp : snapshot.getChildren()) {
                    TeamInfo currentTeam = sp.getValue(TeamInfo.class);
                    currentTeam.getTeamName(sp.getKey());
                    tempName.add(currentTeam.getTeamName());
                }
                String[] test = new String[tempName.size()];
                for (int i = 0; i < tempName.size(); i++)
                    test[i] = tempName.get(i);
                printCombination(test, test.length, 2);

                int randomNumber = (int) (Math.random() * (25 - 5 + 1) + 5);
                while (randomNumber-- > 0) {
                    Collections.shuffle(allMatches);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Games").child(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                NewGame game = snapshot.getValue(NewGame.class);
                game.getName(snapshot.getKey());
                schedule.clear();
                if (game.getSchedule().equals("none")) {
                    binding.animationView.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Create Match-ups", Toast.LENGTH_SHORT).show();
                } else {
                    binding.animationView.setVisibility(View.GONE);
                    String temp = "";
                    for (int i = 0; i < game.getSchedule().length(); i++) {
                        if (game.getSchedule().charAt(i) == '\n') {
                            schedule.add(temp);
                            temp = "";
                        } else {
                            temp += game.getSchedule().charAt(i);
                        }
                    }
                    adapter.setGameName(name);
                    adapter.notifyDataSetChanged();
                }
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
                    if (auth.getCurrentUser() == null) {
                        Toast.makeText(getContext(), "You are not the Admin", Toast.LENGTH_SHORT).show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(true);
                        builder.setMessage("Are you sure you want to refresh the Schedule?");
                        builder.setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (allMatches.size() == 0)
                                            Toast.makeText(getContext(), "Please Create some Teams", Toast.LENGTH_SHORT).show();
                                        else {
                                            progressDialog.show();
                                            database.getReference().child("Games").child(name).child("currentMatch").setValue("0").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    progressDialog.dismiss();
                                                    if (!task.isSuccessful())
                                                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            int randomNumber = (int) (Math.random() * (100 - 5 + 1) + 5);
                                            while (randomNumber-- > 0) {
                                                Collections.shuffle(allMatches);
                                            }
                                            progressDialog.show();
                                            StringBuilder toUpload = new StringBuilder();
                                            for (String s : allMatches)
                                                toUpload.append(s).append("\n");
                                            database.getReference().child("Games").child(name).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    NewGame game = snapshot.getValue(NewGame.class);
                                                    game.getName(snapshot.getKey());
                                                    game.setSchedule(toUpload.toString());

                                                    database.getReference().child("Games").child(name).child("schedule").setValue(game.getSchedule()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            progressDialog.dismiss();
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getContext(), "Task Completed", Toast.LENGTH_SHORT).show();
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
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
                return true;
            }
        });

        binding.recycleView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), binding.recycleView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (auth.getCurrentUser() == null) {
                            Toast.makeText(getContext(), "You are not the Admin", Toast.LENGTH_SHORT).show();
                        } else {
                            database.getReference().child("Games").child(name).child("currentMatch").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    String cur = snap.getValue(String.class);
                                    if (Integer.parseInt(cur) >= schedule.size()) {
                                        Toast.makeText(getContext(), "Tournament Ended", Toast.LENGTH_SHORT).show();
                                    } else if (Integer.parseInt(cur) != position) {
                                        Toast.makeText(getContext(), "Please Select Match Number: " + (Integer.parseInt(cur) + 1), Toast.LENGTH_SHORT).show();
                                    } else {
                                        String[] names = schedule.get(position).split(" vs ");
                                        WinnerSelectionBottomSheet frag = new WinnerSelectionBottomSheet();
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("position",position);
                                        bundle.putString("team1", names[0]);
                                        bundle.putString("team2", names[1].substring(0, names[1].length() - 1));
                                        frag.setArguments(bundle);
                                        frag.show(getActivity().getSupportFragmentManager(), frag.getTag());
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onLongItemClick(View vierw, int position) {

                    }
                })
        );

        return binding.getRoot();
    }

    void combinationUtil(String arr[], int n, int r, int index,
                         String data[], int i) {
        if (index == r) {
            StringBuilder cur = new StringBuilder();
            for (int j = 0; j < r; j++)
                cur.append(data[j]).append(" vs ");
            String round = getActivity().getIntent().getStringExtra("Rounds");
            for (int k = 0; k < Integer.parseInt(round); k++)
                allMatches.add(cur.substring(0, cur.length() - 3));
            return;
        }

        if (i >= n)
            return;

        data[index] = arr[i];
        combinationUtil(arr, n, r, index + 1, data, i + 1);
        combinationUtil(arr, n, r, index, data, i + 1);
    }

    void printCombination(String arr[], int n, int r) {
        String data[] = new String[r];
        combinationUtil(arr, n, r, 0, data, 0);
    }
}
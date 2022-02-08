package com.example.badminton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.badminton.Adapter.GameAdapter;
import com.example.badminton.Fragment.BottomSheets.AddGame;
import com.example.badminton.Fragment.BottomSheets.DeleteAGame;
import com.example.badminton.Modules.NewGame;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.RecyclerItemClickListener;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    GestureDetector gestureDetector;
    ArrayList<NewGame> list = new ArrayList<>();
    FirebaseDatabase database;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Deleting");
        database = FirebaseDatabase.getInstance();
        gestureDetector = new GestureDetector(this, new SingleTapClick());
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding.shimmerLayout.startShimmer();

        database.getReference("update").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                //4.3
                if (!value.equals("5.1")) {
                    auth.signOut();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("Version Update");
                    builder.setMessage("Contact the owner for the update");
                    builder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAffinity();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("sponsor").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String sponsorName = snapshot.getValue(String.class);
                binding.sponsorName.setText("Sponsored by " + sponsorName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        if(!internetIsConnected()){
//            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//            builder.setCancelable(false);
//            builder.setTitle("Network Error");
//            builder.setMessage("Please check your connection and Reload the App");
//            builder.setPositiveButton("Reload",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent=new Intent(MainActivity.this,MainActivity.class);
//                            startActivity(intent);
//                        }
//                    });
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        }


        binding.itemImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MainActivity.this, AppLock.class);
                startActivity(intent);
                return false;
            }
        });

        binding.add.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (auth.getCurrentUser() == null) {
                        Toast.makeText(MainActivity.this, "You are not the Admin", Toast.LENGTH_SHORT).show();
                    } else {
                        AddGame item = new AddGame();
                        item.show(getSupportFragmentManager(), item.getTag());
                    }
                }
                return true;
            }
        });

        binding.individuals.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    Intent intent = new Intent(MainActivity.this, Individual.class);
                    startActivity(intent);
                }
                return true;
            }
        });
        binding.notification.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    Intent intent = new Intent(MainActivity.this, notifications.class);
                    startActivity(intent);
                }
                return true;
            }
        });

        GameAdapter adapter = new GameAdapter(list, this);
        binding.recycleView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recycleView.setLayoutManager(layoutManager);

        database.getReference().child("Games").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NewGame games = dataSnapshot.getValue(NewGame.class);
                    games.getName(dataSnapshot.getKey());
                    list.add(games);
                }
                Collections.sort(list, new sortByDates());
                adapter.notifyDataSetChanged();
                binding.shimmerLayout.stopShimmer();
                binding.individuals.setVisibility(View.VISIBLE);
                binding.shimmerLayout.setVisibility(View.GONE);
                binding.recycleView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.recycleView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, binding.recycleView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent1 = new Intent(MainActivity.this, InGamePage.class);
                        intent1.putExtra("Name", list.get(position).getName());
                        intent1.putExtra("Rounds", list.get(position).getRounds());
                        startActivity(intent1);
                    }

                    @Override
                    public void onLongItemClick(View vierw, int position) {
                        if (auth.getCurrentUser() != null) {
                            DeleteAGame fragment = new DeleteAGame();
                            Bundle bundle = new Bundle();
                            bundle.putString("Name", list.get(position).getName());
                            fragment.setArguments(bundle);
                            fragment.show(getSupportFragmentManager(), fragment.getTag());
                            adapter.notifyDataSetChanged();
                        } else
                            Toast.makeText(MainActivity.this, "You are not Admin", Toast.LENGTH_SHORT).show();
                    }
                })
        );


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    public class sortByDates implements Comparator {

        public int compare(Object o1, Object o2) {
            NewGame p1 = (NewGame) o1;
            NewGame p2 = (NewGame) o2;
            return (int) (Long.parseLong(p2.getComputerDate()) - Long.parseLong(p1.getComputerDate()));
        }
    }

    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }
}
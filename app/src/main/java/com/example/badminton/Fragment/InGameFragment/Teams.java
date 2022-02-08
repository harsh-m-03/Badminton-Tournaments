package com.example.badminton.Fragment.InGameFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.badminton.Adapter.TeamAdapter;
import com.example.badminton.Fragment.BottomSheets.NewTeam;
import com.example.badminton.Fragment.BottomSheets.TeamInfoBottomSheet;
import com.example.badminton.Fragment.BottomSheets.TeamMaker;
import com.example.badminton.Fragment.BottomSheets.TeamUpdateBottomSheet;
import com.example.badminton.MainActivity;
import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.RecyclerItemClickListener;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.Modules.TeamInfo;
import com.example.badminton.databinding.FragmentTeamsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Teams extends Fragment {
    FragmentTeamsBinding binding;
    GestureDetector gestureDetector;
    FirebaseDatabase database;
    ArrayList<TeamInfo> list = new ArrayList<>();
    FirebaseAuth auth;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTeamsBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());

        TeamAdapter adapter = new TeamAdapter(list, getContext());
        binding.recycleView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recycleView.setLayoutManager(layoutManager);
        String name = getActivity().getIntent().getStringExtra("Name");

        binding.add.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (auth.getCurrentUser() == null) {
                        Toast.makeText(getContext(), "You are not the Admin", Toast.LENGTH_SHORT).show();
                    } else {
                        database.getReference().child("Games").child(name).child("currentMatch").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot s) {
                                String cur = s.getValue(String.class);
                                if (Integer.parseInt(cur) != 0) {
                                    Toast.makeText(getContext(), "Tournament has started, You can't edit the teams now", Toast.LENGTH_SHORT).show();
                                } else {
                                    NewTeam team = new NewTeam();
                                    team.show(getActivity().getSupportFragmentManager(), team.getTag());
                                }
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

        binding.random.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    if (auth.getCurrentUser() == null) {
                        Toast.makeText(getContext(), "You are not the Admin", Toast.LENGTH_SHORT).show();
                    } else {
                        database.getReference().child("Games").child(name).child("currentMatch").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot s) {
                                String cur = s.getValue(String.class);
                                if (Integer.parseInt(cur) != 0) {
                                    Toast.makeText(getContext(), "Tournament has started, You can't edit the teams now", Toast.LENGTH_SHORT).show();
                                } else {
                                    TeamMaker frag = new TeamMaker();
                                    frag.show(getActivity().getSupportFragmentManager(), frag.getTag());
                                }
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

        database.getReference().child("Games").child(name).child("Teams").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TeamInfo t = dataSnapshot.getValue(TeamInfo.class);
                    t.getTeamName(dataSnapshot.getKey());
                    list.add(t);
                }
                if (list.isEmpty()) {
                    binding.animationView.setVisibility(View.VISIBLE);
                } else {
                    binding.animationView.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
                binding.shimmerLayout.stopShimmer();
                binding.shimmerLayout.setVisibility(View.GONE);
                binding.recycleView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.recycleView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), binding.recycleView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        TeamInfoBottomSheet info = new TeamInfoBottomSheet();
                        Bundle bundle = new Bundle();
                        bundle.putString("Name", list.get(position).getTeamName());
                        info.setArguments(bundle);
                        info.show(getActivity().getSupportFragmentManager(), info.getTag());
                    }

                    @Override
                    public void onLongItemClick(View vierw, int position) {
                        if (auth.getCurrentUser() == null) {
                            Toast.makeText(getContext(), "You are not the Admin", Toast.LENGTH_SHORT).show();
                        } else {
                            TeamUpdateBottomSheet info = new TeamUpdateBottomSheet();
                            Bundle bundle = new Bundle();
                            bundle.putString("Name", list.get(position).getTeamName());
                            info.setArguments(bundle);
                            info.show(getActivity().getSupportFragmentManager(), info.getTag());
                            adapter.notifyDataSetChanged();

                        }
                    }
                })
        );

        return binding.getRoot();
    }
}
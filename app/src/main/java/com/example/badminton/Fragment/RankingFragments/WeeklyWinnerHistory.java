package com.example.badminton.Fragment.RankingFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.badminton.databinding.FragmentWeeklyWinnerHistoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WeeklyWinnerHistory extends Fragment {
    FragmentWeeklyWinnerHistoryBinding binding;
    FirebaseDatabase database;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        database=FirebaseDatabase.getInstance();
        binding=FragmentWeeklyWinnerHistoryBinding.inflate(inflater,container,false);
        binding.history.setMovementMethod(new ScrollingMovementMethod());


        database.getReference().child("Weekly Status").child("History").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String>weeklyHistory=new ArrayList<>();
                for(DataSnapshot snap:snapshot.getChildren()){
                    String cur=snap.getValue(String.class);
                    weeklyHistory.add(cur);
                }
                String history="";
                for(int i=weeklyHistory.size()-1;i>=0;i--){
                    history+="Week "+(i + 1)+" Winner: "+weeklyHistory.get(i)+"\n\n";
                }
                binding.history.setText(history.substring(0,history.length()-1));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();

    }
}
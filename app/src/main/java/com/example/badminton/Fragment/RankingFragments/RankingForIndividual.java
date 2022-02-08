package com.example.badminton.Fragment.RankingFragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.badminton.Individual;
import com.example.badminton.Modules.IndividualRankings;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentIndividualRankingBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankingForIndividual extends Fragment {

  FragmentIndividualRankingBinding binding;
FirebaseDatabase database;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentIndividualRankingBinding.inflate(inflater,container,false);
        database = FirebaseDatabase.getInstance();
        List<IndividualRankings> list = new ArrayList<>();


        database.getReference().child("Individual Rankings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    IndividualRankings temp = s.getValue(IndividualRankings.class);
                    float percentage;
                    if (temp.getTournamentPlayed().equals("0")) percentage = 0;
                    else {
                        int played = Integer.parseInt(temp.getTournamentPlayed());
                        int won = Integer.parseInt(temp.getTournamentWon());
                        percentage = (float) (won * 100.0 / played);
                    }

                    temp.setWinningPercentage(String.valueOf(percentage) + "000");
                    if (temp.getWinningPercentage().length() > 5)
                        temp.setWinningPercentage(temp.getWinningPercentage().substring(0, 5));
                    list.add(temp);
                }
                Collections.sort(list, new SortByPCT());
                String percentage = "PCT (%)\n\n", playerName = "Player\n\n", lost = "Lost\n\n", won = "Won\n\n", played = "Played\n\n";
                String rankings = "Rank\n\n", weekly = "Titles\n\n";
                int counter = 1;
                for (IndividualRankings i : list) {
                    percentage += i.getWinningPercentage() + "\n";
                    playerName += i.getPlayerName() + "\n";
                    lost += i.getTournamentLost() + '\n';
                    weekly += i.getWeeklyWins() + "\n";
                    won += i.getTournamentWon() + '\n';
                    played += i.getTournamentPlayed() + '\n';
                    rankings += String.valueOf(counter++) + "\n";
                }
                binding.weeklyWins.setText(weekly);
                binding.rank.setText(rankings);
                binding.percentage.setText(percentage);
                binding.playerName.setText(playerName);
                binding.tournamentLost.setText(lost);
                binding.tournamentWon.setText(won);
                binding.tournamentPlayed.setText(played);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return binding.getRoot();
    }
    public class SortByPCT implements Comparator {

        public int compare(Object o1, Object o2) {
            IndividualRankings p1 = (IndividualRankings) o1;
            IndividualRankings p2 = (IndividualRankings) o2;
            if (!p2.getWeeklyWins().equals(p1.getWeeklyWins()))
                return (Integer.parseInt(p2.getWeeklyWins()) - Integer.parseInt(p1.getWeeklyWins()));
            else if (!p2.getWinningPercentage().equals(p1.getWinningPercentage()))
                return Float.compare(Float.parseFloat(p2.getWinningPercentage()), Float.parseFloat(p1.getWinningPercentage()));
            return (Integer.parseInt(p2.getTournamentWon()) - Integer.parseInt(p1.getTournamentWon()));
        }
    }
}
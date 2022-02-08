package com.example.badminton.Fragment.BottomSheets;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.badminton.R;
import com.example.badminton.databinding.FragmentShowingWinnersBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class ShowingWinners extends BottomSheetDialogFragment {
    FirebaseDatabase database;
    FragmentShowingWinnersBinding binding;
    MediaPlayer mediaPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentShowingWinnersBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        String gameName = getActivity().getIntent().getStringExtra("Name");

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource("https://www.fesliyanstudios.com/play-mp3/6978");
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.prepareAsync();
        mediaPlayer.setLooping(true);
        
        database.getReference().child("Games").child(gameName).child("winner").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String winner = snapshot.getValue(String.class);
                binding.winners.setText(winner);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mediaPlayer.stop();
    }
}
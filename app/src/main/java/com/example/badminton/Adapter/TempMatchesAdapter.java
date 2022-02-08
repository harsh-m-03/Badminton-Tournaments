package com.example.badminton.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badminton.Modules.NewGame;
import com.example.badminton.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TempMatchesAdapter extends RecyclerView.Adapter<TempMatchesAdapter.ViewHolder> {
    ArrayList<String> list;
    Context context;
    FirebaseDatabase database;

    public TempMatchesAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_layout_home_page, parent, false);
        return new TempMatchesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        database=FirebaseDatabase.getInstance();
        String temp = list.get(position);
        holder.name.setText(String.valueOf(position + 1) + ". " + temp);

        int i=position;

        database.getReference().child("Games").child(gameName).child("currentMatch").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String cur=snapshot.getValue(String.class);
                if(Integer.parseInt(cur)==i&&Integer.parseInt(cur)!=list.size()){
                    holder.itemView.setBackgroundResource(R.drawable.current_match_btn);
                }else if(Integer.parseInt(cur)>i&&Integer.parseInt(cur)!=list.size()){
                    holder.itemView.setBackgroundColor(R.drawable.completed_match_btn);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    String gameName;

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.gameName);
        }
    }
}

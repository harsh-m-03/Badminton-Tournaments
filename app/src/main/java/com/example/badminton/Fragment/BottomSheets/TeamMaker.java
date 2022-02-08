package com.example.badminton.Fragment.BottomSheets;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.badminton.Modules.OnPressUI;
import com.example.badminton.Modules.SingleTapClick;
import com.example.badminton.R;
import com.example.badminton.databinding.FragmentTeamMakerBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class TeamMaker extends BottomSheetDialogFragment {
    GestureDetector gestureDetector;
    FirebaseDatabase database;
    FragmentTeamMakerBinding binding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTeamMakerBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        gestureDetector = new GestureDetector(getContext(), new SingleTapClick());

        binding.create.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new OnPressUI().onPressUi(view, motionEvent);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    String teams = "";
                    teams = (binding.noOfTeams.getText().toString());
                    if (teams.isEmpty())
                        binding.noOfTeams.setError("Required");
                    else if (!isNumeric(teams))
                        binding.noOfTeams.setError("Numbers only");
                    else if (binding.names.getText().toString().isEmpty())
                        binding.names.setError("Required");
                    else if (Integer.parseInt(teams) < 2)
                        binding.noOfTeams.setError("2 or more Teams Required");
//                    else if (Integer.parseInt(teams) % 2 != 0)
//                        binding.noOfTeams.setError("Even Number Required");
                    else if ((binding.names.getText().toString().contains("-") || !binding.names.getText().toString().contains(" "))||Integer.parseInt(teams) * 2 + Integer.parseInt(teams) - 1 < binding.names.getText().toString().length())
                        binding.names.setError("Please use Correct Naming Format");
                    else {
                        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData data = ClipData.newPlainText("EditText", binding.names.getText().toString());
                        clipboardManager.setPrimaryClip(data);
                        TeamList info = new TeamList();
                        Bundle bundle = new Bundle();
                        bundle.putString("names", binding.names.getText().toString());
                        info.setArguments(bundle);
                        info.show(getActivity().getSupportFragmentManager(), info.getTag());
                        TeamMaker.this.dismiss();
                    }
                }
                return true;
            }
        });

        return binding.getRoot();
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
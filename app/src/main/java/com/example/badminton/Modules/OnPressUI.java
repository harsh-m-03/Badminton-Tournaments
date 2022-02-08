package com.example.badminton.Modules;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.badminton.R;

public class OnPressUI {
    public void onPressUi(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            AnimatorSet reducer = (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), R.animator.reduce_size);
            reducer.setTarget(view);
            reducer.start();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            AnimatorSet regainer = (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), R.animator.regain_size);
            regainer.setTarget(view);
            regainer.start();
        }
    }
}

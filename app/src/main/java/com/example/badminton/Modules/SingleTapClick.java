package com.example.badminton.Modules;


import android.view.GestureDetector;
import android.view.MotionEvent;

public class SingleTapClick extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onDown(MotionEvent e) {
            /*it needs to return true if we don't want
            to ignore rest of the gestures*/
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        return true;
    }
}
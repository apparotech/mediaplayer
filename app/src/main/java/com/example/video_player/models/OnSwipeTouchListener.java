package com.example.video_player.models;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {
    private  final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context ctx) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    public  boolean onTouch(View view, MotionEvent motionEvent) {
        return  gestureDetector.onTouchEvent(motionEvent);
    }

    public  final  class  GestureListener extends  GestureDetector.SimpleOnGestureListener {

        public  boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return  super.onFling(e1,e2,velocityX,velocityY);
        }
        public  boolean onDoubleTap(MotionEvent e) {
            onDoubleTouch();
            return  super.onDoubleTap(e);
        }
        public  boolean onSingleTapConfirmed(MotionEvent e) {
            onSingleTouch();
            return  super.onSingleTapConfirmed(e);
        }
    }
    public  void onDoubleTouch(){

    }
    public  void onSingleTouch(){

    }
}

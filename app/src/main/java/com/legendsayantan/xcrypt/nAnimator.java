package com.legendsayantan.xcrypt;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import com.borutsky.neumorphism.NeumorphicFrameLayout;

public class nAnimator {
    public NeumorphicFrameLayout neumorphicFrameLayout;
    public NeumorphicFrameLayout.State initialState;

    @SuppressLint("ClickableViewAccessibility")
    public nAnimator(NeumorphicFrameLayout neumorphicFrameLayout, NeumorphicFrameLayout.State pressstate, Runnable callback) {
        this.neumorphicFrameLayout = neumorphicFrameLayout;
        initialState=neumorphicFrameLayout.getState();
        neumorphicFrameLayout.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                neumorphicFrameLayout.setState(pressstate);
            }
            if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                neumorphicFrameLayout.setState(initialState);
                try {
                    callback.run();
                }catch (Exception e){
                }
            }
            return true;
        });
    }

}

package com.legendsayantan.xcrypt;

import android.view.MotionEvent;
import android.view.View;

import com.borutsky.neumorphism.NeumorphicFrameLayout;

public class nAnimator {
    public NeumorphicFrameLayout neumorphicFrameLayout;
    public NeumorphicFrameLayout.State initialState;

    public nAnimator(NeumorphicFrameLayout neumorphicFrameLayout, NeumorphicFrameLayout.State pressstate, Runnable callback) {
        this.neumorphicFrameLayout = neumorphicFrameLayout;
        initialState=neumorphicFrameLayout.getState();
        neumorphicFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
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
            }
        });
    }

}

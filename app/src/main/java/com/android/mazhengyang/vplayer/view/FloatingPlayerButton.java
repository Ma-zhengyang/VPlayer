package com.android.mazhengyang.vplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by mazhengyang on 19-2-22.
 */

public class FloatingPlayerButton extends ImageView {

    private Listener mListener;

    public FloatingPlayerButton(Context context) {
        this(context, null);
    }

    public FloatingPlayerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingPlayerButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);
        setClickable(true);
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (mListener != null) {
                    mListener.show(true);
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mListener != null) {
                    mListener.show(false);
                }
                break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public interface Listener {
        void show(boolean pressed);
    }

}

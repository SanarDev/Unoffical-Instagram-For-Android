package com.idirect.app.customview.storyrecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class StoryRecyclerView extends RecyclerView {

    private boolean isTouchMovable = false;

    public boolean isTouchMovable() {
        return isTouchMovable;
    }

    public void setTouchMovable(boolean touchMovable) {
        isTouchMovable = touchMovable;
    }

    public StoryRecyclerView(@NonNull Context context) {
        super(context);
    }

    public StoryRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StoryRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(isTouchMovable);
                break;
            default:
                getParent().requestDisallowInterceptTouchEvent(!isTouchMovable);
                break;

        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return isTouchMovable;
    }
}

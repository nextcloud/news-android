package de.luhmer.owncloudnewsreader.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by David on 21.06.2014.
 */
public class PodcastSlidingUpPanelLayout extends SlidingUpPanelLayout{
    public PodcastSlidingUpPanelLayout(Context context) {
        super(context);
    }

    public PodcastSlidingUpPanelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PodcastSlidingUpPanelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //return super.onInterceptTouchEvent(ev);

        return isDragViewHit((int)ev.getX(), (int)ev.getY());
    }

    private View mDragView;
    private View mSlideableView;

    public void setSlideableView(View view) {
        this.mSlideableView = view;
    }

    @Override
    public void setDragView(View dragView) {
        this.mDragView = dragView;
        super.setDragView(dragView);
    }


    private boolean isDragViewHit(int x, int y) {



        //original implementation - only allow dragging on mDragView
        View v = isPanelExpanded() ? mDragView : mSlideableView;
        if (v == null) return false;
        int[] viewLocation = new int[2];
        v.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + v.getWidth() &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + v.getHeight();

    }
}

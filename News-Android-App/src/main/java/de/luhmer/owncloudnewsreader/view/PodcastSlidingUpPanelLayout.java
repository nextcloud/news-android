package de.luhmer.owncloudnewsreader.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by David on 21.06.2014.
 */
public class PodcastSlidingUpPanelLayout extends SlidingUpPanelLayout {
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
        return super.onInterceptTouchEvent(ev);
        //return isDragViewHit((int)ev.getX(), (int)ev.getY());
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







    public static void expand(final View v) {
        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targtetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LayoutParams.WRAP_CONTENT
                        : (int)(targtetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targtetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}

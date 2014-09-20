package de.luhmer.owncloudnewsreader.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.RelativeLayout;

import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.events.podcast.VideoDoubleClicked;

/**
 * Created by David on 30.06.2014.
 */
//http://stackoverflow.com/questions/10013906/android-zoom-in-out-relativelayout-with-spread-pinch
public class ZoomableRelativeLayout extends RelativeLayout {
    private static final int INVALID_POINTER_ID = -1;
    private static final int INVALID_SIZE = -1;


    public boolean disableScale = false;
    public void setDisableScale(boolean disableScale) {
        this.disableScale = disableScale;
    }

    private static final String TAG = "ZoomableRelativeLayout";

    private GestureDetector mDoubleTapDetector;
    private ScaleGestureDetector mScaleDetector;
    float mScaleFactor = 1;
    public float getScaleFactor() {
        return mScaleFactor;
    }

    float mPosX;
    float mPosY;
    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId;

    private float mInitHeight = INVALID_SIZE;
    private float mInitWidth = INVALID_SIZE;

    public ZoomableRelativeLayout(Context context) {
        super(context);
        initZoomView(context);
    }

    public ZoomableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initZoomView(context);
    }

    public ZoomableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initZoomView(context);
    }


    boolean mPositionReady = false;
    public boolean isPositionReady() {
        return mPositionReady;
    }

    /*
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if(hasWindowFocus) {
            readVideoPosition();
            mPositionReady = true;
        }
        super.onWindowFocusChanged(hasWindowFocus);
    }
    */



    public void readVideoPosition() {
        int position[] = new int[2];
        getLocationOnScreen(position);
        mVideoXPosition = position[0];
        mVideoYPosition = position[1];

        mPositionReady = true;

        Log.d(TAG, "Grabbing new Video Wrapper Position. X:" + mVideoXPosition + " - Y:" + mVideoYPosition);

        //mVideoXPosition = getX();
        //mVideoYPosition = getY();
    }

    private void initZoomView(Context context) {
        // Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mDoubleTapDetector = new GestureDetector(context, new DoubleTapListener());
    }

    /*
    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScaleFactor, mScaleFactor, mPosX, mPosY);
        super.dispatchDraw(canvas);
        canvas.restore();
    }*/

    private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            Log.d("Double Tap", "Tapped at: (" + x + "," + y + ")");

            EventBus.getDefault().post(new VideoDoubleClicked());

            return true;
        }
    }


    private float mVideoXPosition;
    private float mVideoYPosition;
    public float getVideoXPosition() {return mVideoXPosition;}
    public float getVideoYPosition() {return mVideoYPosition;}

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if(!disableScale) {
                readVideoPosition();
            }

            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if(disableScale)
                return true;

            if(mInitWidth == INVALID_SIZE) {
                mInitWidth = getWidth();
                mInitHeight = getHeight();
            }

            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            if(mScaleFactor < 1)
                mScaleFactor = 1;


            Log.d(TAG, "Scale:" + mScaleFactor);


            getLayoutParams().width = (int)(mInitWidth * mScaleFactor);
            getLayoutParams().height = (int)(mInitHeight * mScaleFactor);
            setLayoutParams(getLayoutParams());

            //invalidate();
            return true;
        }
    }


    /*
    public void restore() {
        mScaleFactor = 1;
        this.invalidate();
    }*/



    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        mDoubleTapDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

}
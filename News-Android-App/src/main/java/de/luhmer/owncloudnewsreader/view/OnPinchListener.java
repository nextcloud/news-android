package de.luhmer.owncloudnewsreader.view;

import android.view.ScaleGestureDetector;

/**
 * Created by David on 30.06.2014.
 */
public class OnPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    private float mScaleFactor = 1.f;

    ZoomableRelativeLayout mZoomableRelativeLayout;

    public OnPinchListener(ZoomableRelativeLayout mZoomableRelativeLayout) {
        this.mZoomableRelativeLayout = mZoomableRelativeLayout;
    }

    float startingSpan;
    float endSpan;
    float startFocusX;
    float startFocusY;


    public boolean onScaleBegin(ScaleGestureDetector detector) {
        startingSpan = detector.getCurrentSpan();
        startFocusX = detector.getFocusX();
        startFocusY = detector.getFocusY();
        return true;
    }




    /*
    public boolean onScale(ScaleGestureDetector detector) {
        mZoomableRelativeLayout.scale(detector.getCurrentSpan()/startingSpan, startFocusX, startFocusY);
        return true;
    }
    */

    public void onScaleEnd(ScaleGestureDetector detector) {
        //mZoomableRelativeLayout.restore();
    }
}
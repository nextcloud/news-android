package de.luhmer.owncloudnewsreader.helper;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by David on 30.06.2014.
 */
public class SizeAnimator {

    public SizeAnimator(View view, float mWidth, float mHeight, float oldWidth, float oldHeight, int duration) {
        this.viewToSizeAnimate = view;
        this.mWidth = mWidth;
        this.mHeight = mHeight;
        this.mOldHeight = oldHeight;
        this.mOldWidth = oldWidth;

        sizeAnimator.setDuration(duration);
    }

    View viewToSizeAnimate;
    float mWidth;
    float mHeight;
    float mOldWidth;
    float mOldHeight;



    public Animation sizeAnimator = new Animation() {

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            viewToSizeAnimate.getLayoutParams().width = (int)(mOldWidth + ((mWidth - mOldWidth) * interpolatedTime));
            viewToSizeAnimate.getLayoutParams().height =  (int) (mOldHeight + ((mHeight - mOldHeight) * interpolatedTime));
            viewToSizeAnimate.setLayoutParams(viewToSizeAnimate.getLayoutParams());
        }
    };

}

package de.luhmer.owncloudnewsreader.helper;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Created by David on 02.07.2014.
 */
public class AlphaAnimator {

    public static void AnimateVisibilityChange(final View view, final int visibilityTo) {


        Animation animation;
        if(visibilityTo == View.GONE) {
            animation = new AlphaAnimation(1f, 0f);
        } else {
            view.setAlpha(0.1f);
            view.setVisibility(View.VISIBLE);
            animation = new AlphaAnimation(0f, 1f);
        }


        animation.setFillAfter(true);
        animation.setDuration(1000);
        animation.setStartOffset(1000);
        //animation.setStartOffset(5000);

        /*
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(visibilityTo);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        */

        animation.start();


    }
}

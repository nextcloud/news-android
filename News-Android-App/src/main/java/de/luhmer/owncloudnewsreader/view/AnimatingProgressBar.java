package de.luhmer.owncloudnewsreader.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;

/**
 * Like a standard android progress bar but with an animated progress
 * on Honeycomb and above. Use it like a normal progress bar.
 */
public class AnimatingProgressBar extends ProgressBar {

    /**
     * easing of the bar animation
     */
    private static final Interpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();

    /**
     * animation dureation in milliseconds
     */
    private static final int ANIMATION_DURATION = 350;

    /**
     * Factor by which the progress bar resolution will be increased. E.g. the max
     * value is set to 5 and the resolution to 100: the bar can animate internally
     * between the values 0 and 500.
     */
    private static final int RESOLUTION = 100;

    private ValueAnimator animator;
    private ValueAnimator animatorSecondary;
    private boolean animate = true;

    public AnimatingProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AnimatingProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatingProgressBar(Context context) {
        super(context);
    }

    public boolean isAnimate() {
        return animate;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    @Override
    public synchronized void setMax(int max) {
        super.setMax(max * RESOLUTION);
    }

    @Override
    public synchronized int getMax() {
        return super.getMax() / RESOLUTION;
    }

    @Override
    public synchronized int getProgress() {
        return super.getProgress() / RESOLUTION;
    }

    @Override
    public synchronized int getSecondaryProgress() {
        return super.getSecondaryProgress() / RESOLUTION;
    }

    @Override
    public synchronized void setProgress(int progress) {
        if (!animate) {
            super.setProgress(progress);
            return;
        }

        if (animator == null) {
            animator = ValueAnimator.ofInt(getProgress() * RESOLUTION, progress * RESOLUTION);
            animator.setInterpolator(DEFAULT_INTERPOLATOR);
            animator.setDuration(ANIMATION_DURATION);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    AnimatingProgressBar.super.setProgress((Integer) animation.getAnimatedValue());
                }
            });
        }

        animator.cancel();
        animator.setIntValues(getProgress() * RESOLUTION, progress * RESOLUTION);
        animator.start();
    }

    @Override
    public synchronized void setSecondaryProgress(int secondaryProgress) {
        if (!animate) {
            super.setSecondaryProgress(secondaryProgress);
            return;
        }

        if (animatorSecondary == null) {
            animatorSecondary = ValueAnimator.ofInt(getSecondaryProgress() * RESOLUTION, secondaryProgress * RESOLUTION);
            animatorSecondary.setInterpolator(DEFAULT_INTERPOLATOR);
            animatorSecondary.setDuration(ANIMATION_DURATION);
            animatorSecondary.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    AnimatingProgressBar.super.setSecondaryProgress((Integer) animation.getAnimatedValue());
                }
            });
        }

        animatorSecondary.cancel();
        animatorSecondary.setIntValues(getSecondaryProgress() * RESOLUTION, secondaryProgress * RESOLUTION);
        animatorSecondary.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (animator != null) {
            animator.cancel();
        }
        if (animatorSecondary != null) {
            animatorSecondary.cancel();
        }
    }
}

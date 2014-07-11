package de.luhmer.owncloudnewsreader;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nineoldandroids.view.ViewHelper;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterVideoOutput;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.events.podcast.VideoDoubleClicked;
import de.luhmer.owncloudnewsreader.helper.SizeAnimator;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.view.PodcastSlidingUpPanelLayout;
import de.luhmer.owncloudnewsreader.view.ZoomableRelativeLayout;

/**
 * Created by David on 29.06.2014.
 */
public class PodcastSherlockFragmentActivity extends SherlockFragmentActivity {

    private static final String TAG = "PodcastSherlockFragmentActivity";
    private PodcastFragment podcastFragment;

    private EventBus eventBus;

    @InjectView(R.id.videoPodcastSurfaceWrapper) ZoomableRelativeLayout rlVideoPodcastSurfaceWrapper;
    //@InjectView(R.id.videoPodcastSurface) SurfaceView surfaceView;
    @InjectView(R.id.sliding_layout) PodcastSlidingUpPanelLayout sliding_layout;

    int appHeight;
    int appWidth;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        eventBus = EventBus.getDefault();

        ButterKnife.inject(this);

        //surfaceView.getHolder().setFixedSize(surfaceView.getWidth(), 10);
        //surfaceView.setVisibility(View.GONE);
        //rlVideoPodcastSurfaceWrapper.setVisibility(View.GONE);

        rlVideoPodcastSurfaceWrapper.setVisibility(View.INVISIBLE);

        UpdatePodcastView();

        /*
        new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
            @Override
            public void onOrientationChanged(int i) {
                sliding_layout.collapsePanel();
            }
        };
        */

        super.onPostCreate(savedInstanceState);
    }




    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if(hasWindowFocus) {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation != lastOrientation) {
                sliding_layout.collapsePanel();
                lastOrientation = currentOrientation;
            }
        }

        //rlVideoPodcastSurfaceWrapper.setVisibility(View.GONE);
        //isVideoViewVisible = false;

        super.onWindowFocusChanged(hasWindowFocus);
    }



    int lastOrientation = -1;
    @Override
    protected void onResume() {
        eventBus.register(this);

        //eventBus.post(new RegisterVideoOutput(surfaceView));
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        eventBus.unregister(this);
        eventBus.post(new RegisterVideoOutput(null, null));

        super.onPause();
    }


    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        sliding_layout.collapsePanel();
        super.onConfigurationChanged(newConfig);
    }
    */

    public PodcastSlidingUpPanelLayout getSlidingLayout() {
        return sliding_layout;
    }

    public boolean handlePodcastBackPressed() {
        if(podcastFragment != null && sliding_layout.isPanelExpanded()) {
            if (!podcastFragment.onBackPressed())
                sliding_layout.collapsePanel();
            return true;
        }
        return false;
    }

    protected void UpdatePodcastView() {

        if(podcastFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(podcastFragment).commitAllowingStateLoss();
        }

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPrefs.getBoolean(SettingsActivity.CB_ENABLE_PODCASTS_STRING, false)) {
            podcastFragment = PodcastFragment.newInstance(null, null);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.podcast_frame, podcastFragment)
                    .commitAllowingStateLoss();

            sliding_layout.getChildAt(1).setVisibility(View.VISIBLE);
        } else {
            sliding_layout.getChildAt(1).setVisibility(View.GONE);
            podcastFragment = null;
        }
    }

    protected boolean podcastRequiresRestartOfUI() {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean podcastEnabled = mPrefs.getBoolean(SettingsActivity.CB_ENABLE_PODCASTS_STRING, false);

        if(podcastEnabled && sliding_layout.getVisibility() == View.GONE ||
                !podcastEnabled && sliding_layout.getVisibility() == View.VISIBLE)
            return true;
        return false;
    }


    boolean surfaceInitalized = false;
    boolean isVideoViewVisible = true;
    public void onEventMainThread(UpdatePodcastStatusEvent podcast) {

        if (podcast.isVideoFile()) {
            if((!isVideoViewVisible || !surfaceInitalized) && rlVideoPodcastSurfaceWrapper.isPositionReady()) {
                surfaceInitalized = true;
                isVideoViewVisible = true;

                rlVideoPodcastSurfaceWrapper.setVisibility(View.VISIBLE);
                //AlphaAnimator.AnimateVisibilityChange(rlVideoPodcastSurfaceWrapper, View.VISIBLE);


                SurfaceView surfaceView = new SurfaceView(this);
                surfaceView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                rlVideoPodcastSurfaceWrapper.addView(surfaceView);

                eventBus.post(new RegisterVideoOutput(surfaceView, rlVideoPodcastSurfaceWrapper));
                togglePodcastVideoViewAnimation();
            }
        } else if(isVideoViewVisible) {
            isVideoViewVisible = false;

            eventBus.post(new RegisterVideoOutput(null, null));

            rlVideoPodcastSurfaceWrapper.setVisibility(View.GONE);
            //AlphaAnimator.AnimateVisibilityChange(rlVideoPodcastSurfaceWrapper, View.GONE);

            rlVideoPodcastSurfaceWrapper.removeAllViews();
        }

    }

    /*
    // This snippet hides the system bars.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        //| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }
    */

    private static final int animationTime = 300; //Milliseconds
    float oldScaleFactor = 1;
    boolean isFullScreen = false;
    float scaleFactor = 1;
    boolean useAnimation = false;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onEventMainThread(VideoDoubleClicked doubleClicked) {
        appHeight = getWindow().getDecorView().findViewById(android.R.id.content).getHeight();
        appWidth = getWindow().getDecorView().findViewById(android.R.id.content).getWidth();

        if(isFullScreen) {
            rlVideoPodcastSurfaceWrapper.setDisableScale(false);
            togglePodcastVideoViewAnimation();

            //showSystemUI();
        } else {
            //hideSystemUI();

            rlVideoPodcastSurfaceWrapper.setDisableScale(true);
            oldScaleFactor = rlVideoPodcastSurfaceWrapper.getScaleFactor();

            final View view = rlVideoPodcastSurfaceWrapper;

            final float oldHeight = view.getLayoutParams().height;
            final float oldWidth = view.getLayoutParams().width;


            //view.setPivotX(oldWidth/2);
            //view.setPivotY(oldHeight/2);

            /*
            Display display = getWindowManager().getDefaultDisplay();
            float width = display.getWidth();  // deprecated
            float height = display.getHeight();  // deprecated
            */



            scaleFactor = appWidth / (float) view.getWidth();
            float newHeightTemp = oldHeight * scaleFactor;
            float newWidthTemp = oldWidth * scaleFactor;

            //view.animate().scaleX(scaleFactor).scaleY(scaleFactor).setDuration(100);
            //scaleView(view, 1f, scaleFactor, 1f, scaleFactor);


            if(newHeightTemp > appHeight) { //Could happen on Tablets or in Landscape Mode
                scaleFactor = appHeight / (float) view.getHeight();
                newHeightTemp = oldHeight * scaleFactor;
                newWidthTemp = oldWidth * scaleFactor;
            }


            final float newHeight = newHeightTemp;
            final float newWidth = newWidthTemp;
            float newXPosition = rlVideoPodcastSurfaceWrapper.getVideoXPosition() + (int) getResources().getDimension(R.dimen.activity_vertical_margin);// (appWidth / 2) + dipToPx(10);
            float newYPosition = (appHeight/2) + ((newHeight/2) - oldHeight);

            useAnimation = true;

            view.animate().x(newXPosition).y(newYPosition).setDuration(animationTime).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if(useAnimation) {
                        view.startAnimation(new SizeAnimator(view, newWidth, newHeight, oldWidth, oldHeight, animationTime).sizeAnimator);
                    }
                    useAnimation = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });


            //rlVideoPodcastSurfaceWrapper.animate().scaleX(scaleFactor).scaleY(scaleFactor).x(newXPosition).y(newYPosition).setDuration(500).setListener(onResizeListener);
            //surfaceView.animate().scaleX(scaleFactor).scaleY(scaleFactor).setDuration(500);

            //oldScaleFactor
            //scaleFactor = dipToPx(oldWidth) / newWidth;
            //scaleFactor = (1/oldScaleFactor) * dipToPx(oldWidth) / newWidth;
            //scaleFactor = oldWidth / newWidth;

            scaleFactor = 1/scaleFactor;
        }

        isFullScreen = !isFullScreen;
    }



    /*
    Animator.AnimatorListener onResizeListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            View view = rlVideoPodcastSurfaceWrapper;


            int height = (int) (view.getHeight() * view.getScaleY());
            int width = (int) (view.getWidth() * view.getScaleX());

            //view.setPivotX(width/2);
            //view.setPivotY(height/2);

            view.setScaleX(1);
            view.setScaleY(1);
            view.getLayoutParams().height = height;
            view.getLayoutParams().width = width;
            view.setLayoutParams(view.getLayoutParams());

            //view.setX(0);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };
    */


    /*
    public void scaleView(View v, float startX, float startY, float endX, float endY) {
        Animation anim = new ScaleAnimation(
                startX, endX, // Start and end values for the X axis scaling
                startY, endY, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(500);
        v.startAnimation(anim);
    }
    */

    /*
    public void onEvent(PodcastPlaybackService.PodcastPlaybackServiceStarted serviceStarted) {

    }
    */


    public void togglePodcastVideoViewAnimation() {
        boolean isLeftSliderOpen = false;

        if(this instanceof NewsReaderListActivity) {
            isLeftSliderOpen = ((NewsReaderListActivity) this).mSlidingLayout.isOpen();
        }

        boolean isTabletView = SubscriptionExpandableListAdapter.isTwoPane(this);

        int podcastMediaControlHeightDp = pxToDp((int) getResources().getDimension(R.dimen.podcast_media_control_height));

        if(isTabletView && sliding_layout.isPanelExpanded()) { //On Tablets
            animateToPositionTargetApiSafe(podcastMediaControlHeightDp);
        } else if(!isTabletView && isLeftSliderOpen)
            animateToPositionTargetApiSafe(0);
        else if(sliding_layout.isPanelExpanded()) {
            animateToPositionTargetApiSafe(podcastMediaControlHeightDp);
        } else {
            animateToPositionTargetApiSafe(64);
        }
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }


    public void animateToPositionTargetApiSafe(int yPosition) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){
            animateToPositionNewApi(yPosition);
        } else{
            animateToPositionOldApi(yPosition);
        }
    }

    public void animateToPositionOldApi(final int yPosition) {
        appHeight = getWindow().getDecorView().findViewById(android.R.id.content).getHeight();
        appWidth = getWindow().getDecorView().findViewById(android.R.id.content).getWidth();

        View view = rlVideoPodcastSurfaceWrapper; //surfaceView

        if(scaleFactor != 1) {
            int newHeight = view.getLayoutParams().height *= scaleFactor;
            int newWidth = view.getLayoutParams().width *= scaleFactor;
            scaleFactor = 1;

            view.getLayoutParams().height = newHeight;
            view.getLayoutParams().width = newWidth;

            view.setLayoutParams(view.getLayoutParams());
        }

        int absoluteYPosition = appHeight - view.getHeight() - (int) getResources().getDimension(R.dimen.activity_vertical_margin) - (int) dipToPx(yPosition);
        float xPosition = rlVideoPodcastSurfaceWrapper.getVideoXPosition();
        ViewHelper.setTranslationX(view, xPosition);
        ViewHelper.setTranslationY(view, absoluteYPosition);

        oldScaleFactor = 1;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void animateToPositionNewApi(final int yPosition) {
        appHeight = getWindow().getDecorView().findViewById(android.R.id.content).getHeight();
        appWidth = getWindow().getDecorView().findViewById(android.R.id.content).getWidth();

        final View view = rlVideoPodcastSurfaceWrapper; //surfaceView

        /*
        viewToMove.getLayoutParams().height *= scaleFactor;
        viewToMove.getLayoutParams().width *= scaleFactor;
        viewToMove.setLayoutParams(viewToMove.getLayoutParams());
        */
        //float newHeight = viewToMove.getLayoutParams().height * scaleFactor;
        //float newWidth = viewToMove.getLayoutParams().width * scaleFactor;
        //viewToMove.animate().scaleX(scaleFactor).scaleY(scaleFactor).setDuration(100);
        //viewToMove.startAnimation(new SizeAnimator(viewToMove, newWidth, newHeight, 500).sizeAnimator);

        //scaleView(viewToMove, scaleFactor, 1f, scaleFactor, 1f);



        if(scaleFactor != 1) {
            int oldHeight = view.getLayoutParams().height;
            int oldWidth = view.getLayoutParams().width;
            int newHeight = view.getLayoutParams().height *= scaleFactor;
            int newWidth = view.getLayoutParams().width *= scaleFactor;
            scaleFactor = 1;

            Animation animator = new SizeAnimator(view, newWidth, newHeight, oldWidth, oldHeight, animationTime).sizeAnimator;
            animator.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animateToPositionTargetApiSafe(yPosition);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animator);
        } else {
            int absoluteYPosition = appHeight - view.getHeight() - (int) getResources().getDimension(R.dimen.activity_vertical_margin) - (int) dipToPx(yPosition);

            //int animationpos = 500;
            float xPosition = rlVideoPodcastSurfaceWrapper.getVideoXPosition();
            view.animate().x(xPosition).y(absoluteYPosition).setDuration(animationTime);
            //scaleX(scaleFactor).scaleY(scaleFactor)
        }




        /*
        int height = (int)(view.getHeight() * scaleFactor);
        int width = (int)(view.getWidth() * scaleFactor);
        view.setScaleX(oldScaleFactor);
        view.setScaleY(oldScaleFactor);
        view.getLayoutParams().height = height;
        view.getLayoutParams().width = width;
*/
        oldScaleFactor = 1;


    }


    float dipToPx(float dip) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
        return px;
    }
}

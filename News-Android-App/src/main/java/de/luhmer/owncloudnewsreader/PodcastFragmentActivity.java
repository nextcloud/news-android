package de.luhmer.owncloudnewsreader;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterVideoOutput;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.events.podcast.VideoDoubleClicked;
import de.luhmer.owncloudnewsreader.helper.SizeAnimator;
import de.luhmer.owncloudnewsreader.interfaces.IPlayPodcastClicked;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;
import de.luhmer.owncloudnewsreader.services.PodcastPlaybackService;
import de.luhmer.owncloudnewsreader.view.PodcastSlidingUpPanelLayout;
import de.luhmer.owncloudnewsreader.view.ZoomableRelativeLayout;

/**
 * Created by David on 29.06.2014.
 */
public class PodcastFragmentActivity extends ActionBarActivity implements IPlayPodcastClicked {

    PodcastPlaybackService mPodcastPlaybackService;
    boolean mBound = false;


    private static final String TAG = "PodcastSherlockFragmentActivity";
    private PodcastFragment mPodcastFragment;

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
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, PodcastPlaybackService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
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


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PodcastPlaybackService.LocalBinder binder = (PodcastPlaybackService.LocalBinder) service;
            mPodcastPlaybackService = binder.getService();

            if(mPodcastPlaybackService.getCurrentlyPlayingPodcast() != null) {
                Picasso.with(PodcastFragmentActivity.this).load(mPodcastPlaybackService.getCurrentlyPlayingPodcast().favIcon).into(mPodcastFragment.imgFavIcon);
            }

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };



    public PodcastSlidingUpPanelLayout getSlidingLayout() {
        return sliding_layout;
    }

    public boolean handlePodcastBackPressed() {
        if(mPodcastFragment != null && sliding_layout.isPanelExpanded()) {
            if (!mPodcastFragment.onBackPressed())
                sliding_layout.collapsePanel();
            return true;
        }
        return false;
    }

    protected void UpdatePodcastView() {

        if(mPodcastFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mPodcastFragment).commitAllowingStateLoss();
        }

        mPodcastFragment = PodcastFragment.newInstance(null, null);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.podcast_frame, mPodcastFragment)
                .commitAllowingStateLoss();

        if(!currentlyPlaying)
            sliding_layout.setPanelHeight(0);
    }

    boolean currentlyPlaying = false;

    boolean surfaceInitalized = false;
    boolean isVideoViewVisible = true;
    public void onEventMainThread(UpdatePodcastStatusEvent podcast) {
        if(podcast.isFileLoaded() || podcast.isPreparingFile() && !currentlyPlaying) {
            //Expand view

            sliding_layout.setPanelHeight((int)dipToPx(68));

            currentlyPlaying = true;
        } else if(!(podcast.isPreparingFile() || podcast.isFileLoaded()) && currentlyPlaying) {
            //Hide view

            sliding_layout.setPanelHeight(0);

            currentlyPlaying = false;
        }

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

    @Override
    public void openPodcast(RssItem rssItem) {
        PodcastItem podcastItem = DatabaseConnectionOrm.ParsePodcastItemFromRssItem(this, rssItem);

        if(podcastItem.mimeType.equals("youtube") && !podcastItem.offlineCached)
            Toast.makeText(this, "Cannot stream from youtube. Please download the video first.", Toast.LENGTH_SHORT).show();
        else {
            File file = new File(PodcastDownloadService.getUrlToPodcastFile(this, podcastItem.link, false));
            if(file.exists())
                podcastItem.link = file.getAbsolutePath();
            else if(!podcastItem.offlineCached)
                Toast.makeText(this, "Starting podcast.. please wait", Toast.LENGTH_SHORT).show(); //Only show if we need to stream the file

            //EventBus.getDefault().post(new OpenPodcastEvent(podcastItem.link, podcastItem.title, isVideo));
            mPodcastPlaybackService.openFile(podcastItem);

            Picasso.with(this).load(rssItem.getFeed().getFaviconUrl()).into(mPodcastFragment.imgFavIcon);
        }
    }
}

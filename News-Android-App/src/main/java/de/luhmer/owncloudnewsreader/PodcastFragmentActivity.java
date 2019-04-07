package de.luhmer.owncloudnewsreader;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nextcloud.android.sso.helper.VersionCheckHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.lang.reflect.Proxy;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.events.podcast.PodcastCompletedEvent;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterVideoOutput;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterYoutubeOutput;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.events.podcast.VideoDoubleClicked;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.SizeAnimator;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.IPlayPausePodcastClicked;
import de.luhmer.owncloudnewsreader.model.MediaItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.notification.NextcloudNotificationManager;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;
import de.luhmer.owncloudnewsreader.services.PodcastPlaybackService;
import de.luhmer.owncloudnewsreader.services.podcast.PlaybackService;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import de.luhmer.owncloudnewsreader.view.PodcastSlidingUpPanelLayout;
import de.luhmer.owncloudnewsreader.view.ZoomableRelativeLayout;
import de.luhmer.owncloudnewsreader.widget.WidgetProvider;

import static de.luhmer.owncloudnewsreader.Constants.MIN_NEXTCLOUD_FILES_APP_VERSION_CODE;
import static de.luhmer.owncloudnewsreader.services.PodcastPlaybackService.CURRENT_PODCAST_ITEM_MEDIA_ITEM;
import static de.luhmer.owncloudnewsreader.services.PodcastPlaybackService.PLAYBACK_SPEED_FLOAT;

public class PodcastFragmentActivity extends AppCompatActivity implements IPlayPausePodcastClicked {

    private static final String TAG = PodcastFragmentActivity.class.getCanonicalName();

    @Inject protected SharedPreferences mPrefs;
    @Inject protected ApiProvider mApi;
    @Inject protected MemorizingTrustManager mMTM;
    @Inject protected PostDelayHandler mPostDelayHandler;

    private MediaBrowserCompat mMediaBrowser;
    private EventBus eventBus;
    private PodcastFragment mPodcastFragment;
    private int appHeight;
    private int appWidth;

    @BindView(R.id.videoPodcastSurfaceWrapper)
    protected ZoomableRelativeLayout rlVideoPodcastSurfaceWrapper;
    @BindView(R.id.sliding_layout)
    protected PodcastSlidingUpPanelLayout sliding_layout;
    //YouTubePlayerFragment youtubeplayerfragment;

    private boolean currentlyPlaying = false;
    private boolean showedYoutubeFeatureNotAvailableDialog = false;
    private boolean videoViewInitialized = false;
    private boolean isVideoViewVisible = true;


    private static final int animationTime = 300; //Milliseconds
    private boolean isFullScreen = false;
    private float scaleFactor = 1;
    private boolean useAnimation = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((NewsReaderApplication) getApplication()).getAppComponent().injectActivity(this);

        ThemeChooser.getInstance(this).chooseTheme(this);
        super.onCreate(savedInstanceState);
        ThemeChooser.getInstance(this).afterOnCreate(this);

        if (mApi.getAPI() instanceof Proxy) { // Single Sign On
            VersionCheckHelper.verifyMinVersion(this, MIN_NEXTCLOUD_FILES_APP_VERSION_CODE);
        }

        mPostDelayHandler.stopRunningPostDelayHandler();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMTM.bindDisplayActivity(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        eventBus = EventBus.getDefault();

        ButterKnife.bind(this);

        //youtubeplayerfragment = (YouTubePlayerFragment)getFragmentManager().findFragmentById(R.id.youtubeplayerfragment);


        ViewTreeObserver vto = rlVideoPodcastSurfaceWrapper.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rlVideoPodcastSurfaceWrapper.readVideoPosition();

                rlVideoPodcastSurfaceWrapper.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        rlVideoPodcastSurfaceWrapper.setVisibility(View.INVISIBLE);

        updatePodcastView();

        /*
        if (isMyServiceRunning(PodcastPlaybackService.class, this)) {
            Intent intent = new Intent(this, PodcastPlaybackService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        */
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, PodcastPlaybackService.class),
                mConnectionCallbacks,
                null); // optional Bundle

        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onStop() {
        mMTM.unbindDisplayActivity(this);

        super.onStop();

        mMediaBrowser.disconnect();
    }


    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        mPostDelayHandler.delayOnExitTimer();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation != lastOrientation) {
                sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                lastOrientation = currentOrientation;
            }
        }

        super.onWindowFocusChanged(hasWindowFocus);
    }


    int lastOrientation = -1;

    @Override
    protected void onResume() {
        eventBus.register(this);

        if (mMediaBrowser != null && !mMediaBrowser.isConnected()) {
            sliding_layout.setPanelHeight(0);
            sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        eventBus.unregister(this);


        //TODO THIS IS NEVER REACHED!
        isVideoViewVisible = false;
        videoViewInitialized = false;

        eventBus.post(new RegisterVideoOutput(null, null));
        eventBus.post(new RegisterYoutubeOutput(null, false));

        rlVideoPodcastSurfaceWrapper.setVisibility(View.GONE);
        rlVideoPodcastSurfaceWrapper.removeAllViews();

        WidgetProvider.UpdateWidget(this);


        if (NextcloudNotificationManager.isUnreadRssCountNotificationVisible(this)) {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);
            int count = Integer.parseInt(dbConn.getUnreadItemsCountForSpecificFolder(SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS));
            NextcloudNotificationManager.showUnreadRssItemsNotification(this, count);

            if (count == 0) {
                NextcloudNotificationManager.removeRssItemsNotification(this);
            }
        }


        super.onPause();
    }


    /*
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    */

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
        new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                Log.d(TAG, "onConnected() called");

                // Get the token for the MediaSession
                MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                try {
                    // Create a MediaControllerCompat
                    MediaControllerCompat mediaController = new MediaControllerCompat(PodcastFragmentActivity.this, token);

                    // Save the controller
                    MediaControllerCompat.setMediaController(PodcastFragmentActivity.this, mediaController);

                    // Finish building the UI
                    //buildTransportControls();
                } catch (RemoteException e) {
                    Log.e(TAG, "Connecting to podcast service failed!", e);
                }
            }

            @Override
            public void onConnectionSuspended() {
                Log.d(TAG, "onConnectionSuspended() called");
                // The Service has crashed. Disable transport controls until it automatically reconnects
            }

            @Override
            public void onConnectionFailed() {
                Log.e(TAG, "onConnectionFailed() called");
                // The Service has refused our connection
            }
        };

    /*
    private void buildTransportControls() {
        // Grab the view for the play/pause button

        int pbState = MediaControllerCompat.getMediaController(PodcastFragmentActivity.this).getPlaybackState().getState();
        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
            MediaControllerCompat.getMediaController(PodcastFragmentActivity.this).getTransportControls().pause();
        } else {
            MediaControllerCompat.getMediaController(PodcastFragmentActivity.this).getTransportControls().play();
        }
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(PodcastFragmentActivity.this);

        // Display the initial state
        MediaMetadataCompat metadata = mediaController.getMetadata();
        PlaybackStateCompat pbState = mediaController.getPlaybackState();

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);
    }

    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {}

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {}
            };
    */

    public PodcastSlidingUpPanelLayout getSlidingLayout() {
        return sliding_layout;
    }

    public boolean handlePodcastBackPressed() {
        if(mPodcastFragment != null && sliding_layout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            if (!mPodcastFragment.onBackPressed())
                sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return true;
        }
        return false;
    }

    protected void updatePodcastView() {

        if(mPodcastFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mPodcastFragment).commitAllowingStateLoss();
        }

        mPodcastFragment = PodcastFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.podcast_frame, mPodcastFragment)
                .commitAllowingStateLoss();

        if(!currentlyPlaying)
            sliding_layout.setPanelHeight(0);
    }

    @Subscribe
    public void onEvent(UpdatePodcastStatusEvent podcast) {
        boolean playStateChanged = currentlyPlaying;
        //If file is loaded or preparing and podcast is paused/not running expand the view
        currentlyPlaying = podcast.getStatus() == PlaybackService.Status.PLAYING
                            || podcast.getStatus() == PlaybackService.Status.PAUSED;

        //Check if state was changed
        playStateChanged = playStateChanged != currentlyPlaying;

        // If preparing or state changed and is now playing or paused
        if(podcast.getStatus() == PlaybackService.Status.PREPARING
                || (playStateChanged
                    && (podcast.getStatus() == PlaybackService.Status.PLAYING
                        || podcast.getStatus() == PlaybackService.Status.PAUSED
                        || podcast.getStatus() == PlaybackService.Status.STOPPED))) {
            //Expand view
            sliding_layout.setPanelHeight((int) dipToPx(68));
            Log.v(TAG, "expanding podcast view!");
        } else if(playStateChanged) {
            //Hide view
            sliding_layout.setPanelHeight(0);
            currentlyPlaying = false;
            Log.v(TAG, "collapsing podcast view!");
        }

        if (podcast.isVideoFile() && podcast.getVideoType() == PlaybackService.VideoType.Video) {
            if ((!isVideoViewVisible || !videoViewInitialized) && rlVideoPodcastSurfaceWrapper.isPositionReady()) {
                rlVideoPodcastSurfaceWrapper.removeAllViews();
                videoViewInitialized = true;
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
        } else if(podcast.getVideoType() == PlaybackService.VideoType.YouTube) {
            if(BuildConfig.FLAVOR.equals("extra")) {
                if (!videoViewInitialized) {
                    isVideoViewVisible = true;
                    videoViewInitialized = true;
                    rlVideoPodcastSurfaceWrapper.removeAllViews();

                    rlVideoPodcastSurfaceWrapper.setVisibility(View.VISIBLE);

                    togglePodcastVideoViewAnimation();

                    final int YOUTUBE_CONTENT_VIEW_ID = 10101010;
                    FrameLayout frame = new FrameLayout(this);
                    frame.setId(YOUTUBE_CONTENT_VIEW_ID);
                    rlVideoPodcastSurfaceWrapper.addView(frame);
                    //setContentView(frame, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

                    YoutubePlayerManager.StartYoutubePlayer(this, YOUTUBE_CONTENT_VIEW_ID, eventBus, new Runnable() {
                        @Override
                        public void run() {
                            togglePodcastVideoViewAnimation();
                        }
                    });
                }
            } else if(!showedYoutubeFeatureNotAvailableDialog) {
                showedYoutubeFeatureNotAvailableDialog = true;
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.warning))
                        .setMessage(R.string.dialog_feature_not_available)
                        .setCancelable(true)
                        .setPositiveButton(getString(android.R.string.ok), null)
                        .show();
            }
        } else {
            isVideoViewVisible = false;
            videoViewInitialized = false;

            eventBus.post(new RegisterVideoOutput(null, null));
            eventBus.post(new RegisterYoutubeOutput(null, false));

            rlVideoPodcastSurfaceWrapper.setVisibility(View.GONE);
            //AlphaAnimator.AnimateVisibilityChange(rlVideoPodcastSurfaceWrapper, View.GONE);

            rlVideoPodcastSurfaceWrapper.removeAllViews();
        }

    }

    @Subscribe
    public void onEvent(PodcastCompletedEvent podcastCompletedEvent) {
        sliding_layout.setPanelHeight(0);
        sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        currentlyPlaying = false;
    }


    @Subscribe
    public void onEvent(VideoDoubleClicked videoDoubleClicked) {
        appHeight = getWindow().getDecorView().findViewById(android.R.id.content).getHeight();
        appWidth = getWindow().getDecorView().findViewById(android.R.id.content).getWidth();

        if(isFullScreen) {
            rlVideoPodcastSurfaceWrapper.setDisableScale(false);
            togglePodcastVideoViewAnimation();

            //showSystemUI();
        } else {
            //hideSystemUI();

            rlVideoPodcastSurfaceWrapper.setDisableScale(true);
            //oldScaleFactor = rlVideoPodcastSurfaceWrapper.getScaleFactor();

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


    public void togglePodcastVideoViewAnimation() {
        boolean isLeftSliderOpen = false;

        if(this instanceof NewsReaderListActivity && ((NewsReaderListActivity) this).drawerLayout != null) {
            isLeftSliderOpen = ((NewsReaderListActivity) this).drawerLayout.isDrawerOpen(GravityCompat.START);
        }

        int podcastMediaControlHeightDp = pxToDp((int) getResources().getDimension(R.dimen.podcast_media_control_height));

        if(sliding_layout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) { //On Tablets
            animateToPosition(podcastMediaControlHeightDp);
        } else if(isLeftSliderOpen) {
            animateToPosition(0);
        } else {
            animateToPosition(64);
        }
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public void animateToPosition(final int yPosition) {
        appHeight = getWindow().getDecorView().findViewById(android.R.id.content).getHeight();
        appWidth = getWindow().getDecorView().findViewById(android.R.id.content).getWidth();

        final View view = rlVideoPodcastSurfaceWrapper; //surfaceView

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
                    animateToPosition(yPosition);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animator);
        } else {
            int absoluteYPosition = appHeight - view.getHeight() - (int) getResources().getDimension(R.dimen.activity_vertical_margin) - (int) dipToPx(yPosition);
            float xPosition = rlVideoPodcastSurfaceWrapper.getVideoXPosition();

            //TODO podcast video is only working for newer android versions
            view.animate().x(xPosition).y(absoluteYPosition).setDuration(animationTime);
        }




        /*
        int height = (int)(view.getHeight() * scaleFactor);
        int width = (int)(view.getWidth() * scaleFactor);
        view.setScaleX(oldScaleFactor);
        view.setScaleY(oldScaleFactor);
        view.getLayoutParams().height = height;
        view.getLayoutParams().width = width;
        */
    }


    private float dipToPx(float dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    @VisibleForTesting
    public void openMediaItem(final MediaItem mediaItem) {
        Intent intent = new Intent(this, PodcastPlaybackService.class);
        intent.putExtra(PodcastPlaybackService.MEDIA_ITEM, mediaItem);
        startService(intent);

        if(!mMediaBrowser.isConnected()) {
            mMediaBrowser.connect();
        }
        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void openPodcast(final RssItem rssItem) {
        final PodcastItem podcastItem = DatabaseConnectionOrm.ParsePodcastItemFromRssItem(this, rssItem);

        File file = new File(PodcastDownloadService.getUrlToPodcastFile(this, podcastItem.link, false));
        if(file.exists()) {
            podcastItem.link = file.getAbsolutePath();

            openMediaItem(podcastItem);
        } else if(!podcastItem.offlineCached) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                    .setNegativeButton("Abort", null)
                    .setNeutralButton("Download", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PodcastDownloadService.startPodcastDownload(PodcastFragmentActivity.this, podcastItem);

                            Toast.makeText(PodcastFragmentActivity.this, "Starting download of podcast. Please wait..", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setTitle("Podcast")
                    .setMessage("Choose if you want to download or stream the selected podcast");



            alertDialog.setPositiveButton("Stream", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    openMediaItem(podcastItem);
                }
            });

            alertDialog.show();
        }
    }

    @Override
    public void pausePodcast() {
        MediaControllerCompat.getMediaController(PodcastFragmentActivity.this).getTransportControls().pause();
    }

    public void getCurrentPlaybackSpeed(final OnPlaybackSpeedCallback callback) {
        MediaControllerCompat.getMediaController(PodcastFragmentActivity.this)
            .sendCommand(PLAYBACK_SPEED_FLOAT,
                null,
                new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        callback.currentPlaybackReceived(resultData.getFloat(PLAYBACK_SPEED_FLOAT));
                    }
                });
    }

    public boolean getCurrentPlayingPodcast(final OnCurrentPlayingPodcastCallback callback) {
        if(mMediaBrowser != null && mMediaBrowser.isConnected()) {
            MediaControllerCompat.getMediaController(PodcastFragmentActivity.this)
                .sendCommand(CURRENT_PODCAST_ITEM_MEDIA_ITEM,
                    null,
                    new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            callback.currentPlayingPodcastReceived((MediaItem) resultData.getSerializable(CURRENT_PODCAST_ITEM_MEDIA_ITEM));
                        }
                    });
            return true;
        } else {
            return false;
        }
    }

    public interface OnPlaybackSpeedCallback {
        void currentPlaybackReceived(float playbackSpeed);
    }

    public interface OnCurrentPlayingPodcastCallback {
        void currentPlayingPodcastReceived(MediaItem mediaItem);
    }

}

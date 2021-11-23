package de.luhmer.owncloudnewsreader;

import android.app.PictureInPictureParams;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;

import de.luhmer.owncloudnewsreader.events.podcast.RegisterVideoOutput;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.services.PodcastPlaybackService;
import de.luhmer.owncloudnewsreader.services.podcast.PlaybackService;

import static de.luhmer.owncloudnewsreader.services.PodcastPlaybackService.CURRENT_PODCAST_MEDIA_TYPE;

public class PiPVideoPlaybackActivity extends AppCompatActivity {

    private static final String TAG = PiPVideoPlaybackActivity.class.getCanonicalName();
    private EventBus mEventBus;

    private MediaBrowserCompat mMediaBrowser;

    protected static boolean activityIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        ThemeChooser.chooseTheme(this);
        super.onCreate(savedInstanceState);
        ThemeChooser.afterOnCreate(this);

        setContentView(R.layout.activity_pip_video_playback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            //moveTaskToBack(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PictureInPictureParams.Builder pictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
                //Rational aspectRatio = new Rational(vv.getWidth(), vv.getHeight());
                //pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
                enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
            } else {
                enterPictureInPictureMode();
            }
        } else {
            Toast.makeText(this, "This device does not support video playback.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onPictureInPictureModeChanged (boolean isInPictureInPictureMode, Configuration newConfig) {
        Log.d(TAG, "onPictureInPictureModeChanged() called with: isInPictureInPictureMode = [" + isInPictureInPictureMode + "], newConfig = [" + newConfig + "]");

        RelativeLayout surfaceViewWrapper = findViewById(R.id.layout_activity_pip);
        SurfaceView surfaceView = (SurfaceView) surfaceViewWrapper.getChildAt(0);

        if(surfaceView != null) {
            if (isInPictureInPictureMode) {
                surfaceView.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT));
            } else {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                float width = size.x;
                //int height = size.y;
                //int newWidth = (int) (width * (9f/16f));
                int newWidth = (int) (width * (3f/4f));

                surfaceView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, newWidth));
            }
        }

        /*
        if (isInPictureInPictureMode) {
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.

        } else {
            // Restore the full-screen UI.
            //Intent intent = new Intent(this, NewsReaderListActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            //startActivity(intent);

            // Finish PiP Activity
            //finish();
        }
        */

        /*
        // When dismissing
        if(!isInPictureInPictureMode) {
            finish();
        }
        */
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart() called");
        super.onStart();

        mEventBus = EventBus.getDefault();
        //mEventBus.register(this);

        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, PodcastPlaybackService.class),
                mConnectionCallbacks,
                null); // optional Bundle
        mMediaBrowser.connect();

        activityIsRunning = true;
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop() called");

        unregisterVideoViews();

        //mEventBus.unregister(this);

        // (see "stay in sync with the MediaSession")
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback);
        }

        mMediaBrowser.disconnect();

        activityIsRunning = false;

        super.onStop();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            finishAndRemoveTask();
        }
    }

    public void unregisterVideoViews() {
        mEventBus.post(new RegisterVideoOutput(null, null));
    }

    /*
    @Subscribe
    public void onEvent(CollapsePodcastView event) {
        Log.d(TAG, "onEvent() called with: event = [" + event + "]");
        finishAndRemoveTask();
    }
    */


    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed() called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            enterPictureInPictureMode();
        }
    }





    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "onConnected() called");

                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    // Create a MediaControllerCompat
                    MediaControllerCompat mediaController = new MediaControllerCompat(PiPVideoPlaybackActivity.this, token);

                    // Save the controller
                    MediaControllerCompat.setMediaController(PiPVideoPlaybackActivity.this, mediaController);

                    // Register a Callback to stay in sync
                    mediaController.registerCallback(controllerCallback);

                    // Display the initial state
                    MediaMetadataCompat metadata = mediaController.getMetadata();
                    handleMetadataChange(metadata);
                }
            };

        MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    Log.v(TAG, "onMetadataChanged() called with: metadata = [" + metadata + "]");
                    handleMetadataChange(metadata);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat stateCompat) {
                    Log.v(TAG, "onPlaybackStateChanged() called with: state = [" + stateCompat + "]");
                }
            };

    private void handleMetadataChange(MediaMetadataCompat metadata) {
        Log.d(TAG, "handleMetadataChange() called with: metadata = [" + metadata + "]");

        unregisterVideoViews();
        RelativeLayout surfaceViewWrapper = findViewById(R.id.layout_activity_pip);
        surfaceViewWrapper.removeAllViews();

        PlaybackService.VideoType mediaType = PlaybackService.VideoType.valueOf(metadata.getString(CURRENT_PODCAST_MEDIA_TYPE));
        Log.d(TAG, "handleMetadataChange() called with: mediaType = [" + mediaType + "]");

        switch (mediaType) {
            case None:
                finish();
                break;
            case Video:
                // default
                SurfaceView surfaceView = createSurfaceView();
                surfaceViewWrapper.addView(surfaceView);
                mEventBus.post(new RegisterVideoOutput(surfaceView, surfaceViewWrapper));
                break;
            /*
            case YouTube:
                final int YOUTUBE_CONTENT_VIEW_ID = 10101010;
                FrameLayout frame = new FrameLayout(this);
                frame.setId(YOUTUBE_CONTENT_VIEW_ID);
                surfaceViewWrapper.addView(frame);
                YoutubePlayerManager.StartYoutubePlayer(this, YOUTUBE_CONTENT_VIEW_ID, mEventBus, () -> Log.d(TAG, "onInit Success()"));
                break;
            */
            default:
                break;
        }
    }

    private SurfaceView createSurfaceView() {
        SurfaceView surfaceView = new SurfaceView(this);
        surfaceView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        return surfaceView;
    }

}

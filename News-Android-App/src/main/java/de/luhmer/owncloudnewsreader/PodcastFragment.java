package de.luhmer.owncloudnewsreader;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.luhmer.owncloudnewsreader.ListView.PodcastArrayAdapter;
import de.luhmer.owncloudnewsreader.ListView.PodcastFeedArrayAdapter;
import de.luhmer.owncloudnewsreader.events.podcast.SpeedPodcast;
import de.luhmer.owncloudnewsreader.events.podcast.StartDownloadPodcast;
import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.events.podcast.WindPodcast;
import de.luhmer.owncloudnewsreader.model.MediaItem;
import de.luhmer.owncloudnewsreader.model.PodcastFeedItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;
import de.luhmer.owncloudnewsreader.services.PodcastPlaybackService;
import de.luhmer.owncloudnewsreader.services.podcast.PlaybackService;
import de.luhmer.owncloudnewsreader.view.PodcastSlidingUpPanelLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PodcastFragment.OnFragmentInteractionListener} interface
 * to StartYoutubePlayer interaction events.
 * Use the {@link PodcastFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PodcastFragment extends Fragment {

    private static final String TAG = PodcastFragment.class.getCanonicalName();

    private UpdatePodcastStatusEvent podcast;
    private int lastDrawableId;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PodcastFragment.
     */
    public static PodcastFragment newInstance() {
        return new PodcastFragment();
    }
    public PodcastFragment() {
        // Required empty public constructor
    }

    //Your created method
    public boolean onBackPressed() //returns if the event was handled
    {
        return false;
    }


    EventBus eventBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        eventBus = EventBus.getDefault();
    }



    @Override
    public void onResume() {
        eventBus.register(this);

        super.onResume();
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);

        super.onPause();
    }

    @Subscribe
    public void onEvent(StartDownloadPodcast podcast) {
        PodcastDownloadService.startPodcastDownload(getActivity(), podcast.podcast);//, new DownloadReceiver(new Handler(), new WeakReference<ProgressBar>(holder.pbDownloadPodcast)));
    }

    @Subscribe
    public void onEvent(PodcastDownloadService.DownloadProgressUpdate downloadProgress) {
        PodcastArrayAdapter podcastArrayAdapter = (PodcastArrayAdapter) podcastTitleGrid.getAdapter();

        for(int i = 0; i < podcastTitleGrid.getCount(); i++) {
            if(podcastArrayAdapter.getItem(i).link.equals(downloadProgress.podcast.link)) {

                if(!podcastArrayAdapter.getItem(i).downloadProgress.equals(downloadProgress.podcast.downloadProgress)) { //If Progress changed
                    PodcastItem pItem = podcastArrayAdapter.getItem(i);

                    if (downloadProgress.podcast.downloadProgress == 100) {
                        pItem.downloadProgress = PodcastItem.DOWNLOAD_COMPLETED;
                        File file = new File(PodcastDownloadService.getUrlToPodcastFile(getActivity(), pItem.link, false));
                        pItem.offlineCached = file.exists();
                    } else
                        pItem.downloadProgress = downloadProgress.podcast.downloadProgress;
                    podcastTitleGrid.invalidateViews();
                }

                return;
            }
        }
    }

    long lastPodcastRssItemId = -1;
    @Subscribe
    public void onEvent(UpdatePodcastStatusEvent podcast) {
        this.podcast = podcast;

        hasTitleInCache = true;

        int drawableId = podcast.isPlaying() ? R.drawable.ic_action_pause : R.drawable.ic_action_play_arrow;
        int contentDescriptionId = podcast.isPlaying() ? R.string.content_desc_pause : R.string.content_desc_play;

        if(lastDrawableId != drawableId) {
            lastDrawableId = drawableId;
            btnPlayPausePodcast.setImageResource(drawableId);
            btnPlayPausePodcast.setContentDescription(getString(contentDescriptionId));
            btnPlayPausePodcastSlider.setImageResource(drawableId);
        }

        if(lastPodcastRssItemId != podcast.getRssItemId() && imgFavIcon != null) {
            if(loadPodcastFavIcon()) {
                lastPodcastRssItemId = podcast.getRssItemId();
            }
        }

        int hours = (int)(podcast.getCurrent() / (1000*60*60));
        int minutes = (int)(podcast.getCurrent() % (1000*60*60)) / (1000*60);
        int seconds = (int) ((podcast.getCurrent() % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        tvFrom.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        tvFromSlider.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        hours = (int)( podcast.getMax() / (1000*60*60));
        minutes = (int)(podcast.getMax() % (1000*60*60)) / (1000*60);
        seconds = (int) ((podcast.getMax() % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        tvTo.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        tvToSlider.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        tvTitle.setText(podcast.getTitle());
        tvTitleSlider.setText(podcast.getTitle());

        if(podcast.getStatus() == PlaybackService.Status.PREPARING) {
            sb_progress.setVisibility(View.INVISIBLE);
            pb_progress2.setVisibility(View.VISIBLE);

            pb_progress.setIndeterminate(true);
        } else {
            double progress = ((double) podcast.getCurrent() / (double) podcast.getMax()) * 100d;

            if(!blockSeekbarUpdate) {
                sb_progress.setVisibility(View.VISIBLE);
                pb_progress2.setVisibility(View.INVISIBLE);
                sb_progress.setProgress((int) progress);
            }

            pb_progress.setIndeterminate(false);
            pb_progress.setProgress((int) progress);
        }
    }

    private boolean loadPodcastFavIcon() {
        return ((PodcastFragmentActivity) getActivity()).getCurrentPlayingPodcast(
            new PodcastFragmentActivity.OnCurrentPlayingPodcastCallback() {
                @Override
                public void currentPlayingPodcastReceived(MediaItem mediaItem) {
                    Log.d(TAG, "currentPlayingPodcastReceived() called with: mediaItem = [" + mediaItem + "]");
                    if(mediaItem != null) {
                        String favIconUrl = mediaItem.favIcon;
                        Log.d(TAG, "currentPlayingPodcastReceived: " + favIconUrl);
                        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().
                                showImageOnLoading(R.drawable.default_feed_icon_light).
                                showImageForEmptyUri(R.drawable.default_feed_icon_light).
                                showImageOnFail(R.drawable.default_feed_icon_light).
                                build();
                        ImageLoader.getInstance().displayImage(favIconUrl, imgFavIcon, displayImageOptions);
                    }
                }
            });
    }



    @BindView(R.id.btn_playPausePodcast) ImageButton btnPlayPausePodcast;
    @BindView(R.id.btn_playPausePodcastSlider) ImageButton btnPlayPausePodcastSlider;
    @BindView(R.id.btn_nextPodcastSlider) ImageButton btnNextPodcastSlider;
    @BindView(R.id.btn_previousPodcastSlider) ImageButton btnPreviousPodcastSlider;
    @BindView(R.id.btn_podcastSpeed) ImageButton btnPlaybackSpeed;

    @BindView(R.id.img_feed_favicon) ImageView imgFavIcon;

    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.tv_titleSlider) TextView tvTitleSlider;


    @BindView(R.id.tv_from) TextView tvFrom;
    @BindView(R.id.tv_to) TextView tvTo;
    @BindView(R.id.tv_fromSlider) TextView tvFromSlider;
    @BindView(R.id.tv_ToSlider) TextView tvToSlider;

    @BindView(R.id.sb_progress) SeekBar sb_progress;
    @BindView(R.id.pb_progress) ProgressBar pb_progress;
    @BindView(R.id.pb_progress2) ProgressBar pb_progress2;


    @BindView(R.id.podcastFeedList) ListView /* CardGridView CardListView*/ podcastFeedList;
    @BindView(R.id.rlPodcast) RelativeLayout rlPodcast;
    @BindView(R.id.ll_podcast_header) LinearLayout rlPodcastHeader;
    @BindView(R.id.fl_playPausePodcastWrapper) FrameLayout playPausePodcastWrapper;
    @BindView(R.id.podcastTitleGrid) ListView /*CardGridView*/ podcastTitleGrid;

    @BindView(R.id.viewSwitcherProgress) ViewSwitcher /*CardGridView*/ viewSwitcherProgress;


    boolean hasTitleInCache = false;
    @OnClick(R.id.fl_playPausePodcastWrapper) void playPause() {
        if(!hasTitleInCache)
            Toast.makeText(getActivity(), "Please select a title first", Toast.LENGTH_SHORT).show();
        else
            eventBus.post(new TogglePlayerStateEvent());
    }

    @OnClick(R.id.btn_playPausePodcastSlider) void playPauseSlider() {
        playPause();
    }

    @OnClick(R.id.btn_nextPodcastSlider) void nextChapter() {
        eventBus.post(new WindPodcast() {{
            long position = podcast.getCurrent() + 30000;
            toPositionInPercent = ((double) position / (double) podcast.getMax()) * 100d;
        }});
        //Toast.makeText(getActivity(), "This feature is not supported yet :(", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_previousPodcastSlider) void previousChapter() {
        eventBus.post(new WindPodcast() {{
            long position = podcast.getCurrent() - 10000;
            toPositionInPercent = ((double) position / (double) podcast.getMax()) * 100d;
        }});
    }

    @OnClick(R.id.btn_podcastSpeed) void openSpeedMenu() {
        showPlaybackSpeedPicker();
    }

    PodcastSlidingUpPanelLayout sliding_layout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        Context context = new ContextThemeWrapper(getActivity(), R.style.Theme_AppCompat_Light_DarkActionBar);
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(context);
        // inflate using the cloned inflater, not the passed in default
        View view = localInflater.inflate(R.layout.fragment_podcast, container, false);


        //View view = inflater.inflate(R.layout.fragment_podcast, container, false);
        ButterKnife.bind(this, view);


        if(getActivity() instanceof PodcastFragmentActivity) {
            sliding_layout = ((PodcastFragmentActivity) getActivity()).getSlidingLayout();
        }

        if(sliding_layout != null) {
            sliding_layout.setSlideableView(rlPodcast);
            sliding_layout.setDragView(rlPodcastHeader);
            //sliding_layout.setEnableDragViewTouchEvents(true);

            sliding_layout.setPanelSlideListener(onPanelSlideListener);
        }



        PodcastFeedArrayAdapter mArrayAdapter = new PodcastFeedArrayAdapter(getActivity(), new PodcastFeedItem[0]);

        if(mArrayAdapter.getCount() > 0) {
            view.findViewById(R.id.tv_no_podcasts_available).setVisibility(View.GONE);
        }

        podcastTitleGrid.setVisibility(View.GONE);
        podcastFeedList.setVisibility(View.VISIBLE);

        sb_progress.setOnSeekBarChangeListener(onSeekBarChangeListener);

        return view;
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    private SlidingUpPanelLayout.PanelSlideListener onPanelSlideListener = new SlidingUpPanelLayout.PanelSlideListener() {
        @Override
        public void onPanelSlide(View view, float v) {

        }

        @Override
        public void onPanelCollapsed(View view) {
            if(sliding_layout != null)
                sliding_layout.setDragView(rlPodcastHeader);
            viewSwitcherProgress.setDisplayedChild(0);

            if(getActivity() instanceof PodcastFragmentActivity)
                ((PodcastFragmentActivity)getActivity()).togglePodcastVideoViewAnimation();
        }

        @Override
        public void onPanelExpanded(View view) {
            if(sliding_layout != null)
                sliding_layout.setDragView(viewSwitcherProgress);
            viewSwitcherProgress.setDisplayedChild(1);

            if(getActivity() instanceof PodcastFragmentActivity)
                ((PodcastFragmentActivity)getActivity()).togglePodcastVideoViewAnimation();
        }

        @Override
        public void onPanelAnchored(View view) {

        }

        @Override
        public void onPanelHidden(View view) {

        }
    };


    boolean blockSeekbarUpdate = false;
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            //Log.v(TAG, "onProgressChanged");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            blockSeekbarUpdate = true;
            Log.v(TAG, "onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            if(hasTitleInCache) {
                eventBus.post(new WindPodcast() {{
                    toPositionInPercent = seekBar.getProgress();
                }});
                blockSeekbarUpdate = false;
            }
            Log.v(TAG, "onStopTrackingTouch");
        }
    };


    private void showPlaybackSpeedPicker() {
        final NumberPicker numberPicker = new NumberPicker(getContext());
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(PodcastPlaybackService.PLAYBACK_SPEEDS.length-1);
        numberPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.valueOf(PodcastPlaybackService.PLAYBACK_SPEEDS[i]);
            }
        });

        if(getActivity() instanceof PodcastFragmentActivity) {
            ((PodcastFragmentActivity) getActivity()).getCurrentPlaybackSpeed(new PodcastFragmentActivity.OnPlaybackSpeedCallback() {
                @Override
                public void currentPlaybackReceived(float playbackSpeed) {
                    int position = Arrays.binarySearch(PodcastPlaybackService.PLAYBACK_SPEEDS, playbackSpeed);
                    numberPicker.setValue(position);
                }
            });

        } else {
            numberPicker.setValue(3);
        }
        numberPicker.setWrapSelectorWheel(false);


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder.setTitle(getString(R.string.podcast_playback_speed_dialog_title));

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        float speed = PodcastPlaybackService.PLAYBACK_SPEEDS[numberPicker.getValue()];
                        //Toast.makeText(getActivity(), String.valueOf(speed]), Toast.LENGTH_SHORT).show();
                        eventBus.post(new SpeedPodcast(speed));
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setView(numberPicker);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


        // Code below is required to fix bug in Android (default value is not shown) (https://stackoverflow.com/a/30859583)
        try {
            Field f = NumberPicker.class.getDeclaredField("mInputText");
            f.setAccessible(true);
            EditText inputText = (EditText) f.get(numberPicker);
            inputText.setFilters(new InputFilter[0]);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

package de.luhmer.owncloudnewsreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.ListView.PodcastArrayAdapter;
import de.luhmer.owncloudnewsreader.ListView.PodcastFeedArrayAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.events.AudioPodcastClicked;
import de.luhmer.owncloudnewsreader.events.OpenAudioPodcastEvent;
import de.luhmer.owncloudnewsreader.events.PodcastFeedClicked;
import de.luhmer.owncloudnewsreader.events.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.model.AudioPodcastItem;
import de.luhmer.owncloudnewsreader.model.PodcastFeedItem;
import de.luhmer.owncloudnewsreader.services.AudioPodcastService;


/**
 * A simple {@link SherlockFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PodcastFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PodcastFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PodcastFragment extends SherlockFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PodcastFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PodcastFragment newInstance(String param1, String param2) {
        PodcastFragment fragment = new PodcastFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public PodcastFragment() {
        // Required empty public constructor
    }

    //Your created method
    public boolean onBackPressed() //returns if the event was handled
    {
        if(audioPodcasts != null) { //We are in the AudioPodcast view
            audioPodcasts = null;
            podcastTitleGrid.setVisibility(View.GONE);
            podcastFeedList.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }


    EventBus eventBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        eventBus = EventBus.getDefault();

        getActivity().startService(new Intent(getActivity(), AudioPodcastService.class));
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

    public void onEvent(AudioPodcastClicked podcast) {
        final AudioPodcastItem audioPodcast = audioPodcasts.get(podcast.position);

        tvTitle.setText(audioPodcast.title);

        eventBus.post(new OpenAudioPodcastEvent() {{ pathToFile = audioPodcast.link; mediaTitle = audioPodcast.title; }});

        Toast.makeText(getActivity(), "Starting podcast.. please wait", Toast.LENGTH_SHORT).show();
    }

    public void onEvent(PodcastFeedClicked podcast) {
        DatabaseConnection dbConn = new DatabaseConnection(getActivity());
        audioPodcasts = dbConn.getListOfAudioPodcastsForFeed(feedsWithAudioPodcasts.get(podcast.position).itemId);

        PodcastArrayAdapter mArrayAdapter = new PodcastArrayAdapter(getActivity(), audioPodcasts.toArray(new AudioPodcastItem[audioPodcasts.size()]));
        if (podcastTitleGrid != null) {
            podcastTitleGrid.setAdapter(mArrayAdapter);
        }

        podcastTitleGrid.setVisibility(View.VISIBLE);
        podcastFeedList.setVisibility(View.GONE);
    }



    int lastDrawableId;
    public void onEvent(UpdatePodcastStatusEvent podcast) {

        hasTitleInCache = true;

        int drawableId = podcast.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;

        if(lastDrawableId != drawableId) {
            lastDrawableId = drawableId;
            btnPlayPausePodcast.setBackgroundResource(drawableId);
        }

        int hours = (int)( podcast.getCurrent() / (1000*60*60));
        int minutes = (int)(podcast.getCurrent() % (1000*60*60)) / (1000*60);
        int seconds = (int) ((podcast.getCurrent() % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        tvFrom.setText(String.format("%02d:%02d", minutes, seconds));

        hours = (int)( podcast.getMax() / (1000*60*60));
        minutes = (int)(podcast.getMax() % (1000*60*60)) / (1000*60);
        seconds = (int) ((podcast.getMax() % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        tvTo.setText(String.format("%02d:%02d", minutes, seconds));

        tvTitle.setText(podcast.getTitle());

        double progress = ((double)podcast.getCurrent() / (double)podcast.getMax()) * 100d;
        pbProgress.setProgress((int) progress);
    }

    List<AudioPodcastItem> audioPodcasts;
    List<PodcastFeedItem> feedsWithAudioPodcasts;

    @InjectView(R.id.btn_playPausePodcast) ImageButton btnPlayPausePodcast;
    @InjectView(R.id.tv_title) TextView tvTitle;
    @InjectView(R.id.tv_from) TextView tvFrom;
    @InjectView(R.id.tv_to) TextView tvTo;
    @InjectView(R.id.pb_progress) ProgressBar pbProgress;
    @InjectView(R.id.podcastFeedList) ListView /* CardGridView CardListView*/ podcastFeedList;
    @InjectView(R.id.rlPodcast) RelativeLayout rlPodcast;
    @InjectView(R.id.ll_podcast_header) LinearLayout rlPodcastHeader;
    @InjectView(R.id.fl_playPausePodcastWrapper) FrameLayout playPausePodcastWrapper;

    @InjectView(R.id.podcastTitleGrid) ListView /*CardGridView*/ podcastTitleGrid;


    boolean hasTitleInCache = false;
    @OnClick(R.id.fl_playPausePodcastWrapper) void playPause() {
        if(!hasTitleInCache)
            Toast.makeText(getActivity(), "Please select a title first", Toast.LENGTH_SHORT).show();
        else
            eventBus.post(new TogglePlayerStateEvent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_podcast, container, false);
        ButterKnife.inject(this, view);

        if(getActivity() instanceof NewsReaderListActivity) {
            ((NewsReaderListActivity) getActivity()).sliding_layout.setSlideableView(rlPodcast);
            ((NewsReaderListActivity) getActivity()).sliding_layout.setDragView(rlPodcastHeader);
            //((NewsReaderListActivity) getActivity()).sliding_layout.setEnableDragViewTouchEvents(true);
        } else if(getActivity() instanceof NewsDetailActivity) {
            ((NewsDetailActivity) getActivity()).sliding_layout.setSlideableView(rlPodcast);
            ((NewsDetailActivity) getActivity()).sliding_layout.setDragView(rlPodcastHeader);
            //((NewsReaderListActivity) getActivity()).sliding_layout.setEnableDragViewTouchEvents(true);
        }

        DatabaseConnection dbConn = new DatabaseConnection(getActivity());
        feedsWithAudioPodcasts = dbConn.getListOfFeedsWithAudioPodcasts();
        PodcastFeedArrayAdapter mArrayAdapter = new PodcastFeedArrayAdapter(getActivity(), feedsWithAudioPodcasts.toArray(new PodcastFeedItem[feedsWithAudioPodcasts.size()]));
        if (podcastFeedList != null) {
            podcastFeedList.setAdapter(mArrayAdapter);
        }

        if(mArrayAdapter.getCount() > 0) {
            view.findViewById(R.id.tv_no_podcasts_available).setVisibility(View.GONE);
        }

        /*
        ArrayList<Card> cards = new ArrayList<Card>();


        for(String key : feedsWithAudioPodcasts.keySet()) {
            Card card = new CardPodcastFeed(getActivity(), feedsWithAudioPodcasts.get(key), "4 Podcasts verfügbar!");
            //card.setTitle(feedsWithAudioPodcasts.get(key));
            card.setId(key);

            //Set Background resource
            //card.setBackgroundResourceId(R.drawable.card_feed_podcast_background);

            card.setOnClickListener(onFeedCardClickListener);

            cards.add(card);
        }

        CardGridArrayAdapter mCardArrayAdapter = new CardGridArrayAdapter(getActivity(), cards);
        //CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(),cards);
        if (podcastFeedList != null) {
            podcastFeedList.setAdapter(mCardArrayAdapter);
        }*/

        podcastTitleGrid.setVisibility(View.GONE);
        podcastFeedList.setVisibility(View.VISIBLE);

        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}

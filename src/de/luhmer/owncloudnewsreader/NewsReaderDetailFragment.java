package de.luhmer.owncloudnewsreader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.luhmer.owncloudnewsreader.cursor.NewsListCursorAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;

/**
 * A fragment representing a single NewsReader detail screen. This fragment is
 * either contained in a {@link NewsReaderListActivity} in two-pane mode (on
 * tablets) or a {@link NewsReaderDetailActivity} on handsets.
 */
public class NewsReaderDetailFragment extends ListFragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	DatabaseConnection dbConn;
	NewsListCursorAdapter lvAdapter;
	String idSubscription;
	String idFolder;
	String titel;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NewsReaderDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			
			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
					
		}*/
		
		if (getArguments().containsKey(NewsReaderDetailActivity.SUBSCRIPTION_ID)) {
			idSubscription = getArguments().getString(NewsReaderDetailActivity.SUBSCRIPTION_ID);
		}
		if (getArguments().containsKey(NewsReaderDetailActivity.TITEL)) {
			titel = getArguments().getString(NewsReaderDetailActivity.TITEL);
		}
		if (getArguments().containsKey(NewsReaderDetailActivity.FOLDER_ID)) {
			idFolder = getArguments().getString(NewsReaderDetailActivity.FOLDER_ID);
		}
		
		dbConn = new DatabaseConnection(getActivity());
				
		//lvAdapter = new Subscription_ListViewAdapter(this);
		try
		{
			lvAdapter = new NewsListCursorAdapter(getActivity(), getRightCusor());
			setListAdapter(lvAdapter);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

    public Cursor getRightCusor()
    {
        if(idSubscription != null)
            return dbConn.getAllFeedsForSubscription(idSubscription);
        else if(idFolder != null)
            return dbConn.getAllFeedsForFolder(idFolder);
        return null;
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_newsreader_detail, container, false);		
		return rootView;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
				
		Intent intentNewsDetailAct = new Intent(getActivity(), NewsDetailActivity.class);
		if(idSubscription != null)
			intentNewsDetailAct.putExtra(NewsReaderDetailActivity.SUBSCRIPTION_ID, Long.valueOf(idSubscription));
		else if(idFolder != null)
			intentNewsDetailAct.putExtra(NewsReaderDetailActivity.FOLDER_ID, Long.valueOf(idFolder));
		intentNewsDetailAct.putExtra(NewsReaderDetailActivity.ITEM_ID, position);
		intentNewsDetailAct.putExtra(NewsReaderDetailActivity.TITEL, titel);
		startActivityForResult(intentNewsDetailAct, Activity.RESULT_CANCELED);
		
		super.onListItemClick(l, v, position, id);
	}

}

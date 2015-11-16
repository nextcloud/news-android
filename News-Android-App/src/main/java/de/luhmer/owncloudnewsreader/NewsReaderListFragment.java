/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.owncloudnewsreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
import de.luhmer.owncloudnewsreader.model.FolderSubscribtionItem;

/**
 * A list fragment representing a list of NewsReader. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link NewsReaderDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class NewsReaderListFragment extends Fragment implements OnCreateContextMenuListener {
	protected static final String TAG = "NewsReaderListFragment";

    public void ListViewNotifyDataSetChanged()  {
        lvAdapter.NotifyDataSetChangedAsync();
    }

    public void ReloadAdapter() {
        lvAdapter.ReloadAdapterAsync();
    }

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = null;

	public void setRefreshing(boolean isRefreshing) {
		if(isRefreshing) {
			//headerLogo.setImageResource(R.drawable.ic_launcher_background);
			headerLogo.setVisibility(View.INVISIBLE);
			headerLogoProgress.setVisibility(View.VISIBLE);
		} else {
			//headerLogo.setImageResource(R.drawable.ic_launcher);
			headerLogo.setVisibility(View.VISIBLE);
			headerLogoProgress.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		void onChildItemClicked(long idFeed, Long optional_folder_id);
		void onTopItemClicked(long idFeed, boolean isFolder, Long optional_folder_id);
	}


	private SubscriptionExpandableListAdapter lvAdapter;

    @InjectView(R.id.expandableListView) ExpandableListView eListView;
	@InjectView(R.id.urlTextView) protected TextView urlTextView;
	@InjectView(R.id.userTextView) protected TextView userTextView;
	@InjectView(R.id.header_view) protected ViewGroup headerView;
	@InjectView(R.id.header_logo) protected ImageView headerLogo;
	@InjectView(R.id.header_logo_progress) protected View headerLogoProgress;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NewsReaderListFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_newsreader_list, container, false);

        if(!ThemeChooser.isDarkTheme(getActivity())) {
            view.setBackgroundColor(getResources().getColor(R.color.slider_listview_background_color_light_theme));
        }

        ButterKnife.inject(this, view);

        lvAdapter = new SubscriptionExpandableListAdapter(getActivity(), new DatabaseConnectionOrm(getActivity()), eListView);
        lvAdapter.setHandlerListener(expListTextClickedListener);

		eListView.setGroupIndicator(null);

		eListView.setOnChildClickListener(onChildClickListener);
		//eListView.setSmoothScrollbarEnabled(true);

		eListView.setClickable(true);
		eListView.setAdapter(lvAdapter);

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String mUsername = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String mOc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, getString(R.string.app_name));

		userTextView.setText(mUsername);
		urlTextView.setText(mOc_root_path);
		headerView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((NewsReaderListActivity)getActivity()).startSync();
			}
		});

        lvAdapter.notifyDataSetChanged();
        ReloadAdapter();

		return view;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		// Activities containing this fragment must implement its callbacks.
		if (!(context instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) context;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mCallbacks = null;
	}

	ExpListTextClicked expListTextClickedListener = new ExpListTextClicked() {

		@Override
		public void onTextClicked(long idFeed, Context context, boolean isFolder, Long optional_folder_id) {
            mCallbacks.onTopItemClicked(idFeed, isFolder, optional_folder_id);
		}
    };

	OnChildClickListener onChildClickListener = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {

			long idItem = lvAdapter.getChildId(groupPosition, childPosition);

			Long optional_id_folder = null;
			FolderSubscribtionItem groupItem = (FolderSubscribtionItem) lvAdapter.getGroup(groupPosition);
			if(groupItem != null)
				optional_id_folder = groupItem.id_database;

			mCallbacks.onChildItemClicked(idItem, optional_id_folder);

			return false;
		}
	};
}
/*
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

import static de.luhmer.owncloudnewsreader.Constants.USER_INFO_STRING;
import static de.luhmer.owncloudnewsreader.LoginDialogActivity.RESULT_LOGIN;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.databinding.FragmentNewsreaderListBinding;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
import de.luhmer.owncloudnewsreader.model.AbstractItem;
import de.luhmer.owncloudnewsreader.model.ConcreteFeedItem;
import de.luhmer.owncloudnewsreader.model.OcsUser;
import de.luhmer.owncloudnewsreader.reader.nextcloud.OcsAPI;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

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

    protected @Inject ApiProvider mApi;
    protected @Inject SharedPreferences mPrefs;

    private SubscriptionExpandableListAdapter lvAdapter;

    protected FragmentNewsreaderListBinding binding;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = null;


    protected static final String TAG = "NewsReaderListFragment";

    public void listViewNotifyDataSetChanged() {
        lvAdapter.notifyDataSetChangedAsync();
    }

    public void reloadAdapter() {
        lvAdapter.ReloadAdapterAsync();
    }

    public void setRefreshing(boolean isRefreshing) {
        if (isRefreshing) {
            //headerLogo.setImageResource(R.drawable.ic_launcher_background);
            binding.headerLogo.setVisibility(View.INVISIBLE);
			binding.headerLogoProgress.setVisibility(View.VISIBLE);
		} else {
			//headerLogo.setImageResource(R.drawable.ic_launcher);
			binding.headerLogo.setVisibility(View.VISIBLE);
			binding.headerLogoProgress.setVisibility(View.INVISIBLE);
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
		void onTopItemClicked(long idFeed, boolean isFolder, Long onTopItemClicked);
		void onChildItemLongClicked(long idFeed);
		void onTopItemLongClicked(long idFeed, boolean isFolder);
		void onUserInfoUpdated(OcsUser userInfo);
		void onCreateFolderClicked();
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NewsReaderListFragment() {
	}

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        ((NewsReaderApplication) requireActivity().getApplication()).getAppComponent().injectFragment(this);
    }

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
	    binding = FragmentNewsreaderListBinding.inflate(requireActivity().getLayoutInflater(), container, false);

        lvAdapter = new SubscriptionExpandableListAdapter(getActivity(), new DatabaseConnectionOrm(getActivity()), binding.expandableListView, mPrefs);
        lvAdapter.setHandlerListener(expListTextClickedListener);

		binding.expandableListView.setGroupIndicator(null);

		binding.expandableListView.setOnChildClickListener(onChildClickListener);
		binding.expandableListView.setOnItemLongClickListener(onItemLongClickListener);

		binding.expandableListView.setClickable(true);
		binding.expandableListView.setLongClickable(true);
		binding.expandableListView.setAdapter(lvAdapter);

		binding.headerView.setOnClickListener(v -> ((NewsReaderListActivity) requireActivity()).startSync());

        lvAdapter.notifyDataSetChanged();
        reloadAdapter();

        bindNavigationMenu(binding.getRoot(), inflater);

        // move header of sidebar down according to insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerView, (View v, WindowInsetsCompat insets) -> {
            var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // make sure that the end of the sidebar doesn't go behind the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.expandableListView, (View v, WindowInsetsCompat insets) -> {
            var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

		return binding.getRoot();
	}

	@Override
	public void onAttach(@NonNull Context context) {
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

    /**
     * Cares about settings items in news list drawer.
     *  - Binds settings, shown at bottom of drawer
     *  - Inflates NavigationView which is set as footerview of ListView
     *    Currently used to show item "add newsfeed" at bottom of list.
     *
     * @param parent content view of drawer
     * @param inflater inflater provided to fragment
     */
    private void bindNavigationMenu(View parent, LayoutInflater inflater) {
        // Create NavigationView to show as footer of ListView
        View footerView =  inflater.inflate(R.layout.fragment_newsreader_list_footer, null, false);
        ExpandableListView list = parent.findViewById(R.id.expandableListView);

        NavigationView footerNavigation = footerView.findViewById(R.id.listfooterMenu);
        footerNavigation.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_add_new_feed) {
                if (mApi.getNewsAPI() != null) {
                    Intent newFeedIntent = new Intent(getContext(), NewFeedActivity.class);
                    requireActivity().startActivityForResult(newFeedIntent, NewsReaderListActivity.RESULT_ADD_NEW_FEED);
                } else {
                    Intent loginIntent = new Intent(getContext(), LoginDialogActivity.class);
                    requireActivity().startActivityForResult(loginIntent, RESULT_LOGIN);
                }
                return true;
            } else if (itemId == R.id.drawer_settings) {
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                getActivity().startActivityForResult(intent, NewsReaderListActivity.RESULT_SETTINGS);
                return true;
            } else if (itemId == R.id.action_add_new_folder) {
                mCallbacks.onCreateFolderClicked();
                return true;
            }
            return false;
        });
        list.addFooterView(footerView);
    }

	private final ExpListTextClicked expListTextClickedListener = new ExpListTextClicked() {

		@Override
		public void onTextClicked(long idFeed, boolean isFolder, Long optional_folder_id) {
            mCallbacks.onTopItemClicked(idFeed, isFolder, optional_folder_id);
		}

		@Override
		public void onTextLongClicked(long idFeed, boolean isFolder, Long optional_folder_id) {
			mCallbacks.onTopItemLongClicked(idFeed, isFolder);
		}

	};

	// Code below is only used for unit tests
    @VisibleForTesting
	public OnChildClickListener onChildClickListener = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {

            long idItem;
            if(childPosition != -1) {
                idItem = lvAdapter.getChildId(groupPosition, childPosition);
            } else {
                idItem = groupPosition;
            }
			Long optional_id_folder = null;
            AbstractItem groupItem = (AbstractItem) lvAdapter.getGroup(groupPosition);
			if(groupItem != null)
				optional_id_folder = groupItem.id_database;
			if(groupItem instanceof ConcreteFeedItem) {
                idItem = ((ConcreteFeedItem)groupItem).feedId;
            }

			mCallbacks.onChildItemClicked(idItem, optional_id_folder);

			return false;
		}
	};

	AdapterView.OnItemLongClickListener onItemLongClickListener = (parent, view, position, id) -> {
        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int childPosition = ExpandableListView.getPackedPositionChild(id);
            mCallbacks.onChildItemLongClicked(childPosition);
        }

        return true;
    };

    public void startAsyncTaskGetUserInfo() {
        OcsAPI serverAPI = mApi.getServerAPI();

        if(serverAPI == null) {
            return;
        }

        mApi.getServerAPI().user()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull OcsUser userInfo) {
                        Log.d(TAG, "onNext() called with: userInfo = [" + userInfo + "]");

                        try {
                            String userInfoAsString = NewsReaderListFragment.toString(userInfo);
                            //Log.v(TAG, userInfoAsString);
                            mPrefs.edit().putString(USER_INFO_STRING, userInfoAsString).apply();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, "onError() called with:", e);

                        if ("Method Not Allowed".equals(e.getMessage())) { //Remove if old version is used
                            mPrefs.edit().remove(USER_INFO_STRING).apply();
                        }

                        bindUserInfoToUI();
                    }

                    @Override
                    public void onComplete() {
                        bindUserInfoToUI();
                    }
                });
    }


    public void bindUserInfoToUI() {
        if(getActivity() == null) { // e.g. Activity is closed
            return;
        }

        SharedPreferences mPrefs = ((PodcastFragmentActivity) getActivity()).mPrefs;
        if(!mPrefs.contains(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING)) {
            // return if app is not setup yet..
            return;
        }

        String uInfo = mPrefs.getString(USER_INFO_STRING, null);
        if(uInfo == null) {
            return;
        }

        try {
            OcsUser userInfo = (OcsUser) fromString(uInfo);
            mCallbacks.onUserInfoUpdated(userInfo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /** Read the object from Base64 string. */
    public static Object fromString(String s) throws IOException,
            ClassNotFoundException {
        byte [] data = Base64.decode(s, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject(o);
        oos.close();
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}
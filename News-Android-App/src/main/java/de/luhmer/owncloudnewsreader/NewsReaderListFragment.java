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
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
import de.luhmer.owncloudnewsreader.model.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.model.UserInfo;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

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

    @BindView(R.id.expandableListView) protected ExpandableListView eListView;
    @BindView(R.id.urlTextView) protected TextView urlTextView;
    @BindView(R.id.userTextView) protected TextView userTextView;
    @BindView(R.id.header_view) protected ViewGroup headerView;
    @BindView(R.id.header_logo) protected ImageView headerLogo;
    @BindView(R.id.header_logo_progress) protected View headerLogoProgress;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = null;


	@SuppressWarnings("unused")
	protected static final String TAG = "NewsReaderListFragment";

    public void ListViewNotifyDataSetChanged()  {
        lvAdapter.NotifyDataSetChangedAsync();
    }

    public void reloadAdapter() {
        lvAdapter.ReloadAdapterAsync();
    }

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
		void onTopItemClicked(long idFeed, boolean isFolder, Long onTopItemClicked);
		void onChildItemLongClicked(long idFeed);
		void onTopItemLongClicked(long idFeed, boolean isFolder);
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
        ((NewsReaderApplication) getActivity().getApplication()).getAppComponent().injectFragment(this);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_newsreader_list, container, false);

        ButterKnife.bind(this, view);

        loadOwncloudOrNextcloudBanner();

        lvAdapter = new SubscriptionExpandableListAdapter(getActivity(), new DatabaseConnectionOrm(getActivity()), eListView);
        lvAdapter.setHandlerListener(expListTextClickedListener);

		eListView.setGroupIndicator(null);

		eListView.setOnChildClickListener(onChildClickListener);
		eListView.setOnItemLongClickListener(onItemLongClickListener);

		eListView.setClickable(true);
		eListView.setLongClickable(true);
		eListView.setAdapter(lvAdapter);

		headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((NewsReaderListActivity) getActivity()).startSync();
            }
        });

        lvAdapter.notifyDataSetChanged();
        reloadAdapter();

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

	protected void loadOwncloudOrNextcloudBanner() {
        if(!Constants.isNextCloud(getContext())) {
            // Set ownCloud view
            headerView.setBackgroundResource(R.drawable.left_drawer_header_background);
        }
    }

	private ExpListTextClicked expListTextClickedListener = new ExpListTextClicked() {

		@Override
		public void onTextClicked(long idFeed, boolean isFolder, Long optional_folder_id) {
            mCallbacks.onTopItemClicked(idFeed, isFolder, optional_folder_id);
		}

		@Override
		public void onTextLongClicked(long idFeed, boolean isFolder, Long optional_folder_id) {
			mCallbacks.onTopItemLongClicked(idFeed, isFolder);
		}

	};

	public OnChildClickListener onChildClickListener = new OnChildClickListener() {

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

	AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				int childPosition = ExpandableListView.getPackedPositionChild(id);
				mCallbacks.onChildItemLongClicked(childPosition);
			}

			return true;
		}
	};






    public ExpandableListView getListView() {
        return eListView;
    }


    protected void showTapLogoToSyncShowcaseView() {
        new MaterialShowcaseView.Builder(getActivity())
                .setTarget(headerLogo)
                .setDismissText("GOT IT")
                .setContentText("Tap this logo to sync with server")
                .setDelay(300) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse("LOGO_SYNC") // provide a unique ID used to ensure it is only shown once
                .setHideSkipButton(true)
                .show();
    }

    private static final String USER_INFO_STRING = "USER_INFO";

    public void startAsyncTaskGetUserInfo() {
        mApi.getAPI().user()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserInfo>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull UserInfo userInfo) {
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

                        if("Method Not Allowed".equals(e.getMessage())) { //Remove if old version is used
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
        String mUsername = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
        String mOc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null);
        mOc_root_path = mOc_root_path.replace("http://", "").replace("https://", ""); //Remove http:// or https://

        userTextView.setText(mUsername);
        urlTextView.setText(mOc_root_path);

        String uInfo = mPrefs.getString(USER_INFO_STRING, null);
        if(uInfo == null)
            return;

        try {
            UserInfo userInfo = (UserInfo) fromString(uInfo);
            if (userInfo.displayName != null)
                userTextView.setText(userInfo.displayName);

            if (userInfo.avatar != null) {
                Resources r = getResources();
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
                RoundedBitmapDisplayer.RoundedDrawable roundedAvatar =
                        new RoundedBitmapDisplayer.RoundedDrawable(userInfo.avatar, (int) px, 0);
                headerLogo.setImageDrawable(roundedAvatar);
            }
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
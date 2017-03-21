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
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import com.google.gson.stream.JsonReader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
import de.luhmer.owncloudnewsreader.model.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.model.UserInfo;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloudReaderMethods;
import okhttp3.HttpUrl;
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

	@SuppressWarnings("unused")
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
		void onChildItemLongClicked(long idFeed, Long optional_folder_id);
		void onTopItemLongClicked(long idFeed, boolean isFolder, Long optional_folder_id);
	}


	private SubscriptionExpandableListAdapter lvAdapter;

    @Bind(R.id.expandableListView) protected ExpandableListView eListView;
	@Bind(R.id.urlTextView) protected TextView urlTextView;
	@Bind(R.id.userTextView) protected TextView userTextView;
	@Bind(R.id.header_view) protected ViewGroup headerView;
	@Bind(R.id.header_logo) protected ImageView headerLogo;
	@Bind(R.id.header_logo_progress) protected View headerLogoProgress;

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
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.slider_listview_background_color_light_theme));
        }

        ButterKnife.bind(this, view);


        if(!Constants.IsNextCloud(getContext())) {
            // Set ownCloud view
            headerView.setBackgroundResource(R.drawable.left_drawer_header_background);
        }

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
		public void onTextClicked(long idFeed, boolean isFolder, Long optional_folder_id) {
			mCallbacks.onTopItemClicked(idFeed, isFolder, optional_folder_id);
		}

		@Override
		public void onTextLongClicked(long idFeed, boolean isFolder, Long optional_folder_id) {
			mCallbacks.onTopItemLongClicked(idFeed, isFolder, optional_folder_id);
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
				mCallbacks.onChildItemLongClicked(childPosition, null);
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
                .show();
    }

    public void startAsyncTaskGetUserInfo() {
        AsyncTaskHelper.StartAsyncTask(new AsyncTaskGetUserInfo());
    }

    private class AsyncTaskGetUserInfo extends AsyncTask<Void, Void, UserInfo> {
        @Override
        protected UserInfo doInBackground(Void... voids) {
            HttpUrl oc_root_url = HttpJsonRequest.getInstance().getRootUrl();

            try {
                String appVersion = OwnCloudReaderMethods.GetVersionNumber(oc_root_url);
                API api = API.GetRightApiForVersion(appVersion, HttpJsonRequest.getInstance().getRootUrl());

                int[] version = API.ExtractVersionNumberFromString(appVersion);
                if(version[0] < 6 || version[0] == 6 && version[1] <= 4) //Supported since 6.0.5
                    return null; //API NOT SUPPORTED!


                // Update shared prefs
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                mPrefs.edit().putString(Constants.NEWS_WEB_VERSION_NUMBER_STRING, appVersion).apply();

                UserInfo.Builder ui = new UserInfo.Builder();
                InputStream inputStream = HttpJsonRequest.getInstance().PerformJsonRequest(api.getUserUrl());

                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
                reader.beginObject();

                String currentName;
                while(reader.hasNext() && (currentName = reader.nextName()) != null) {
                    switch(currentName) {
                        case "userId":
                            ui.setUserId(reader.nextString());
                            break;
                        case "displayName":
                            ui.setDisplayName(reader.nextString());
                            break;
                        case "avatar":
                            com.google.gson.stream.JsonToken jt = reader.peek();
                            if(jt == com.google.gson.stream.JsonToken.NULL) {
                                Log.v(TAG, "No image available");
                                reader.skipValue();
                                //No image available
                            } else {
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    currentName = reader.nextName();
                                    if (currentName.equals("data")) {
                                        String encodedImage = reader.nextString();
                                        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                                        ui.setAvatar(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                                        Log.v(TAG, encodedImage);
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                            }
                            break;
                        default:
                            Log.v(TAG, "Skipping value for: " + currentName);
                            reader.skipValue();
                            break;
                    }
                }
                reader.close();

                return ui.build();
            } catch (Exception e) {
                if(e.getMessage().equals("Method Not Allowed")) { //Remove if old version is used
                    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    mPrefs.edit().remove("USER_INFO").commit();
                }
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(UserInfo userInfo) {
            if(userInfo != null) {
                try {
                    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    mPrefs.edit().putString("USER_INFO", NewsReaderListFragment.toString(userInfo)).commit();

                    bindUserInfoToUI();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            super.onPostExecute(userInfo);
        }
    }

    protected void bindUserInfoToUI() {
        bindUserInfoToUI(false);
    }

    public void bindUserInfoToUI(boolean testMode) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String mUsername = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
        String mOc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, getString(R.string.app_name));
        mOc_root_path = mOc_root_path.replace("http://", "").replace("https://", ""); //Remove http:// or https://

        userTextView.setText(mUsername);
        urlTextView.setText(mOc_root_path);

        if(testMode) { //Hide real url in test mode
            urlTextView.setText("example.com/ownCloud");
        }

        String uInfo = mPrefs.getString("USER_INFO", null);
        if(uInfo == null)
            return;

        try {
            UserInfo userInfo = (UserInfo) fromString(uInfo);
            if (userInfo.mDisplayName != null)
                userTextView.setText(userInfo.mDisplayName);

            if (userInfo.mAvatar != null) {
                Resources r = getResources();
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
                RoundedBitmapDisplayer.RoundedDrawable roundedAvatar =
                        new RoundedBitmapDisplayer.RoundedDrawable(userInfo.mAvatar, (int) px, 0);
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
package de.luhmer.owncloudnewsreader.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.SSOException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.GsonConfig;
import de.luhmer.owncloudnewsreader.reader.nextcloud.NewsAPI;
import de.luhmer.owncloudnewsreader.reader.nextcloud.OcsAPI;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.NextcloudRetrofitApiBuilder;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by david on 26.05.17.
 */

public class ApiProvider {

    private static final String TAG = ApiProvider.class.getCanonicalName();
    private final MemorizingTrustManager mMemorizingTrustManager;
    protected final SharedPreferences mPrefs;
    protected Context context;
    private NextcloudAPI mNextcloudSsoApi;

    protected NewsAPI mNewsApi;
    private OcsAPI mServerApi;



    public ApiProvider(MemorizingTrustManager mtm, SharedPreferences sp, Context context) {
        this.mMemorizingTrustManager = mtm;
        this.mPrefs = sp;
        this.context = context;
        initApi(new NextcloudAPI.ApiConnectedListener() {
            @Override
            public void onConnected() { }

            @Override
            public void onError(Exception ex) { }
        });
    }

    public void initApi(@NonNull NextcloudAPI.ApiConnectedListener apiConnectedListener) {
        if(mNextcloudSsoApi != null) {
            // Destroy previous Service Connection if we need to reconnect (e.g. login again)
            mNextcloudSsoApi.close();
            mNextcloudSsoApi = null;
        }

        boolean useSSO = mPrefs.getBoolean(SettingsActivity.SW_USE_SINGLE_SIGN_ON, false);
        if(useSSO) {
            initSsoApi(apiConnectedListener);
        } else {
            if(mPrefs.contains(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING)) {
                String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, "");
                String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, "");
                String baseUrlStr = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null);
                HttpUrl baseUrl = HttpUrl.parse(baseUrlStr).newBuilder()
                        .addPathSegments("index.php/apps/news/api/v1-2/")
                        .build();
                Log.d("ApiModule", "HttpUrl: " + baseUrl);
                OkHttpClient client = OkHttpSSLClient.GetSslClient(baseUrl, username, password, mPrefs, mMemorizingTrustManager);
                initRetrofitApi(baseUrl, client);
                apiConnectedListener.onConnected();
            } else {
                apiConnectedListener.onError(new Exception("no login data"));
            }
        }
    }

    private void initRetrofitApi(HttpUrl baseUrl, OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(GsonConfig.GetGson()))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .client(client)
                .build();

        mNewsApi = retrofit.create(NewsAPI.class);
        mServerApi = null;
    }

    protected void initSsoApi(final NextcloudAPI.ApiConnectedListener callback) {
        try {
            SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
            mNextcloudSsoApi = new NextcloudAPI(context, ssoAccount, GsonConfig.GetGson(), callback);
            mNewsApi = new NextcloudRetrofitApiBuilder(mNextcloudSsoApi, NewsAPI.mApiEndpoint).create(NewsAPI.class);
            mServerApi = new NextcloudRetrofitApiBuilder(mNextcloudSsoApi, OcsAPI.mApiEndpoint).create(OcsAPI.class);
        } catch (SSOException e) {
            callback.onError(e);
        }
    }

    public NewsAPI getNewsAPI() {
        return mNewsApi;
    }

    public OcsAPI getServerAPI() {
        return mServerApi;
    }

    @VisibleForTesting
    public void setAPI(NewsAPI newsApi) {
        this.mNewsApi = newsApi;
    }
}

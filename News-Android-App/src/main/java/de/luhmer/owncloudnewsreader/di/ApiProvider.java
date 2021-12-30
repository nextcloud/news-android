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
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.GsonConfig;
import de.luhmer.owncloudnewsreader.reader.OkHttpImageDownloader;
import de.luhmer.owncloudnewsreader.reader.nextcloud.NewsAPI;
import de.luhmer.owncloudnewsreader.reader.nextcloud.OcsAPI;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.NextcloudRetrofitApiBuilder;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
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
            mNextcloudSsoApi.stop();
            mNextcloudSsoApi = null;
        }

        boolean useSSO = mPrefs.getBoolean(SettingsActivity.SW_USE_SINGLE_SIGN_ON, false);
        if(useSSO) {
            OkHttpClient client = new OkHttpClient.Builder().build();
            initImageLoader(mPrefs, client, context);
            initSsoApi(apiConnectedListener);
        } else {
            if(mPrefs.contains(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING)) {
                String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, "");
                String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, "");
                String baseUrlStr = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null);
                HttpUrl baseUrl = HttpUrl.parse(baseUrlStr).newBuilder()
                        .addPathSegments("index.php/apps/news/api/v1-2/")
                        .build();
                Log.d("ApiModule", "HttpUrl: " + baseUrl.toString());
                OkHttpClient client = OkHttpSSLClient.GetSslClient(baseUrl, username, password, mPrefs, mMemorizingTrustManager);
                initImageLoader(mPrefs, client, context);
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
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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



    private void initImageLoader(SharedPreferences mPrefs, OkHttpClient okHttpClient, Context context) {
        String cacheSize = mPrefs.getString(SettingsActivity.SP_MAX_CACHE_SIZE,"500");
        int diskCacheSize = Integer.parseInt(cacheSize)*1024*1024;
        if(ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .diskCacheSize(diskCacheSize)
                .memoryCacheSize(10 * 1024 * 1024)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .defaultDisplayImageOptions(imageOptions)
                .imageDownloader(new OkHttpImageDownloader(context, okHttpClient))
                .build();

        ImageLoader.getInstance().init(config);
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

package de.luhmer.owncloudnewsreader.di;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Created by david on 22.05.17.
 */

@Module
public class ApiModule {

    private Application mApplication;

    public ApiModule(Application application) {
        this.mApplication = application;
    }

    // Dagger will only look for methods annotated with @Provides
    @Provides
    @Singleton
    @VisibleForTesting
    // Application reference must come from AppModule.class
    public SharedPreferences providesSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication);
    }

    /*
    @Provides
    @Singleton
    NextcloudAPI providexNextcloudAPI() {
        return new NextcloudAPI("");
    }*/

    /*
    @Provides
    @Singleton
    Cache provideOkHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(application.getCacheDir(), cacheSize);
        return cache;
    }*/

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache) {
        OkHttpClient client = new OkHttpClient();
        //client.setCache(cache);
        return client;
    }

    @Provides
    @Singleton
    PostDelayHandler providePostDelayHandler() {
        return new PostDelayHandler(mApplication);
    }


    /*
    @Provides
    @Singleton
    Retrofit provideRetrofit(String baseUrl, Gson gson, OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();
        return retrofit;
    }
    */

    @Provides
    @Singleton
    MemorizingTrustManager provideMTM() {
        return new MemorizingTrustManager(mApplication);
    }

    @Provides
    @Singleton
    ApiProvider provideAPI(MemorizingTrustManager mtm, SharedPreferences sp) {
        return new ApiProvider(mtm, sp, mApplication);
    }

}
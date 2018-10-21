package de.luhmer.owncloudnewsreader.tests;

import android.app.Application;
import android.content.SharedPreferences;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Provides;
import de.luhmer.owncloudnewsreader.di.ApiModule;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;

public class TestApiModule extends ApiModule {

    private Application mApplication;

    public TestApiModule(Application application) {
        super(application);
        this.mApplication = application;
    }

    @Override
    public SharedPreferences providesSharedPreferences() {
        return Mockito.mock(SharedPreferences.class);
    }

    @Provides
    @Singleton
    ApiProvider provideAPI(MemorizingTrustManager mtm, SharedPreferences sp) {
        return new ApiProvider(mtm, sp, mApplication);
    }

}

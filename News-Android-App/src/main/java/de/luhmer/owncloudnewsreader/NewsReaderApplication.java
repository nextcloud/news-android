package de.luhmer.owncloudnewsreader;

import android.app.Application;

import de.luhmer.owncloudnewsreader.di.ApiModule;
import de.luhmer.owncloudnewsreader.di.AppComponent;
import de.luhmer.owncloudnewsreader.di.DaggerAppComponent;
import de.luhmer.owncloudnewsreader.helper.AdBlocker;

public class NewsReaderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initDaggerAppComponent();

        AdBlocker.init(this);
    }

    public void initDaggerAppComponent() {
        // Dagger%COMPONENT_NAME%

        mAppComponent = DaggerAppComponent.builder()
                .apiModule(new ApiModule(this))
                .build();

        // If a Dagger 2 component does not have any constructor arguments for any of its modules,
        // then we can use .create() as a shortcut instead:
        //mAppComponent = DaggerAppComponent.create();
    }

    private AppComponent mAppComponent;

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    public void setAppComponent(AppComponent appComponent) {
        this.mAppComponent = appComponent;
    }
}

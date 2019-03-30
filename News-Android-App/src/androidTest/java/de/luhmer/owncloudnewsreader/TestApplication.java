package de.luhmer.owncloudnewsreader;

import android.app.Application;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import de.luhmer.owncloudnewsreader.di.ApiModule;
import de.luhmer.owncloudnewsreader.di.AppComponent;
import de.luhmer.owncloudnewsreader.di.DaggerAppComponent;
import de.luhmer.owncloudnewsreader.di.TestApiModule;
import de.luhmer.owncloudnewsreader.helper.AdBlocker;
import de.luhmer.owncloudnewsreader.helper.ForegroundListener;

public class TestApplication extends NewsReaderApplication {

    public void initDaggerAppComponent() {
        // Dagger%COMPONENT_NAME%

        mAppComponent = DaggerAppComponent.builder()
                .apiModule(new TestApiModule(this))
                .build();

        // If a Dagger 2 component does not have any constructor arguments for any of its modules,
        // then we can use .create() as a shortcut instead:
        //mAppComponent = DaggerAppComponent.create();
    }

}

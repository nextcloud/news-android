package de.luhmer.owncloudnewsreader;

import de.luhmer.owncloudnewsreader.di.DaggerTestComponent;
import de.luhmer.owncloudnewsreader.di.TestApiModule;

public class TestApplication extends NewsReaderApplication {

    @Override
    public void initDaggerAppComponent() {
        // Dagger%COMPONENT_NAME%

        mAppComponent = DaggerTestComponent.builder()
                .apiModule(new TestApiModule(this))
                .build();

        // If a Dagger 2 component does not have any constructor arguments for any of its modules,
        // then we can use .create() as a shortcut instead:
        //mAppComponent = DaggerAppComponent.create();
    }

}

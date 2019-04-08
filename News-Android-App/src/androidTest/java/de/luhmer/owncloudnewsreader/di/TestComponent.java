package de.luhmer.owncloudnewsreader.di;

import javax.inject.Singleton;

import dagger.Component;
import de.luhmer.owncloudnewsreader.tests.NewFeedTests;
import de.luhmer.owncloudnewsreader.tests.NewsReaderListActivityUiTests;
import de.luhmer.owncloudnewsreader.tests.NightModeTest;

@Singleton
@Component(modules = { ApiModule.class })
public interface TestComponent extends AppComponent {

    void inject(NewFeedTests newFeedTest);
    void inject(NightModeTest nightModeTest);

    void inject(NewsReaderListActivityUiTests newsReaderListActivityUiTests);
}

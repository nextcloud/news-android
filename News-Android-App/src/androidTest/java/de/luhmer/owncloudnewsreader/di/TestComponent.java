package de.luhmer.owncloudnewsreader.di;

import com.nextcloud.android.sso.api.NewFeedTests;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { ApiModule.class })
public interface TestComponent extends AppComponent {
    void inject(NewFeedTests test);
}

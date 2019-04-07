package de.luhmer.owncloudnewsreader.di;

import javax.inject.Singleton;

import dagger.Component;
import de.luhmer.owncloudnewsreader.LoginDialogFragment;
import de.luhmer.owncloudnewsreader.NewFeedActivity;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.NewsReaderListDialogFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListFragment;
import de.luhmer.owncloudnewsreader.PodcastFragmentActivity;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.SettingsFragment;
import de.luhmer.owncloudnewsreader.authentication.OwnCloudSyncAdapter;
import de.luhmer.owncloudnewsreader.services.SyncItemStateService;

/**
 * Created by david on 22.05.17.
 */

@Singleton
@Component(modules = { ApiModule.class })
public interface AppComponent {

    void injectActivity(NewsReaderListActivity activity);
    void injectActivity(PodcastFragmentActivity activity);
    void injectActivity(NewFeedActivity activity);
    void injectActivity(SettingsActivity activity);

    void injectFragment(NewsReaderListDialogFragment fragment);
    void injectFragment(NewsReaderListFragment fragment);
    void injectFragment(LoginDialogFragment fragment);
    void injectFragment(SettingsFragment settingsFragment);

    void injectService(SyncItemStateService service);
    void injectService(OwnCloudSyncAdapter ownCloudSyncAdapter);
}

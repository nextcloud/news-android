package de.luhmer.owncloudnewsreader.di;

import javax.inject.Singleton;

import dagger.Component;
import de.luhmer.owncloudnewsreader.AddFolderDialogFragment;
import de.luhmer.owncloudnewsreader.FolderOptionsDialogFragment;
import de.luhmer.owncloudnewsreader.LoginDialogActivity;
import de.luhmer.owncloudnewsreader.NewFeedActivity;
import de.luhmer.owncloudnewsreader.NewsDetailActivity;
import de.luhmer.owncloudnewsreader.NewsDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.NewsReaderListDialogFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListFragment;
import de.luhmer.owncloudnewsreader.PodcastFragmentActivity;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.SettingsFragment;
import de.luhmer.owncloudnewsreader.authentication.OwnCloudSyncAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.services.SyncItemStateService;
import de.luhmer.owncloudnewsreader.widget.WidgetProvider;

/**
 * Created by david on 22.05.17.
 */

@Singleton
@Component(modules = { ApiModule.class })
public interface AppComponent {

    void injectActivity(NewsReaderListActivity activity);
    void injectActivity(NewsDetailActivity activity);
    void injectActivity(PodcastFragmentActivity activity);
    void injectActivity(NewFeedActivity activity);
    void injectActivity(SettingsActivity activity);
    void injectActivity(LoginDialogActivity activity);

    void injectFragment(NewsReaderListDialogFragment fragment);
    void injectFragment(NewsReaderListFragment fragment);
    void injectFragment(SettingsFragment fragment);
    void injectFragment(NewsDetailFragment fragment);
    void injectFragment(NewsReaderDetailFragment fragment);
    void injectFragment(FolderOptionsDialogFragment fragment);
    void injectFragment(AddFolderDialogFragment fragment);

    void injectService(SyncItemStateService service);
    void injectService(OwnCloudSyncAdapter ownCloudSyncAdapter);

    void injectWidget(WidgetProvider widgetProvider);

    void injectDatabaseConnection(DatabaseConnectionOrm databaseConnectionOrm);
}

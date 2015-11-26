package de.luhmer.owncloudnewsreader;

import android.os.Bundle;
import android.support.annotation.Nullable;

import de.luhmer.owncloudnewsreader.helper.ThemeChooser;

/**
 * Created by benson on 11/20/15.
 */
public class DirectoryChooserActivity extends net.rdrei.android.dirchooser.DirectoryChooserActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme = ThemeChooser.isDarkTheme(getApplicationContext())
                ? R.style.DirectoryChooserTheme
                : R.style.DirectoryChooserTheme_Light;
        setTheme(theme);
    }
}


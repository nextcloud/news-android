package de.luhmer.owncloudnewsreader;

import android.os.Bundle;
import androidx.annotation.Nullable;

/**
 * Created by benson on 11/20/15.
 */
public class DirectoryChooserActivity extends net.rdrei.android.dirchooser.DirectoryChooserActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme = R.style.AppTheme;
        setTheme(theme);
    }
}


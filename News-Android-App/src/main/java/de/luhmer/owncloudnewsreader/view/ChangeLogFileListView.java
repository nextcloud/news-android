package de.luhmer.owncloudnewsreader.view;

import android.content.Context;
import android.util.AttributeSet;

import it.gmariotti.changelibs.library.view.ChangeLogListView;

/**
 * Thin wrapper around changeloglib to load local xml files by path
 * after the view has already been instanciated.
 */
public class ChangeLogFileListView extends ChangeLogListView {

    public ChangeLogFileListView(Context context) {
        this(context, null);
    }

    public ChangeLogFileListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChangeLogFileListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @param path  local xml path staring with "file://"
     */
    public void loadFile(String path) {
        mChangeLogFileResourceUrl = path;
        super.initAdapter();
    }

    @Override
    protected void initAdapter() {
        // do nothing yet - will be called in loadFile()
    }
}

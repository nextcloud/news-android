package de.luhmer.owncloudnewsreader.view

import android.content.Context
import android.util.AttributeSet
import it.gmariotti.changelibs.library.view.ChangeLogListView

/**
 * Thin wrapper around changeloglib to load local xml files by path
 * after the view has already been instantiated.
 */
class ChangeLogFileListView
    @JvmOverloads
    constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : ChangeLogListView(context, attrs, defStyle) {
        /**
         * @param path  local xml path staring with "file://"
         */
        fun loadFile(path: String) {
            mChangeLogFileResourceUrl = path
            super.initAdapter()
        }

        override fun initAdapter() {
            // do nothing yet - will be called in loadFile()
        }
    }

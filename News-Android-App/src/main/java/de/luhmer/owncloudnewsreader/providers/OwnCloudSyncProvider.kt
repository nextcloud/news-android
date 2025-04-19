package de.luhmer.owncloudnewsreader.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

class OwnCloudSyncProvider : ContentProvider() {
    /*
     * Always return true, indicating that the
     * provider loaded correctly.
     */
    override fun onCreate(): Boolean = true

    /*
     * Return an empty String for MIME type
     */
    override fun getType(uri: Uri): String = ""

    /*
     * query() always returns no results
     *
     */
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? = null

    /*
     * insert() always returns null (no URI)
     */
    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    /*
     * delete() always returns "no rows affected" (0)
     */
    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int = 0

    /*
     * update() always returns "no rows affected" (0)
     */
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int = 0
}

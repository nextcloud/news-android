/*
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package de.luhmer.owncloudnewsreader.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import de.luhmer.owncloudnewsreader.SettingsActivity
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm.SORT_DIRECTION
import java.io.File

const val DATABASE_NAME = "OwncloudNewsReader.db"

fun copyDatabaseToSdCard(context: Context): Boolean {
    val path = context.getDatabasePath(DATABASE_NAME).path
    val db = File(path)
    val backupDb = getPath(context)
    if (db.exists()) {
        try {
            val parentFolder = backupDb.parentFile
            parentFolder?.mkdirs()
            db.copyTo(backupDb, true)
            return true
        } catch (ignore: Exception) {
            Log.e("DatabaseUtils", "copyDatabaseToSdCard: ", ignore)
        }
    }
    return false
}

fun getPath(context: Context): File =
    File(
        NewsFileUtils.getCacheDirPath(context) + "/dbBackup/" + DATABASE_NAME,
    )

fun getSortDirectionFromSettings(prefs: SharedPreferences): SORT_DIRECTION {
    val default = SORT_DIRECTION.desc
    val sortDirection = prefs.getString(SettingsActivity.SP_SORT_ORDER, default.toString())
    return sortDirection?.toInt()?.let {
        SORT_DIRECTION.values().getOrNull(it)
    } ?: default
}

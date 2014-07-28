/**
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

package de.luhmer.owncloudnewsreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import de.luhmer.owncloudnewsreader.database.model.DaoMaster;
import de.luhmer.owncloudnewsreader.database.model.DaoSession;

public class DatabaseHelperOrm {
    public static final String DATABASE_NAME_ORM = "OwncloudNewsReaderOrm.db";


    private static DaoSession daoSession;


    public static synchronized DaoSession getDaoSession(Context context)
    {
        if(daoSession == null) {
            // As we are in development we will use the DevOpenHelper which drops the database on a schema update
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DATABASE_NAME_ORM, null);
            // Access the database using the helper
            SQLiteDatabase db = helper.getWritableDatabase();
            // Construct the DaoMaster which brokers DAOs for the Domain Objects
            DaoMaster daoMaster = new DaoMaster(db);
            // Create the session which is a container for the DAO layer and has a cache which will return handles to the same object across multiple queries
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }
}

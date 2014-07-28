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

package de.luhmer.owncloudnewsreader.ListView;

import android.content.Context;

import de.luhmer.owncloudnewsreader.async_tasks.IGetTextForTextViewAsyncTask;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;

@Deprecated
public class UnreadFolderCount implements IGetTextForTextViewAsyncTask {

	Context context;
	String idDatabase;
	
	public UnreadFolderCount(Context context, String idDatabase) { 
		this.context = context;
		this.idDatabase = idDatabase;
	}
	
	@Override
	public String getText() {
		DatabaseConnection dbConn = new DatabaseConnection(context);
		int unread = 0;
		try
		{
			unread = dbConn.getCountFeedsForFolder(idDatabase, true);
		} finally {
			dbConn.closeDatabase();
		}
        
        return String.valueOf(unread);
	}
}

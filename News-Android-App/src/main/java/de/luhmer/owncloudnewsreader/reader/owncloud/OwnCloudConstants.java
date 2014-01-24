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

package de.luhmer.owncloudnewsreader.reader.owncloud;

public class OwnCloudConstants {

	//public static final String ROOT_PATH = "/ocs/v1.php/apps/news/";
	public static final String ROOT_PATH_APIv1 = "/ocs/v1.php/apps/news/";
	public static final String ROOT_PATH_APIv2 = "/index.php/apps/news/api/v1-2/";
	public static final String FOLDER_PATH = "folders";
	public static final String SUBSCRIPTION_PATH = "feeds";
	public static final String FEED_PATH = "items";
	public static final String FEED_PATH_UPDATED_ITEMS = "items/updated";
	public static final String VERSION_PATH = "version";
	public static final String JSON_FORMAT = "?format=json";
}

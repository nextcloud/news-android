package de.luhmer.owncloudnewsreader.reader.owncloud;

public class OwnCloudConstants {

	//public static final String ROOT_PATH = "/ocs/v1.php/apps/news/";
	public static final String ROOT_PATH = "/index.php/apps/news/api/v1-2/";
	public static final String FOLDER_PATH = ROOT_PATH + "folders";
	public static final String SUBSCRIPTION_PATH = ROOT_PATH + "feeds";
	public static final String FEED_PATH = ROOT_PATH + "items";
	public static final String FEED_PATH_UPDATED_ITEMS = ROOT_PATH + "items/updated";
	public static final String VERSION_PATH = ROOT_PATH + "version";
	public static final String JSON_FORMAT = "?format=json";
}

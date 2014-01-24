package de.luhmer.owncloudnewsreader.reader.GoogleReaderApi;

public class GoogleReaderConstants {
	public static final String _AUTHPARAMS = "GoogleLogin auth=";
	public static final String _GOOGLE_LOGIN_URL = "https://www.google.com/accounts/ClientLogin";
	public static final String _READER_BASE_URL  = "http://www.google.com/reader/";
	public static final String _API_URL = _READER_BASE_URL + "api/0/";
	public static final String _TOKEN_URL = _API_URL + "token";
	public static final String _USER_INFO_URL = _API_URL + "user-info";
	public static final String _USER_LABEL = "user/-/label/";
	public static final String _TAG_LIST_URL = _API_URL + "tag/list";
	//public static final String _EDIT_TAG_URL = _API_URL + "tag/edit";
	public static final String _EDIT_TAG_URL = _API_URL + "edit-tag";	
	public static final String _RENAME_TAG_URL = _API_URL + "rename-tag";
	public static final String _DISABLE_TAG_URL = _API_URL + "disable-tag";
	public static final String _SUBSCRIPTION_URL = _API_URL + "subscription/edit";
	public static final String _SUBSCRIPTION_LIST_URL = _API_URL + "subscription/list";
	
	public static final String _STATE_READ = "user/-/state/com.google/read";
	public static final String _STATE_STARRED= "user/-/state/com.google/starred";
	public static final String _STATE_BROADCAST = "user/-/state/com.google/broadcast";
	public static final String _STATE_LIKE = "user/-/state/com.google/like";
	
	
	
	public static final String APP_NAME = "Owncloud News Reader";
}

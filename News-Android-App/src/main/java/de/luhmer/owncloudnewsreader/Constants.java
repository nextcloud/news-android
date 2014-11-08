package de.luhmer.owncloudnewsreader;

public class Constants {
	public static final Boolean DEBUG_MODE = true;
	public static final Boolean debugModeWidget = true;


    public static final String _TAG_LABEL_UNREAD = "stream/contents/user/-/state/com.google/reading-list?n=1000&r=n&xt=user/-/state/com.google/read";
    public static final String _TAG_LABEL_STARRED = "stream/contents/user/-/state/com.google/starred?n=20";

    //public static final String LAST_SYNC = "LAST_SYNC";

    public static final int maxItemsCount = 1500;


    public static final int TaskID_GetVersion = -10;
    public static final int TaskID_GetFolder = 1;
    public static final int TaskID_GetFeeds = 2;
    public static final int TaskID_GetItems = 3;
    public static final int TaskID_PerformStateChange = -1;

    public enum SYNC_TYPES { SYNC_TYPE__ITEM_STATES,
    						SYNC_TYPE__FOLDER,
    						SYNC_TYPE__FEEDS,
    						SYNC_TYPE__ITEMS,
    						SYNC_TYPE__GET_API};


    public static final String LAST_UPDATE_NEW_ITEMS_COUNT_STRING = "LAST_UPDATE_NEW_ITEMS_COUNT_STRING";


    public static final String SHOW_CASE_APP_STARTED_SHOWN_BOOLEAN = "FIRST_TIME_APP_STARTED_BOOLEAN";

    /*
    private static final String _P_KEY_PART1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgNmCsMj6M4zkjpKRG8MG5+yIAyqSQE2etVkZsc3s";
    private static final String _P_KEY_PART2 = "V5zLoQ/NUOVC0fS2tj8IWk6UYqQGk9rmVold3sDGiTCvWGFecjwel3qxzz23hKLlemrv2+0WPrZ5KOqiaCEMi";
    private static final String _P_KEY_PART3 = "CeQ7zgpcytcQdD9Y/aeaHJ9P27ntn0ub6H1Bx3VDRHm4Jkg6LQnqmdIpEmeIztnoMFlLXTaVKapaFmqJGX9ar";
    private static final String _P_KEY_PART4 = "RizGd9kqtgAqIP7YnGGDV1vP/MqYpegJkOMOlxhuVvXUsg7t7hBLdGXsJ572DzUK/2/fbZ+PIcG7OF4RgJV7Yb";
    private static final String _P_KEY_PART5 = "/AVD0ssqydMlwuheOG82FCqhBtw2vShAz7mkWgL0l0u5HQIDAQAB";

    public static String getBase64EncodedPublicKey()
    {
    	return _P_KEY_PART1 + _P_KEY_PART2 + _P_KEY_PART3 + _P_KEY_PART4 + _P_KEY_PART5;
    }
    */
}

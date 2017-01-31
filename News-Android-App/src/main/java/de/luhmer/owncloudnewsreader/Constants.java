package de.luhmer.owncloudnewsreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.luhmer.owncloudnewsreader.reader.owncloud.API;

public class Constants {
	public static final Boolean debugModeWidget = false;

    //public static final String LAST_SYNC = "LAST_SYNC";

    public static final int maxItemsCount = 1500;

    public static final String LAST_UPDATE_NEW_ITEMS_COUNT_STRING = "LAST_UPDATE_NEW_ITEMS_COUNT_STRING";


    public static final String NEWS_WEB_VERSION_NUMBER_STRING = "NewsWebVersionNumber";


    public static boolean IsNextCloud(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int[] version = API.ExtractVersionNumberFromString(mPrefs.getString(Constants.NEWS_WEB_VERSION_NUMBER_STRING, ""));
        if(version[0] >= 9) {
            return true;
        }
        return false;
    }

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

package de.luhmer.owncloudnewsreader;

public class Constants {
	public static final Boolean DEBUG_MODE = true;
	
	public static final String UPDATE_SERVER_HOSTNAME = "https://ourhomework.de/N43Z5W5T6721903JS98SFD7";    
    public static final String FILENAME = "OwncloudNewsReader.apk";
    
    public static final String _TAG_LABEL_UNREAD = "stream/contents/user/-/state/com.google/reading-list?n=1000&r=n&xt=user/-/state/com.google/read";
    public static final String _TAG_LABEL_STARRED = "stream/contents/user/-/state/com.google/starred?n=20";
    
    
    private static final String _P_KEY_PART1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgNmCsMj6M4zkjpKRG8MG5+yIAyqSQE2etVkZsc3s";
    private static final String _P_KEY_PART2 = "V5zLoQ/NUOVC0fS2tj8IWk6UYqQGk9rmVold3sDGiTCvWGFecjwel3qxzz23hKLlemrv2+0WPrZ5KOqiaCEMi";
    private static final String _P_KEY_PART3 = "CeQ7zgpcytcQdD9Y/aeaHJ9P27ntn0ub6H1Bx3VDRHm4Jkg6LQnqmdIpEmeIztnoMFlLXTaVKapaFmqJGX9ar";
    private static final String _P_KEY_PART4 = "RizGd9kqtgAqIP7YnGGDV1vP/MqYpegJkOMOlxhuVvXUsg7t7hBLdGXsJ572DzUK/2/fbZ+PIcG7OF4RgJV7Yb";
    private static final String _P_KEY_PART5 = "/AVD0ssqydMlwuheOG82FCqhBtw2vShAz7mkWgL0l0u5HQIDAQAB";
    
    public static String getBase64EncodedPublicKey()
    {
    	return _P_KEY_PART1 + _P_KEY_PART2 + _P_KEY_PART3 + _P_KEY_PART4 + _P_KEY_PART5; 
    }
}

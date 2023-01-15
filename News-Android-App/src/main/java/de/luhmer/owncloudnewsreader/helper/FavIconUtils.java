package de.luhmer.owncloudnewsreader.helper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class FavIconUtils {

    private static final String TAG = FavIconUtils.class.getCanonicalName();

    public static String fixFavIconUrl(String favIconUrl) {
        if (favIconUrl == null) {
            return null;
        }

        if (favIconUrl.startsWith("https://i2.wp.com/stadt-bremerhaven.de/wp-content/uploads/2014/12/logo")) {
            // Fix favicon for cachys blog...
            return "https://stadt-bremerhaven.de/wp-content/uploads/2018/08/sblogo-150x150.jpg";
        }
        return favIconUrl;

        /*
        try {
            favIconUrl = decodeSpecialChars(favIconUrl);
        }catch(Exception ex) {
            Log.e(TAG, ex.toString());
        }
        return fixSvgIcons(favIconUrl);
        */
    }

    protected static String decodeSpecialChars(String favIconUrl) throws UnsupportedEncodingException {

        String before = favIconUrl;
        int idx = favIconUrl.indexOf("?");
        String path = favIconUrl;
        if(idx > 0) {
            path = favIconUrl.substring(0, idx);
        }
        favIconUrl = favIconUrl.replace(path, URLDecoder.decode(path, StandardCharsets.UTF_8.name()));

        /*
        URL url = Objects.requireNonNull(HttpUrl.parse(favIconUrl)).url();

        String pathDecoded = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.name());
        String portPostfix = (url.getPort() != -1) ? String.valueOf(url.getPort()) : "";

        // some urls use specials chars which for some reason cause issues in Glide
        // e.g.
        // https://i2.wp.com/stadt-bremerhaven.de/wp-content/uploads/2014/12/logo-gro%C3%9F-549c81bbv1_site_icon.png?fit=32%2C32
        // https://i2.wp.com//stadt-bremerhaven.de/wp-content/uploads/2014/12/logo-groß-549c81bbv1_site_icon.png?fit=32%2C32

        favIconUrl = String.format("%s://%s%s/%s", url.getProtocol(), url.getHost(), portPostfix, pathDecoded);


        if(url.getQuery() != null) {
            favIconUrl = favIconUrl + "?" + url.getQuery();
        }

        */
        //Log.d(TAG, "before: " + before);
        //Log.d(TAG, "after: " + favIconUrl);

        return favIconUrl;
    }

    protected static String fixSvgIcons(String favIconUrl) {
        if(favIconUrl.endsWith(".svg")) {
            favIconUrl = String.format("https://images.weserv.nl?url=%s&output=webp", favIconUrl);
        }
        return favIconUrl;
    }
}

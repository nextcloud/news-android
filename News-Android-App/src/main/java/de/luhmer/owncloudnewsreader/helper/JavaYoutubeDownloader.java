package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import de.luhmer.owncloudnewsreader.collections.IKeyValuePair;
import de.luhmer.owncloudnewsreader.collections.KeyValuePair;
import de.luhmer.owncloudnewsreader.collections.MapUtils;
import de.luhmer.owncloudnewsreader.collections.URLParams;

public class JavaYoutubeDownloader {

    public static String newline = System.getProperty("line.separator");
    public static final String scheme = "http";
    public static final String host = "www.youtube.com";
    public static final Pattern commaPattern = Pattern.compile(",");
    public static final char[] ILLEGAL_FILENAME_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };






    public String getVideoID( String urlString ){
        int start = urlString.indexOf("?v=") + 3;
        int end = urlString.indexOf("&", start);
        if ( end == -1 ){
            end = urlString.length();
        }

        if(start == 2) { //Doesn't work
            start = urlString.indexOf("/v/") + 3;
            end = urlString.indexOf("?", start);
            if (end == -1){
                end = urlString.length();
            }
        }

        return urlString.substring(start, end);
    }

    public String getExtension(int format) {
        return "mp4";
    }


    public String getDownloadUrl(String url, Context context) throws Throwable {

        //Map<String, String> videoInfo = getVideoInfo(getVideoID(url));// Html.fromHtml(getStringFromWebsite("http://youtube.com/get_video_info?video_id=" + getVideoID(url))).toString();
        /*

        byte[] b = videoInfo.getBytes();
        String videoInfoAscii = new String(b, "ASCII");
        */

        //getFullPathOfPodcastYoutubeFile(url, context), ".mp4"

        //String urlTemp = videoInfo.get("sig");

        return getDownloadUrl(getVideoID(url), "UTF-8", "ownCloud News Reader");
    }

    public static File getFullPathOfPodcastYoutubeFile(String WEB_URL_TO_FILE, Context context) throws Exception
    {
        String rootPath = FileUtils.getPathPodcasts(context);
        URL url = new URL(WEB_URL_TO_FILE.trim());

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(url.toString().getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);

        return new File(rootPath + "/" + hashtext  + ".mp4");
    }


    public String getDownloadUrl(String videoId, String encoding, String userAgent) throws Throwable {
              //Utils.log.fine("Retrieving " + videoId);
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("video_id", videoId));
        URI uri = getUri("get_video_info", qparams);

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(uri);
        httpget.setHeader("User-Agent", userAgent);

        //Utils.log.finer("Executing " + uri);
        HttpResponse response = httpclient.execute(httpget, localContext);
        HttpEntity entity = response.getEntity();
        if (entity != null && response.getStatusLine().getStatusCode() == 200) {
            InputStream instream = entity.getContent();
            String videoInfo = getStringFromInputStream(encoding, instream);
            if (videoInfo != null && videoInfo.length() > 0) {
                List<NameValuePair> infoMap = new ArrayList<NameValuePair>();
                URLEncodedUtils.parse(infoMap, new Scanner(videoInfo), encoding);
                String downloadUrl = null;
                String filename = videoId;
                int bestQuality = -1;
                for (NameValuePair pair : infoMap) {
                    String key = pair.getName();
                    String val = pair.getValue();
                    //Utils.log.finest(key + "=" + val);
                    if (key.equals("title")) {
                        filename = val;
                    } else if (key.equals("url_encoded_fmt_stream_map")) {
                        String[] formats = commaPattern.split(val);

                        String fmtString = null;
                        //for (String fmt : formats) {
                        String fmt = formats[0]; {
                            int itagLocation = fmt.indexOf("itag=");
                            if ( itagLocation == -1 ) continue;
                            itagLocation += 5;
                            String subStr = null;
                            try {
                                subStr = fmt.substring(itagLocation, fmt.indexOf("&", itagLocation));
                            }
                            catch( IndexOutOfBoundsException ex){
                                return "Could not find the itag attribute to determine quality";
                            }
                            int tempQuality = Integer.parseInt(fmt.substring(itagLocation, fmt.indexOf("&", itagLocation)));
                            if ( bestQuality < tempQuality ){
                                bestQuality = tempQuality;
                                fmtString = fmt;
                            }

                        }
                        //we are going to automatically download the best quality youtube
                        int begin = fmtString.indexOf("url=");
                        int sig = fmtString.indexOf("sig=");
                        if (begin != -1) {
                            int end = fmtString.indexOf("&", begin + 4);
                            int end2 = fmtString.indexOf("&", sig + 4);
                            if (end == -1) {
                                end = fmtString.length();
                            }
                            if (end2 == -1 ){
                                end2 = fmtString.length();
                            }
                            String tempURL = fmtString.substring(begin+ 4, end );
                            String signatureURL = URLEncoder.encode("&signature="+fmtString.substring(sig + 4, end2), "UTF-8");
                            //downloadUrl = new String(URLCodec.decodeUrl((tempURL + signatureURL).getBytes()));
                            downloadUrl = URLDecoder.decode((tempURL + signatureURL), "UTF-8");

                            return downloadUrl;
                            //break;
                        }
                    }
                }

                /*
                if ( downloadUrl == null ){
                    //Utils.log.fine("Content is protected");
                }
                filename = cleanFilename(filename);
                if (filename.length() == 0) {
                    filename = videoId;
                } else {
                    filename += "_" + videoId;
                }
                filename += "." + extension;
                File outputfile = new File(outputdir, filename);
                if (downloadUrl != null) {
                    downloadWithHttpClient(userAgent, downloadUrl, outputfile);
                }
                */
            }
        }
        return "successful";
    }

    public void downloadWithHttpClient(String userAgent, String downloadUrl, File outputfile) throws Throwable {

        HttpGet httpget2 = new HttpGet(downloadUrl);
        //Utils.log.finer("Executing " + httpget2.getURI());
        HttpClient httpclient2 = new DefaultHttpClient();

        HttpResponse response2 = httpclient2.execute(httpget2);
        HttpEntity entity2 = response2.getEntity();

        if (entity2 != null && response2.getStatusLine().getStatusCode() == 200) {
            long length = entity2.getContentLength();
            InputStream instream2 = entity2.getContent();
            //Utils.log.finer("Writing " + length + " bytes to " + outputfile);
            if (outputfile.exists()) {
                outputfile.delete();
            }
            FileOutputStream outstream = new FileOutputStream(outputfile);
            try {
                byte[] buffer = new byte[2048];
                int count = -1;
                while ((count = instream2.read(buffer)) != -1) {
                    outstream.write(buffer, 0, count);
                }
                outstream.flush();
            } finally {
                outstream.close();
            }
        }
    }

    private String cleanFilename(String filename) {
        for (char c : ILLEGAL_FILENAME_CHARACTERS) {
            filename = filename.replace(c, '_');
        }
        return filename;
    }

    private static URI getUri(String path, List<NameValuePair> qparams) throws URISyntaxException {
        URI uri = URIUtils.createURI(scheme, host, -1, "/" + path, URLEncodedUtils.format(qparams, "UTF-8"), null);
        return uri;
    }

    private String getStringFromInputStream(String encoding, InputStream instream) throws UnsupportedEncodingException, IOException {
        Writer writer = new StringWriter();

        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(instream, encoding));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            instream.close();
        }
        String result = writer.toString();
        return result;
    }


    private String getStringFromWebsite(String url) {
        String response = "";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();

            BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(content));
            String s = "";
            //Charset cs = Charset.forName("ASCII"); // Or whatever encoding you want


            while ((s = buffer.readLine()) != null) {
                response += s;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }





    /**
     * Gets all parameters we can figure out about this youTubeId by scraping YouTube.
     * The URL for the underlying video will have the key "location".
     * @param youTubeId A YouTube ID
     * @return A map with all parameters as key-value pairs.
     * @throws IOException if we have networking troubles
     */
    public static Map<String, String> getVideoInfo(String youTubeId) throws IOException
    {
        final Map<String, String> retval = new HashMap<String, String>();
        getVideoInfo(youTubeId, retval);
        return retval;
    }
    /**
     * Gets all parameters we can figure out about this youTubeId by scraping YouTube.
     * The URL for the underlying video will have the key "location".
     * @param youTubeId A YouTube ID
     * @param map A map to fill with data; it is not cleared first.
     * @throws IOException if we have networking troubles
     */
    public static void getVideoInfo(String youTubeId, Map<String, String> map) throws IOException
    {
        final String host = "http://www.youtube.com";
        final List<IKeyValuePair> params = new ArrayList<IKeyValuePair>();
        params.add(new KeyValuePair("video_id", youTubeId));
        final String urlString = host + "/get_video_info?&"+ URLParams.generateQueryString(params);

        final URL url;
        try
        {
            url = new URL(urlString);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("malformed url: " + urlString, e);
        }
        HttpURLConnection conn;
        BufferedInputStream in;
        byte[] data = new byte[4096]; // bad Art; fixed size

        conn = (HttpURLConnection)url.openConnection();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            // badness.
            conn.disconnect();
            throw new RuntimeException("could not get video info: " + urlString);
        }
        in = new BufferedInputStream(conn.getInputStream());
        int offset=0;
        do {
            offset = in.read(data, offset, data.length-offset);
        } while(offset >= 0 && offset < data.length);
        conn.disconnect();
        // convert to string; ugh
        String response = new String(data, "UTF-8");
        System.out.println("Response: "+response);
        // convert into parameter map
        final Map<String, String> youTubeParams = MapUtils.listToMap(URLParams.parseQueryString(response), MapUtils.ListToMapMode.FIRST_WINS);


        final String token = youTubeParams.get("token");
        //if (token == null)
            //throw new RuntimeException("Could not find youtube token: "+ urlString +"; response="+response);


        params.clear();
        params.add(new KeyValuePair("video_id", youTubeId));
        params.add(new KeyValuePair("t", token));
        final String location = host + "/get_video?"+URLParams.generateQueryString(params);
        map.put("location", location);
        map.putAll(youTubeParams);

    }
}



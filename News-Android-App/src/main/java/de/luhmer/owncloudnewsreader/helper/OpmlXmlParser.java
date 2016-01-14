package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;

/**
 * Created by David on 14.01.2016.
 */
public class OpmlXmlParser {

    //Create XML
    public static String GenerateOPML(Context context) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "opml");
            serializer.attribute("", "version", "2.0");

            serializer.startTag("", "head");
            serializer.startTag("", "title");
            serializer.text("Subscriptions");
            serializer.endTag("", "title");
            serializer.endTag("", "head");

            serializer.startTag("", "body");


            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
            List<Folder> folderList = dbConn.getListOfFolders();
            List<Feed> feedList = dbConn.getListOfFeeds();

            //Process all feeds in folders
            for(Folder folder : folderList) {
                serializer.startTag("", "outline");
                serializer.attribute("", "title", folder.getLabel());
                serializer.attribute("", "text", folder.getLabel());

                for(Feed feed : folder.getFeedList()) {
                    feedList.remove(feed);//Remove feed from feedlist (So only feeds without folders will remain)
                    GenerateXMLForFeed(serializer, feed);
                }
                serializer.endTag("", "outline");
            }

            //All feeds without folder
            for(Feed feed : feedList) {
                GenerateXMLForFeed(serializer, feed);
            }

            serializer.endTag("", "body");
            serializer.endTag("", "opml");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void GenerateXMLForFeed(XmlSerializer serializer, Feed feed) throws IOException {
        serializer.startTag("", "outline");
        serializer.attribute("", "title", feed.getFeedTitle());
        serializer.attribute("", "text", feed.getFeedTitle());
        serializer.attribute("", "type", "rss");
        serializer.attribute("", "xmlUrl", feed.getLink());
        //serializer.attribute("", "htmlUrl", key);
        serializer.endTag("", "outline");
    }


    //Parse XML

    // We don't use namespaces
    private static final String ns = null;
    public static HashMap<String, String> ReadFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        HashMap<String, String> extractedUrls = new HashMap<>();

        parser.require(XmlPullParser.START_TAG, ns, "opml");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("body")) {
                extractedUrls.putAll(readFolder(parser));
            } else {
                Skip(parser);
            }
        }
        return extractedUrls;
    }

    private static class Entry {
        public Entry(String folderName, String feedUrl) {
            this.feedUrl = feedUrl;
            this.folderName = folderName;
        }

        public String folderName;
        public String feedUrl;
    }


    private static HashMap<String, String> readFolder(XmlPullParser parser) throws XmlPullParserException, IOException {
        HashMap<String, String> extractedUrls = new HashMap<>();

        String name;
        String folderName = null;

        parser.require(XmlPullParser.START_TAG, ns, "body");

        while(parser.next() >= 0) { //Loop over all
            if(parser.getEventType() == XmlPullParser.END_TAG) { //If read endtag and folder Name is != null
                if(folderName == null) { //If end tag is read and we aren't exiting a folder --> exit!
                    break;
                }
                folderName = null;
            }
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            name = parser.getName();
            if (name.equals("outline")) {
                Entry entry = ReadOutline(parser);
                if (entry.folderName != null) {
                    folderName = entry.folderName;
                } else {
                    entry.folderName = folderName;
                    extractedUrls.put(entry.feedUrl, entry.folderName);
                    parser.next(); //Read closing tag
                }
            }
        }

        return extractedUrls;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private static Entry ReadOutline(XmlPullParser parser) throws XmlPullParserException, IOException {
        //parser.require(XmlPullParser.START_TAG, ns, "outline");

        String link = parser.getAttributeValue(null, "xmlUrl");
        String title = null;
        if(link == null) { //Parse folder title if no feedUrl is available
            title = parser.getAttributeValue(null, "title");
        }

        return new Entry(title, link);
    }

    private static void Skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}

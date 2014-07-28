package de.luhmer.owncloudnewsreader.database.generator;

/**
 * Created by David on 18.07.2014.
 */

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * Version 1 of the Schema definition
 *
 * @author Jeremy
 */
public class Version3 extends SchemaVersion {

    /**
     * Constructor
     *
     * @param current
     */
    public Version3(boolean current) {
        super(current);

        Schema schema = getSchema();
        addEntitysToSchema(schema);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersionNumber() {
        return 3;
    }

    private static void addEntitysToSchema(Schema schema) {

        /* Folder */
        Entity folder = schema.addEntity("Folder");
        Property folderId = folder.addIdProperty().notNull().getProperty();
        folder.addStringProperty("label").notNull();

        /* Feed */
        Entity feed = schema.addEntity("Feed");
        Property feedId = feed.addIdProperty().notNull().getProperty();
        Property folderIdProperty = feed.addLongProperty("folderId").getProperty();

        feed.addStringProperty("feedTitle").notNull();
        feed.addStringProperty("faviconUrl");
        feed.addStringProperty("link");
        feed.addStringProperty("avgColour");



        /* RSS Item */
        Entity rssItem = schema.addEntity("RssItem");
        Property rssItemId = rssItem.addIdProperty().notNull().getProperty();
        Property rssItemFeedId = rssItem.addLongProperty("feedId").notNull().getProperty();

        rssItem.addStringProperty("link");
        rssItem.addStringProperty("title");
        rssItem.addStringProperty("body");
        rssItem.addBooleanProperty("read");
        rssItem.addBooleanProperty("starred");
        rssItem.addStringProperty("author").notNull();
        rssItem.addStringProperty("guid").notNull();
        rssItem.addStringProperty("guidHash").notNull();
        rssItem.addBooleanProperty("read_temp");
        rssItem.addBooleanProperty("starred_temp");
        rssItem.addDateProperty("lastModified");
        rssItem.addDateProperty("pubDate");


        rssItem.addStringProperty("enclosureLink");
        rssItem.addStringProperty("enclosureMime");


        feed.addToOne(folder, folderIdProperty);
        folder.addToMany(feed, folderIdProperty);

        feed.addToMany(rssItem, rssItemFeedId);
        rssItem.addToOne(feed, rssItemFeedId);




        Entity rssItemView = schema.addEntity("CurrentRssItemView");
        rssItemView.addIdProperty().notNull();
        rssItemView.addLongProperty("rssItemId").notNull();


        rssItem.implementsInterface("HasId<Long>");
    }
}

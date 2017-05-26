package de.luhmer.owncloudnewsreader.reader.nextcloud;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;

/**
 * Created by david on 26.05.17.
 */

public class ItemMap {
    private final Set<Map<String, Object>> items = new HashSet<>();

    public ItemMap(Iterable<String> itemIds, DatabaseConnectionOrm dbConn) {
        for(String idItem : itemIds)
        {
            RssItem rssItem = dbConn.getRssItemById(Long.parseLong(idItem));
            HashMap<String, Object> itemMap = new HashMap<>();
            itemMap.put("feedId", rssItem.getFeedId());
            itemMap.put("guidHash", rssItem.getGuidHash());
            this.items.add(itemMap);
        }
    }

    public Set<Map<String, Object>> getItems() {
        return items;
    }
}

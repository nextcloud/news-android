package de.luhmer.owncloudnewsreader.reader.nextcloud;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 26.05.17.
 */

public class ItemIds {
    private final Set<Long> items = new HashSet<>();

    public ItemIds(Iterable<String> items) {
        for (String itemId : items) {
            this.items.add(Long.parseLong(itemId));
        }
    }

    public Set<Long> getItems() {
        return items;
    }
}

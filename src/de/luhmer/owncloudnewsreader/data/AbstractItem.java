package de.luhmer.owncloudnewsreader.data;

/**
 * Created by David on 24.05.13.
 */
public abstract class AbstractItem {
    public long id_database;

    AbstractItem(long id_database)
    {
        this.id_database = id_database;
    }
}

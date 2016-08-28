package de.luhmer.owncloudnewsreader.services.events;

import com.google.auto.value.AutoValue;

/**
 * Created by David on 26.08.2016.
 */
@AutoValue
public abstract class SyncFailedEvent {

    public abstract Exception exception();

    public static SyncFailedEvent create(Exception exception) {
        return new AutoValue_SyncFailedEvent(exception);
    }

}

package de.luhmer.owncloudnewsreader.services.events;

import com.google.auto.value.AutoValue;

/**
 * Created by David on 26.08.2016.
 */
@AutoValue
public abstract class SyncFailedEvent {

    public abstract Throwable exception();

    public static SyncFailedEvent create(Throwable exception) {
        return new AutoValue_SyncFailedEvent(exception);
    }

}

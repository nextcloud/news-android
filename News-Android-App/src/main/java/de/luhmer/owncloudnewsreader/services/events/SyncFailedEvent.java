package de.luhmer.owncloudnewsreader.services.events;

/**
 * Created by David on 26.08.2016.
 */
public class SyncFailedEvent {

    private Throwable throwable;

    public SyncFailedEvent(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable exception() {
        return throwable;
    }

    public static SyncFailedEvent create(Throwable exception) {
        return new SyncFailedEvent(exception);
    }

}

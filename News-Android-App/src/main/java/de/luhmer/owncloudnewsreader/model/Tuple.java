package de.luhmer.owncloudnewsreader.model;

/**
 * Created by David on 27.03.2015.
 */
public class Tuple<E, T> {
    public final E key;
    public final T value;
    public Tuple(E key, T value) {
        this.key = key;
        this.value = value;
    }
}

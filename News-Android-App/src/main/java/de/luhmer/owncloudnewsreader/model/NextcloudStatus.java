package de.luhmer.owncloudnewsreader.model;

/**
 * Created by david on 26.05.17.
 */

public class NextcloudStatus {

    public String version;
    public Warnings warnings;

    class Warnings {
        public String improperlyConfiguredCron;
    }
}

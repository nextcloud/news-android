package de.luhmer.owncloudnewsreader.reader.nextcloud;

/**
 * Created by david on 24.05.17.
 */

public enum Types {

    FOLDERS("folders"),
    FEEDS("feeds"),
    ITEMS("items");

    private final String text;

    /**
     * @param text
     */
    private Types(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}

package de.luhmer.owncloudnewsreader.reader.nextcloud;

import com.google.gson.JsonObject;

/**
 * Created by david on 24.05.17.
 */

public interface IHandleJsonObject {
    boolean performAction(JsonObject jObj);
}

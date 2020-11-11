package de.luhmer.owncloudnewsreader.reader.nextcloud;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import de.luhmer.owncloudnewsreader.model.OcsUser;

/**
 * Created by david on 24.05.17.
 */

public class NextcloudServerDeserializer<T> implements JsonDeserializer<T> {

    private final String mKey;
    private final Class<T> mType;


    public NextcloudServerDeserializer(String key, Class<T> type) {
        this.mKey = key;
        this.mType = type;
    }

    public static final String TAG = NextcloudServerDeserializer.class.getCanonicalName();

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(typeOfT == OcsUser.class) {
            return (T) this.parseOcsUser(json.getAsJsonObject());
        }
        return null;
    }

    private static OcsUser parseOcsUser(JsonObject obj) {
        OcsUser ocsUser = new OcsUser();
        JsonElement data = obj.get("ocs").getAsJsonObject().get("data");
        if (!data.isJsonNull()) {
            JsonObject user = data.getAsJsonObject();
            if (user.has("id")) {
                ocsUser.setId(user.get("id").getAsString());
            }
            if (user.has("displayname")) {
                ocsUser.setDisplayName(user.get("displayname").getAsString());
            } else if (user.has("display-name")) {
                ocsUser.setDisplayName(user.get("display-name").getAsString());
            }
        }
        return ocsUser;
    }
}

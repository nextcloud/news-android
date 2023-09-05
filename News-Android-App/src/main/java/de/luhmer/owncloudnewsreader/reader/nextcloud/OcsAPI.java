package de.luhmer.owncloudnewsreader.reader.nextcloud;

import de.luhmer.owncloudnewsreader.model.OcsUser;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;

public interface OcsAPI {

    String mApiEndpoint = "/ocs/v2.php/";

    @GET("cloud/user?format=json")
    Observable<OcsUser> user();

}

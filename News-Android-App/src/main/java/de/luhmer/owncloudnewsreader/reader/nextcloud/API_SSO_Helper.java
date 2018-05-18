package de.luhmer.owncloudnewsreader.reader.nextcloud;

import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import de.luhmer.owncloud.accountimporter.helper.NextcloudAPI;
import de.luhmer.owncloud.accountimporter.helper.NextcloudRequest;
import io.reactivex.Completable;
import io.reactivex.functions.Action;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class API_SSO_Helper {

    public static ResponseBody getResponseBodyFromRequest(NextcloudAPI nextcloudAPI, NextcloudRequest request) {
        final ParcelFileDescriptor output;
        try {
            output = nextcloudAPI.performNetworkRequest(request);
            InputStream os = new ParcelFileDescriptor.AutoCloseInputStream(output);
            return ResponseBody.create(null, 0, new BufferedSourceSSO(os));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ResponseBody.create(null, "");
    }

    public static Completable WrapInCompletable(final NextcloudAPI nextcloudAPI, final NextcloudRequest request) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws IOException, RemoteException {
                nextcloudAPI.performRequest(Void.class, request);
            }
        });
    }

    public static <T> Call<T> WrapInCall(final NextcloudAPI nextcloudAPI, final NextcloudRequest nextcloudRequest, final Type resType) {
        return new Call<T>() {
            @Override
            public Response<T> execute() throws IOException {
                try {
                    T body = nextcloudAPI.performRequest(resType, nextcloudRequest);
                    return Response.success(body);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void enqueue(Callback<T> callback) {
                try {
                    T body = nextcloudAPI.performRequest(resType, nextcloudRequest);
                    callback.onResponse(null, Response.success(body));
                } catch (RemoteException e) {
                    callback.onResponse(null, Response.<T>error(520, ResponseBody.create(null, e.toString())));
                } catch (IOException e) {
                    callback.onResponse(null, Response.<T>error(520, ResponseBody.create(null, e.toString())));
                }
            }

            @Override
            public boolean isExecuted() {
                return false;
            }

            @Override
            public void cancel() {

            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Override
            public Call<T> clone() {
                return null;
            }

            @Override
            public Request request() {
                return null;
            }
        };
    }

    public static Call<Void> WrapVoidCall(final boolean success) {
        return new Call<Void>() {
            @Override
            public Response<Void> execute() {
                if(success) {
                    return Response.success(null);
                } else {
                    return Response.error(520, null);
                }
            }

            @Override
            public void enqueue(Callback callback) {
                if(success) {
                    callback.onResponse(null, Response.success(null));
                } else {
                    callback.onResponse(null, Response.error(520, null));
                }
            }

            @Override
            public boolean isExecuted() {
                return false;
            }

            @Override
            public void cancel() {

            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Override
            public Call<Void> clone() {
                return null;
            }

            @Override
            public Request request() {
                return null;
            }
        };

    }
}

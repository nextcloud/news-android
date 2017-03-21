package de.luhmer.owncloudnewsreader.reader;

import android.content.Context;

import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class OkHttpImageDownloader extends BaseImageDownloader {

    @SuppressWarnings("unused")
    private static final String TAG = "OkHttpImageDownloader";

    private OkHttpClient imageClient;

    public OkHttpImageDownloader(Context context, OkHttpClient imageClient) {
        super(context);
        this.imageClient = imageClient;
    }

    @Override
    public InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        HttpUrl httpUrl = HttpUrl.parse(imageUri);

        Request request = new Request.Builder()
                .url(httpUrl)
                .build();

        ResponseBody responseBody = imageClient.newCall(request).execute().body();
        InputStream inputStream = responseBody.byteStream();

        return new ContentLengthInputStream(inputStream, (int) responseBody.contentLength());
    }
}

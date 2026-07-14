/*
 * Android ownCloud News
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU AFFERO GENERAL PUBLIC LICENSE for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.luhmer.owncloudnewsreader.async_tasks;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;

import java.util.concurrent.TimeUnit;

import de.luhmer.owncloudnewsreader.model.ClientItemState;
import de.luhmer.owncloudnewsreader.repository.ClientItemStateRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Downloads the web page behind an article's link and extracts its readable full text using
 * Mozilla's Readability algorithm (via Readability4J).
 * <p>
 * The fetch is owned by this task, not by any fragment: on completion the result (and the cleared
 * "in flight" flag) is written straight into the {@link ClientItemState} for the item id, so the
 * outcome survives even if the fragment that started the fetch was recycled. A {@link Listener} is
 * notified afterwards purely so an still-attached fragment can re-render.
 */
public class FetchFullTextTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = FetchFullTextTask.class.getCanonicalName();

    // Use a desktop-ish user agent - some sites serve stripped-down or paywalled markup otherwise.
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";

    private static OkHttpClient sClient;

    public interface Listener {
        /** Full text was extracted and stored. {@code itemId} identifies which item was updated. */
        void onFullTextFetched(long itemId, String bodyHtml);
        /** Fetching or extracting the full text failed (network error, empty result, ...). */
        void onFullTextError(long itemId, @Nullable Exception exception);
    }

    private final String mUrl;
    private final long mItemId;
    private final ClientItemStateRepository mRepository;
    @Nullable private final Listener mListener;
    private Exception mException;

    public FetchFullTextTask(String url, long itemId, ClientItemStateRepository repository, @Nullable Listener listener) {
        this.mUrl = url;
        this.mItemId = itemId;
        this.mRepository = repository;
        this.mListener = listener;
    }

    private static synchronized OkHttpClient getClient() {
        if (sClient == null) {
            // Plain client - the article lives on the publisher's website, not on the Nextcloud server,
            // so we don't reuse the authenticated Nextcloud OkHttpClient here.
            sClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build();
        }
        return sClient;
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (mUrl == null || mUrl.trim().isEmpty()) {
            mException = new IllegalArgumentException("Article has no link to fetch full text from");
            return null;
        }

        try {
            Request request = new Request.Builder()
                    .url(mUrl)
                    .header("User-Agent", USER_AGENT)
                    .build();

            try (Response response = getClient().newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                if (!response.isSuccessful() || responseBody == null) {
                    mException = new Exception("Unexpected response " + response.code() + " for " + mUrl);
                    return null;
                }

                String html = responseBody.string();
                Readability4J readability4J = new Readability4J(mUrl, html);
                Article article = readability4J.parse();

                String content = article.getContentWithUtf8Encoding();
                if (content == null || content.trim().isEmpty()) {
                    content = article.getContent();
                }

                if (content == null || content.trim().isEmpty()) {
                    mException = new Exception("Could not extract readable content from " + mUrl);
                    return null;
                }

                return content;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to fetch full text for " + mUrl, ex);
            mException = ex;
            return null;
        }
    }

    @Override
    protected void onPostExecute(String bodyHtml) {
        super.onPostExecute(bodyHtml);

        // Update the shared per-item state regardless of whether the originating fragment is still
        // alive - this is what makes the result/flag independent of fragment lifecycle.
        ClientItemState state = mRepository.get(mItemId);
        state.setFetchingFullText(false);

        if (bodyHtml != null) {
            state.setFullTextHtml(bodyHtml);
            state.setShowingFullText(true);
            if (mListener != null) {
                mListener.onFullTextFetched(mItemId, bodyHtml);
            }
        } else {
            if (mListener != null) {
                mListener.onFullTextError(mItemId, mException);
            }
        }
    }
}

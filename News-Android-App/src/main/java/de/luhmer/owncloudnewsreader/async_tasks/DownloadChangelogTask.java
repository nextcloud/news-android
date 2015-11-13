package de.luhmer.owncloudnewsreader.async_tasks;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import de.luhmer.owncloudnewsreader.view.ChangeLogFileListView;

/**
 * Downloads the owncloud news reader changelog from github, transforms it into xml
 * and saves it as tempfile. This xml tempfile can be used for changeloglib library.
 */
public class DownloadChangelogTask extends AsyncTask<Void, Void, String> {

    @SuppressWarnings("unused")
    private static final String TAG = "DownloadChangelogTask";

    private static final String README_URL = "https://raw.github.com/owncloud/News-Android-App/master/README.md";
    private static final String FILE_NAME = "changelog.xml";

    private Context mContext;
    private ChangeLogFileListView mChangelogView;
    private Listener mListener;
    private IOException exception;

    /**
     * @param context
     * @param changelogView  this list view will be automatically filled when
     *                       downloading and saving has finished
     * @param listener       called when task has finished or errors have been raised
     */
    public DownloadChangelogTask(Context context,
                                 ChangeLogFileListView changelogView,
                                 Listener listener) {
        mContext = context;
        mChangelogView = changelogView;
        mListener = listener;
    }


    @Override
    protected String doInBackground(Void... params) {
        String path = null;

        try {
            ArrayList<String> changelogArr = downloadReadme();
            String xml = convertToXML(changelogArr);
            path = saveToTempFile(xml, FILE_NAME);
        } catch (IOException e) {
            exception = e;
        }

        return path;
    }

    @Override
    protected void onPostExecute(String filePath) {
        if (exception != null) {
            mListener.onError(exception);
            return;
        }

        mChangelogView.loadFile(filePath);
        mChangelogView.getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                mListener.onSuccess();
            }
        });
    }

    private ArrayList<String> downloadReadme() throws IOException {
        ArrayList<String> changelogArr = new ArrayList<>();

        URL url = new URL(README_URL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream isTemp = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(isTemp));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                changelogArr.add(inputLine.replace("<", "[").replace(">", "]"));
            }
            in.close();
        } finally {
            urlConnection.disconnect();
        }

        return changelogArr;
    }

    private String convertToXML(ArrayList<String> changelogArr) {
        // use changelog section exclusively
        int changelogStartIndex = changelogArr.indexOf("Updates") + 2;
        changelogArr.subList(0, changelogStartIndex).clear();
        changelogArr.add("");

        // create xml nodes
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        builder.append("<changelog bulletedList=\"true\">");

        boolean versionStarted = false;

        for (String line : changelogArr) {
            if (line.startsWith("- ")) {
                // change entry
                builder.append("<changelogtext>");
                builder.append(line.substring(2).trim());
                builder.append("</changelogtext>");
            } else if (line.equals("")) {
                // version end
                if (versionStarted) {
                    versionStarted = false;
                    builder.append("</changelogversion>");
                }
            } else if (!line.contains("---------------------")) {
                // version start
                versionStarted = true;
                builder.append("<changelogversion versionName=\"" + line + "\">");
            }
        }

        builder.append("</changelog>");

        return builder.toString();
    }

    private String saveToTempFile(String content, String fileName) throws IOException {
        File file = File.createTempFile(fileName, null, mContext.getCacheDir());
        BufferedWriter out = new BufferedWriter(new FileWriter(file));

        try {
            out.write(content);
        } finally {
            out.close();
        }

        return "file://" + file.getAbsolutePath();
    }


    public interface Listener {

        /**
         * Called when ChangeLogFileListView instance has successfully been updated.
         */
        void onSuccess();

        /**
         * Called when some error has been thrown during download, parsing or saving.
         */
        void onError(IOException e);
    }
}

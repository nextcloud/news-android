package de.luhmer.owncloudnewsreader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.NewsFileUtils;
import de.luhmer.owncloudnewsreader.helper.OpmlXmlParser;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.helper.URLConnectionReader;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewFeedActivity extends AppCompatActivity {

    private static final String TAG = NewFeedActivity.class.getCanonicalName();
    public final static String ADD_NEW_SUCCESS = "success";

    // UI references.
    protected @BindView(R.id.et_feed_url) EditText mFeedUrlView;
    protected @BindView(R.id.sp_folder) Spinner mFolderView;
    protected @BindView(R.id.new_feed_progress) View mProgressView;
    protected @BindView(R.id.new_feed_form) View mLoginFormView;
    protected @BindView(R.id.btn_addFeed) Button mAddFeedButton;
    protected @BindView(R.id.toolbar) Toolbar toolbar;

    private List<Folder> folders;
    protected @Inject ApiProvider mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((NewsReaderApplication) getApplication()).getAppComponent().injectActivity(this);

        ThemeChooser.getInstance(this).chooseTheme(this);
        super.onCreate(savedInstanceState);
        ThemeChooser.getInstance(this).afterOnCreate(this);
        setContentView(R.layout.activity_new_feed);

        ButterKnife.bind(this);


        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);


        folders = dbConn.getListOfFolders();
        folders.add(0, new Folder(0, "No folder"));

        String[] folderNames = new String[folders.size()];
        for(int i = 0; i < folders.size(); i++) {
            folderNames[i] = folders.get(i).getLabel();
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, folderNames);
        mFolderView.setAdapter(spinnerArrayAdapter);

        Intent intent = getIntent();
        String action = intent.getAction();

        if (action != null) {
            String url = "";
            if(action.equals(Intent.ACTION_VIEW)) {
                url = intent.getDataString();
            } else if(action.equals(Intent.ACTION_SEND)) {
                url = intent.getStringExtra(Intent.EXTRA_TEXT);
            }

            if(url.endsWith(".opml")) {
                AsyncTaskHelper.StartAsyncTask(new ImportOpmlSubscriptionsTask(url, NewFeedActivity.this));
            }

            //String scheme = intent.getScheme();
            //ContentResolver resolver = getContentResolver();

            //Uri uri = intent.getData();
            Log.v("tag" , "Content intent detected: " + action + " : " + url);
            mFeedUrlView.setText(url);
        }
    }

    @OnClick(R.id.btn_addFeed)
    public void btnAddFeedClick() {
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFeedUrlView.getWindowToken(), 0);


        attemptAddNewFeed();
    }

    @OnClick(R.id.btn_import_opml)
    public void importOpml() {
        Intent intentFilePicker = new Intent(this, FilePickerActivity.class);
        startActivityForResult(intentFilePicker, 1);
    }

    @OnClick(R.id.btn_export_opml)
    public void exportOpml() {
        String xml = OpmlXmlParser.GenerateOPML(this);

        String path = NewsFileUtils.getCacheDirPath(this) + "/../subscriptions.opml";

        try {
            FileOutputStream fos = new FileOutputStream(new File(path));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);
            outputStreamWriter.write(xml);
            outputStreamWriter.close();
            fos.close();

            new AlertDialog.Builder(this)
                    .setMessage("Successfully exported to: " + path)
                    .setTitle("OPML Export")
                    .setNeutralButton("Ok", null)
                    .create()
                    .show();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            AsyncTaskHelper.StartAsyncTask(new ImportOpmlSubscriptionsTask(filePath, NewFeedActivity.this));
        }
    }

    public class ImportOpmlSubscriptionsTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrlToFile;
        private HashMap<String, String> extractedUrls;
        private ProgressDialog pd;
        private Context mContext;

        ImportOpmlSubscriptionsTask(String urlToFile, Context context) {
            this.mUrlToFile = urlToFile;
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(mContext);
            pd.setTitle("Parsing OMPL...");
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String opmlContent;
            try {
                if(mUrlToFile.startsWith("http")) {//http[s]
                    opmlContent = URLConnectionReader.getText(mUrlToFile.toString());
                } else {
                    opmlContent = getStringFromFile(mUrlToFile);
                }

                InputStream is = new ByteArrayInputStream(opmlContent.getBytes());
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, null);
                parser.nextTag();
                extractedUrls = OpmlXmlParser.ReadFeed(parser);

                publishProgress();

                final HashMap<String, Long> existingFolders = new HashMap<>();

                mApi.getAPI().folders().blockingSubscribe(new Consumer<List<Folder>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull List<Folder> folders) throws Exception {
                        for(Folder folder : folders) {
                            existingFolders.put(folder.getLabel(), folder.getId());
                        }
                    }
                });

                for(String feedUrl : extractedUrls.keySet()) {
                    long folderId = 0; //id of the parent folder, 0 for root
                    String folderName = extractedUrls.get(feedUrl);
                    if(folderName != null) { //Get Folder ID (create folder if not exists)
                        if(existingFolders.containsKey(folderName)) { //Check if folder exists
                            folderId = existingFolders.get(folderName);
                        } else { //If not, create a new one on the server
                            //mApi.getAPI().createFolder(foldername) // HttpJsonRequest.getInstance().performCreateFolderRequest(api.getFolderUrl(), folderName);
                            final Map<String, Object> folderMap = new HashMap<>(2);
                            folderMap.put("name", folderName);
                            Folder folder = mApi.getAPI().createFolder(folderMap).execute().body().get(0);
                            //TODO test this!!!
                            existingFolders.put(folder.getLabel(), folder.getId()); //Add folder to list of existing folder in order to prevent that the method tries to create it multiple times
                        }
                    }

                    final Map<String, Object> feedMap = new HashMap<>(2);
                    feedMap.put("url", feedUrl);
                    feedMap.put("folderId", folderId);
                    
                    Feed feed = mApi.getAPI().createFeed(feedMap).execute().body().get(0);
                    Log.v(TAG, "New Feed-ID: " + feed.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            String text = "Extracted the following feeds:\n";
            for (String url : extractedUrls.keySet()) {
                text += "\n" + url;
            }
            pd.setMessage(text);

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pd != null) {
                pd.dismiss();
            }

            if(!result) {
                Toast.makeText(mContext, "Failed to parse OPML file", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Successfully imported OPML!", Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(result);
        }
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptAddNewFeed() {
        Folder folder = folders.get(mFolderView.getSelectedItemPosition());

        // Reset errors.
        mFeedUrlView.setError(null);

        // Store values at the time of the login attempt.
        String urlToFeed = mFeedUrlView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(urlToFeed)) {
            mFeedUrlView.setError(getString(R.string.error_field_required));
            focusView = mFeedUrlView;
            cancel = true;
        } else if (!isUrlValid(urlToFeed)) {
            mFeedUrlView.setError(getString(R.string.error_invalid_url));
            focusView = mFeedUrlView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            final Map<String, Object> feedMap = new HashMap<>(2);
            feedMap.put("url", urlToFeed);
            feedMap.put("folderId", folder.getId());
            mApi.getAPI().createFeed(feedMap).enqueue(new Callback<List<Feed>>() {
                @Override
                public void onResponse(Call<List<Feed>> call, Response<List<Feed>> response) {
                    showProgress(false);

                    if (response.isSuccessful()) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("success", true);
                        setResult(RESULT_OK,returnIntent);

                        finish();
                    } else {
                        mFeedUrlView.setError(getString(R.string.login_dialog_text_something_went_wrong));
                        mFeedUrlView.requestFocus();
                    }
                }

                @Override
                public void onFailure(Call<List<Feed>> call, Throwable t) {
                    showProgress(false);

                    mFeedUrlView.setError(getString(R.string.login_dialog_text_something_went_wrong) + " - " + OkHttpSSLClient.HandleExceptions((Exception) t).getMessage());
                    mFeedUrlView.requestFocus();
                }
            });
        }
    }
    private boolean isUrlValid(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            default:
                    Log.v(TAG, "Unknown option selected..");
        }
        return super.onOptionsItemSelected(item);
    }




    @NonNull public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }
}




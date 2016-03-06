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

import org.json.JSONArray;
import org.json.JSONObject;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.FileUtils;
import de.luhmer.owncloudnewsreader.helper.OpmlXmlParser;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.helper.URLConnectionReader;
import de.luhmer.owncloudnewsreader.model.Tuple;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;

public class NewFeedActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AddNewFeedTask mAddFeedTask = null;

    // UI references.
    @Bind(R.id.et_feed_url) EditText mFeedUrlView;
    @Bind(R.id.sp_folder) Spinner mFolderView;
    @Bind(R.id.new_feed_progress) View mProgressView;
    @Bind(R.id.new_feed_form) View mLoginFormView;
    @Bind(R.id.btn_addFeed) Button mAddFeedButton;
    @Bind(R.id.toolbar) Toolbar toolbar;

    List<Folder> folders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.chooseTheme(this);
        super.onCreate(savedInstanceState);
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

        String path = FileUtils.getPath(this) + "/../subscriptions.opml";

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

    public static class ImportOpmlSubscriptionsTask extends AsyncTask<Void, Void, Boolean> {

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

                API api = new APIv2(HttpJsonRequest.getInstance().getRootUrl());

                HashMap<String, Long> existingFolders = new HashMap<>();
                InputStream isFolder = HttpJsonRequest.getInstance().PerformJsonRequest(api.getFolderUrl());
                String folderJSON = convertStreamToString(isFolder);
                JSONArray jArrFolder = new JSONObject(folderJSON).getJSONArray("folders");
                for(int i = 0; i < jArrFolder.length(); i++) {
                    JSONObject folder = ((JSONObject) jArrFolder.get(i));
                    long folderId = folder.getLong("id");
                    String folderName = folder.getString("name");

                    existingFolders.put(folderName, folderId);
                }


                for(String feedUrl : extractedUrls.keySet()) {
                    long folderId = 0; //id of the parent folder, 0 for root
                    String folderName = extractedUrls.get(feedUrl);
                    if(folderName != null) { //Get Folder ID (create folder if not exists)
                        if(existingFolders.containsKey(folderName)) { //Check if folder exists
                            folderId = existingFolders.get(folderName);
                        } else { //If not, create a new one on the server
                            Tuple<Integer, String> status = HttpJsonRequest.getInstance().performCreateFolderRequest(api.getFolderUrl(), folderName);
                            if (status.key == 200 || status.key == 409) { //200 = Ok, 409 = If the folder exists already
                                JSONObject jObj = new JSONObject(status.value).getJSONArray("folders").getJSONObject(0);
                                folderId = jObj.getLong("id");
                                existingFolders.put(folderName, folderId); //Add folder to list of existing folder in order to prevent that the method tries to create it multiple times
                            } else {
                                throw new Exception("Failed to create folder on server!");
                            }
                        }
                    }


                    int status = HttpJsonRequest.getInstance().performCreateFeedRequest(api.getFeedUrl(), feedUrl, folderId);
                    if(status == 200 || status == 409) {

                    } else {
                        throw new Exception("Failed to create feed on server!");
                    }
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
        if (mAddFeedTask != null) {
            return;
        }

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
            mAddFeedTask = new AddNewFeedTask(urlToFeed, folder.getId());//TODO needs testing!
            mAddFeedTask.execute((Void) null);
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

    public final static String ADD_NEW_SUCCESS = "success";



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AddNewFeedTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrlToFeed;
        private final long mFolderId;


        AddNewFeedTask(String urlToFeed, long folderId) {
            this.mUrlToFeed = urlToFeed;
            this.mFolderId = folderId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            API api = new APIv2(HttpJsonRequest.getInstance().getRootUrl());
            try {
                int status = HttpJsonRequest.getInstance().performCreateFeedRequest(api.getFeedUrl(), mUrlToFeed, mFolderId);
                if(status == 200) {
                    return true;
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddFeedTask = null;
            showProgress(false);

            if (success) {
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
        protected void onCancelled() {
            mAddFeedTask = null;
            showProgress(false);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(mAddFeedTask != null)
                    mAddFeedTask.cancel(true);

                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
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




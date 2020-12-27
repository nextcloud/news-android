package de.luhmer.owncloudnewsreader;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.json.JSONException;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.databinding.ActivityNewFeedBinding;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.OpmlXmlParser;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.helper.URLConnectionReader;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewFeedActivity extends AppCompatActivity {

    private static final String TAG = NewFeedActivity.class.getCanonicalName();
    public final static String ADD_NEW_SUCCESS = "success";
    private static final int PERMISSIONS_REQUEST_READ_CODE = 0;
    private static final int PERMISSIONS_REQUEST_WRITE_CODE = 1;
    private final static int REQUEST_CODE_OPML_IMPORT = 2;

    // UI references.
    protected ActivityNewFeedBinding binding;

    private List<Folder> folders;
    protected @Inject ApiProvider mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((NewsReaderApplication) getApplication()).getAppComponent().injectActivity(this);

        ThemeChooser.chooseTheme(this);
        super.onCreate(savedInstanceState);
        ThemeChooser.afterOnCreate(this);

        binding = ActivityNewFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnAddFeed.setOnClickListener((v) -> btnAddFeedClick());
        binding.btnImportOpml.setOnClickListener((v) -> importOpml());
        binding.btnExportOpml.setOnClickListener((v) -> exportOpml());

        if (binding.toolbarLayout.toolbar != null) {
            setSupportActionBar(binding.toolbarLayout.toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);


        folders = dbConn.getListOfFolders();
        folders.add(0, new Folder(0, getString(R.string.move_feed_root_folder)));

        String[] folderNames = new String[folders.size()];
        for(int i = 0; i < folders.size(); i++) {
            folderNames[i] = folders.get(i).getLabel();
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, folderNames);
        binding.spFolder.setAdapter(spinnerArrayAdapter);

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
            binding.etFeedUrl.setText(url);
        }
    }

    public void btnAddFeedClick() {
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.etFeedUrl.getWindowToken(), 0);


        attemptAddNewFeed();
    }

    public void importOpml() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, "Allow external storage reading", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_READ_CODE);
            }
        } else {
            openFilePicker();
        }

    }

    private void openFilePicker() {
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(REQUEST_CODE_OPML_IMPORT)
                //.withFilter(Pattern.compile(".*\\.opml$")) // Filtering files and directories by file name using regexp
                .withFilterDirectories(true) // Set directories filterable (false by default)
                .withHiddenFiles(true) // Show hidden files and folders
                .start();
    }

    public void exportOpml() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, "Allow external storage writing", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_WRITE_CODE);
            }
        } else {
            exportOpmlFile();
        }
    }

    private void exportOpmlFile() {
        String xml = OpmlXmlParser.GenerateOPML(this);

        //String path = NewsFileUtils.getCacheDirPath(this) + "/subscriptions.opml";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String filename = "subscriptions-" + format.format(new Date()) + ".opml";
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

        try {
            FileOutputStream fos = new FileOutputStream(path);
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

        if (requestCode == REQUEST_CODE_OPML_IMPORT && resultCode == RESULT_OK) {
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
                    opmlContent = URLConnectionReader.getText(mUrlToFile);
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

                mApi.getNewsAPI().folders().blockingSubscribe(folders -> {
                    for(Folder folder : folders) {
                        existingFolders.put(folder.getLabel(), folder.getId());
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
                            Folder folder = mApi.getNewsAPI().createFolder(folderMap).execute().body().get(0);
                            //TODO test this!!!
                            existingFolders.put(folder.getLabel(), folder.getId()); //Add folder to list of existing folder in order to prevent that the method tries to create it multiple times
                        }
                    }

                    Feed feed = mApi.getNewsAPI().createFeed(feedUrl, folderId).execute().body().get(0);
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
        Folder folder = folders.get(binding.spFolder.getSelectedItemPosition());

        // Reset errors.
        binding.etFeedUrl.setError(null);

        // Store values at the time of the login attempt.
        String urlToFeed = binding.etFeedUrl.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(urlToFeed)) {
            binding.etFeedUrl.setError(getString(R.string.error_field_required));
            focusView = binding.etFeedUrl;
            cancel = true;
        } else if (!isUrlValid(urlToFeed)) {
            binding.etFeedUrl.setError(getString(R.string.error_invalid_url));
            focusView = binding.etFeedUrl;
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

            mApi.getNewsAPI().createFeed(urlToFeed, folder.getId()).enqueue(new Callback<List<Feed>>() {
                @Override
                public void onResponse(Call<List<Feed>> call, final Response<List<Feed>> response) {
                    runOnUiThread(() -> {
                        showProgress(false);

                        if (response.isSuccessful()) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(ADD_NEW_SUCCESS, true);
                            setResult(RESULT_OK, returnIntent);

                            finish();
                        } else {
                            try {
                                String errorMessage = response.errorBody().string();
                                try {
                                    //Log.e(TAG, errorMessage);
                                    JSONObject jObjError= new JSONObject(errorMessage);
                                    errorMessage = jObjError.getString("message");
                                    errorMessage = truncate(errorMessage, 150);
                                } catch (JSONException e) {
                                    Log.e(TAG, "Extracting error message failed: " + errorMessage, e);
                                }
                                binding.etFeedUrl.setError(errorMessage);
                                Log.e(TAG, errorMessage);
                            } catch (IOException e) {
                                Log.e(TAG, "IOException", e);
                                binding.etFeedUrl.setError(getString(R.string.login_dialog_text_something_went_wrong));
                            }
                            binding.etFeedUrl.requestFocus();
                        }
                    });
                }

                @Override
                public void onFailure(Call<List<Feed>> call, final Throwable t) {
                    runOnUiThread(() -> {
                        showProgress(false);

                        binding.etFeedUrl.setError(getString(R.string.login_dialog_text_something_went_wrong) + " - " + OkHttpSSLClient.HandleExceptions((Exception) t).getMessage());
                        binding.etFeedUrl.requestFocus();
                    });
                }
            });
        }
    }

    public static String truncate(String str, int len) {
        if (str.length() > len) {
            return str.substring(0, len) + "...";
        } else {
            return str;
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

            binding.newFeedForm.setVisibility(show ? View.GONE : View.VISIBLE);
            binding.newFeedForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    binding.newFeedForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            binding.newFeedProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.newFeedProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    binding.newFeedProgress.setVisibility(show ? View.VISIBLE : View.GONE);
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




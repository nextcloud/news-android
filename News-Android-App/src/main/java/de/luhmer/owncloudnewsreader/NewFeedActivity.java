package de.luhmer.owncloudnewsreader;

import static java.util.Objects.requireNonNull;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import java.util.ArrayList;
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

        setSupportActionBar(binding.toolbarLayout.toolbar);
        requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);


        folders = dbConn.getListOfFolders();
        Folder rootFolder = new Folder(0, getString(R.string.move_feed_root_folder));

        if (folders.isEmpty()) {
            // list is of type EmptyList and is not modifiable - therefore create a new modifiable list
            folders = new ArrayList<>();
        }

        folders.add(0, rootFolder);

        String[] folderNames = folders.stream().map(Folder::getLabel).toArray(String[]::new);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, folderNames);
        binding.spFolder.setAdapter(spinnerArrayAdapter);

        Intent intent = getIntent();
        String action = intent.getAction();

        if (action != null) {
            String url = "";
            if (action.equals(Intent.ACTION_VIEW)) {
                url = intent.getDataString();
            } else if(action.equals(Intent.ACTION_SEND)) {
                url = intent.getStringExtra(Intent.EXTRA_TEXT);
            }

            if(url != null && url.endsWith(".opml")) {
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
        startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"), REQUEST_CODE_OPML_IMPORT);
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
                    .setMessage(getString(R.string.successfully_exported) + " " + path)
                    .setTitle(getString(R.string.opml_export))
                    .setNeutralButton(getString(android.R.string.ok), null)
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
            final Uri importUri = data.getData();

            switch (importUri.getScheme()) {
                case ContentResolver.SCHEME_CONTENT:
                case ContentResolver.SCHEME_FILE:
                    new Thread(() -> {
                        final File cacheFile = new File(getCacheDir().getAbsolutePath() + "/import.opml");
                        byte[] buffer = new byte[4096];
                        try (
                                final InputStream inputStream = getContentResolver().openInputStream(importUri);
                                final FileOutputStream outputStream = new FileOutputStream(cacheFile)
                        ) {
                            int count;
                            while ((count = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, count);
                            }
                            runOnUiThread(() -> AsyncTaskHelper.StartAsyncTask(new ImportOpmlSubscriptionsTask(cacheFile.getAbsolutePath(), NewFeedActivity.this)));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }).start();
                    break;
                default:
                    Toast.makeText(this, "Unknown URI scheme: " + importUri.getScheme(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public class ImportOpmlSubscriptionsTask extends AsyncTask<Void, List<String>, Boolean> {

        private final String mUrlToFile;
        private HashMap<String, String> extractedUrls;
        private NewsReaderOPMLImportDialogFragment pd;
        private final Context mContext;

        ImportOpmlSubscriptionsTask(String urlToFile, Context context) {
            this.mUrlToFile = urlToFile;
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("news_reader_opml_import_dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            pd = NewsReaderOPMLImportDialogFragment.newInstance(false);
            pd.show(ft, "news_reader_opml_import_dialog");

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // wait for NewsReaderOPMLImportDialogFragment to be visible
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String opmlContent;
            try {
                if (mUrlToFile.startsWith("http")) {//http[s]
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

                List<String> result = new ArrayList<>();
                publishProgress(new ArrayList<>(result));

                final HashMap<String, Long> existingFolders = new HashMap<>();

                mApi.getNewsAPI().folders().blockingSubscribe(folders -> {
                    for (Folder folder : folders) {
                        existingFolders.put(folder.getLabel(), folder.getId());
                    }
                });

                for (String feedUrl : extractedUrls.keySet()) {
                    long folderId = 0; //id of the parent folder, 0 for root
                    String folderName = extractedUrls.get(feedUrl);
                    if(folderName != null) { //Get Folder ID (create folder if not exists)
                        if (!existingFolders.containsKey(folderName)) {
                            // If folder does not exist, create a new one on the server
                            final Map<String, Object> folderMap = new HashMap<>(1);
                            folderMap.put("name", folderName);
                            Folder folder = mApi.getNewsAPI().createFolder(folderMap).execute().body().get(0);
                            folderId = folder.getId();
                            // Add folder to list of existing folder in order to prevent that the method tries to create it multiple times
                            existingFolders.put(folder.getLabel(), folderId);
                        }

                        folderId = existingFolders.get(folderName);
                    }

                    Response<List<Feed>> response = mApi.getNewsAPI().createFeed(feedUrl, folderId).execute();
                    if (response.isSuccessful()) {
                        Feed feed = response.body().get(0);
                        result.add("✓ " + feed.getLink());
                        Log.d(TAG, "Successfully imported feed: " + feedUrl + " - Feed-ID: " + feed.getId());
                    } else if (response.code() == 409) {
                        // already exists
                        result.add("⤏ " + feedUrl);
                    } else {
                        result.add("✗ " + response.code() + " - " + feedUrl);
                        Log.e(TAG, "Failed to import feed: " + feedUrl + " - Status-Code: " + response.code());
                        Log.e(TAG, response.errorBody().string());
                    }

                    // make list immutable and report it as progress
                    publishProgress(new ArrayList<>(result));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(List<String>... values) {
            // StringBuilder text = new StringBuilder("This might take a few minutes.. please wait..\n");
            StringBuilder text = new StringBuilder();

            List<String> log = values[0];
            for (String line : log) {
                text.append("\n").append(line);
            }

            pd.updateProgress(log.size(), extractedUrls.size());
            pd.setMessage(text.toString().trim());

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            pd.setVisibilityOkButton(true);

            if(!result) {
                Toast.makeText(mContext, "Failed to parse OPML file", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Import done!", Toast.LENGTH_LONG).show();
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
                public void onResponse(@NonNull Call<List<Feed>> call, @NonNull final Response<List<Feed>> response) {
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
                public void onFailure(@NonNull Call<List<Feed>> call, @NonNull final Throwable t) {
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
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {//NavUtils.navigateUpFromSameTask(this);
            finish();
            return true;
        } else {
            Log.v(TAG, "Unknown option selected..");
        }
        return super.onOptionsItemSelected(item);
    }




    @NonNull public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
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




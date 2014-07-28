package de.luhmer.owncloudnewsreader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import java.net.URL;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;

public class NewFeedActivity extends ActionBarActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AddNewFeedTask mAddFeedTask = null;

    // UI references.
    @InjectView(R.id.et_feed_url) EditText mFeedUrlView;
    @InjectView(R.id.sp_folder) Spinner mFolderView;
    @InjectView(R.id.new_feed_progress) View mProgressView;
    @InjectView(R.id.new_feed_form) View mLoginFormView;
    @InjectView(R.id.btn_addFeed) Button mAddFeedButton;

    List<Folder> folders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.chooseTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_feed);

        ButterKnife.inject(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);


        folders = dbConn.getListOfFolders();
        folders.add(0, new Folder(0, "No folder"));

        String[] folderNames = new String[folders.size()];
        for(int i = 0; i < folders.size(); i++) {
            folderNames[i] = folders.get(i).getLabel();
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, folderNames);
        mFolderView.setAdapter(spinnerArrayAdapter);

        mAddFeedButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                //Hide keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mFeedUrlView.getWindowToken(), 0);


                attemptAddNewFeed();
            }
        });
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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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

            API api = new APIv2(NewFeedActivity.this);

            try {
                int status = HttpJsonRequest.performCreateFeedRequest(api.getFeedUrl(),
                                api.getUsername(),
                                api.getPassword(),
                                NewFeedActivity.this,
                                mUrlToFeed, mFolderId);

                if(status == 200) {
                    return true;
                }

                Log.d("NewFeedActivity", "Status: " + status);
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
}




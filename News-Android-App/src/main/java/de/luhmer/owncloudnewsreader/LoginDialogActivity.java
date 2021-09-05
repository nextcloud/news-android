/*
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
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
*
*/

package de.luhmer.owncloudnewsreader;

import static java.util.Objects.requireNonNull;
import static de.luhmer.owncloudnewsreader.Constants.MIN_NEXTCLOUD_FILES_APP_VERSION_CODE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.helper.VersionCheckHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.databinding.ActivityLoginDialogBinding;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.model.NextcloudNewsVersion;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginDialogActivity extends AppCompatActivity {

    private final String TAG = LoginDialogActivity.class.getCanonicalName();

    public static final int RESULT_LOGIN = 16000;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	protected @Inject ApiProvider mApi;
    protected @Inject SharedPreferences mPrefs;
    protected @Inject MemorizingTrustManager mMemorizingTrustManager;
	//private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mUsername;
    private String mPassword;
    private String mOc_root_path;

    // UI references.
    protected ActivityLoginDialogBinding binding;

    private SingleSignOnAccount importedAccount = null;
    private boolean mPasswordVisible = false;


	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		((NewsReaderApplication) getApplication()).getAppComponent().injectActivity(this);
		
		binding = ActivityLoginDialogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSingleSignOn.setOnClickListener((v) -> startSingleSignOn());
        binding.btnLogin.setOnClickListener((v) -> startManualLogin());
        binding.tvManualLogin.setOnClickListener((v) -> manualLogin());

        // Manual Login
        binding.imgViewShowPassword.setOnClickListener(ImgViewShowPasswordListener);
        binding.password.addTextChangedListener(PasswordTextChangedListener);

        mUsername = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, "");
        mPassword = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, "");
        mOc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
        boolean mCbDisableHostnameVerification = mPrefs.getBoolean(SettingsActivity.CB_DISABLE_HOSTNAME_VERIFICATION_STRING, false);

        if(!mPassword.isEmpty()) {
            binding.imgViewShowPassword.setVisibility(View.GONE);
        }

        // Set up the login form.
        binding.username.setText(mUsername);
        binding.password.setText(mPassword);
        binding.edtOwncloudRootPath.setText(mOc_root_path);

        binding.cbAllowAllSSLCertificates.setChecked(mCbDisableHostnameVerification);
        binding.cbAllowAllSSLCertificates.setOnCheckedChangeListener((buttonView, isChecked) -> mPrefs.edit()
                .putBoolean(SettingsActivity.CB_DISABLE_HOSTNAME_VERIFICATION_STRING, isChecked)
                .commit());
	}

    @Override
    public void onBackPressed() {
	    if (mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) == null) {
	        // exit application if no account is set uo
            finishAffinity();
        } else {
            // go back to previous activity
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMemorizingTrustManager.bindDisplayActivity(this);
    }

    @Override
    protected void onStop() {
        mMemorizingTrustManager.unbindDisplayActivity(this);
        super.onStop();
    }

    public void startSingleSignOn() {
	    if (!VersionCheckHelper.verifyMinVersion(LoginDialogActivity.this, MIN_NEXTCLOUD_FILES_APP_VERSION_CODE)) {
            // Dialog will be shown automatically
            return;
        }

        binding.oldLoginWrapper.setVisibility(View.GONE);

        try {
            AccountImporter.pickNewAccount(LoginDialogActivity.this);
        } catch (NextcloudFilesAppNotInstalledException e) {
            UiExceptionManager.showDialogForException(LoginDialogActivity.this, e);
        } catch (AndroidGetAccountsPermissionNotGranted e) {
            AccountImporter.requestAndroidAccountPermissionsAndPickAccount(this);
        }
    }

    public void startManualLogin() {
        attemptLogin();
    }

    public void manualLogin() {
        binding.oldLoginWrapper.setVisibility(View.VISIBLE);
    }

	private final TextWatcher PasswordTextChangedListener = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
            if(s.toString().isEmpty()) {
                binding.imgViewShowPassword.setVisibility(View.VISIBLE);
            }
		}
	};

	private final View.OnClickListener ImgViewShowPasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int lastSelection = binding.password.getSelectionEnd();
            mPasswordVisible = !mPasswordVisible;

            if(mPasswordVisible) {
                binding.password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                binding.password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

            binding.password.setSelection(lastSelection);
        }
    };

    private ProgressDialog buildPendingDialogWhileLoggingIn() {
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setTitle(getString(R.string.login_progress_signing_in));
        return pDialog;
    }

    private void loginSingleSignOn() {
        final ProgressDialog dialogLogin = buildPendingDialogWhileLoggingIn();
        dialogLogin.show();

        Editor editor = mPrefs.edit();
        editor.putString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, importedAccount.url);
        editor.putString(SettingsActivity.EDT_PASSWORD_STRING, importedAccount.token);
        editor.putString(SettingsActivity.EDT_USERNAME_STRING, importedAccount.name);
        editor.putBoolean(SettingsActivity.SW_USE_SINGLE_SIGN_ON, true);
        editor.commit();

        resetDatabase();

        SingleAccountHelper.setCurrentAccount(this, importedAccount.name);

        mApi.initApi(new NextcloudAPI.ApiConnectedListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "onConnected() called");
                finishLogin(dialogLogin);
            }

            @Override
            public void onError(Exception ex) {
                dialogLogin.dismiss();
                Log.d(TAG, "onError() called with: ex = [" + ex + "]");
                ShowAlertDialog(getString(R.string.login_dialog_title_error), ex.getMessage(), LoginDialogActivity.this);
            }
        });
    }

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	@SuppressLint({"SetTextI18n"})
    public void attemptLogin() {
		// Reset errors.
		binding.username.setError(null);
		binding.password.setError(null);
		binding.edtOwncloudRootPath.setError(null);

		// Append "https://" is url doesn't contain it already
        mOc_root_path = requireNonNull(binding.edtOwncloudRootPath.getText()).toString().trim();
        if(!mOc_root_path.startsWith("http")) {
            binding.edtOwncloudRootPath.setText("https://" + mOc_root_path);
        }

		// Store values at the time of the login attempt.
		mUsername = requireNonNull(binding.username.getText()).toString().trim();
		mPassword = requireNonNull(binding.password.getText()).toString();
		mOc_root_path = binding.edtOwncloudRootPath.getText().toString().trim();

		boolean cancel = false;
		View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            binding.password.setError(getString(R.string.error_field_required));
            focusView = binding.password;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(mUsername)) {
            binding.username.setError(getString(R.string.error_field_required));
            focusView = binding.username;
            cancel = true;
        }

        if (TextUtils.isEmpty(mOc_root_path)) {
            binding.edtOwncloudRootPath.setError(getString(R.string.error_field_required));
            focusView = binding.edtOwncloudRootPath;
            cancel = true;
        } else {
            try {
                URL url = new URL(mOc_root_path);
                if(!Patterns.WEB_URL.matcher(mOc_root_path).matches()) {
                    throw new MalformedURLException();
                }
                if (!url.getProtocol().equals("https")) {
                    ShowAlertDialog(getString(R.string.login_dialog_title_security_warning),
                            getString(R.string.login_dialog_text_security_warning), this);
                }
            } catch (MalformedURLException e) {
                binding.edtOwncloudRootPath.setError(getString(R.string.error_invalid_url));
                focusView = binding.edtOwncloudRootPath;
                cancel = true;
            }
        }

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
            Editor editor = mPrefs.edit();
            editor.putString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, mOc_root_path);
            editor.putString(SettingsActivity.EDT_PASSWORD_STRING, mPassword);
            editor.putString(SettingsActivity.EDT_USERNAME_STRING, mUsername);
            editor.putBoolean(SettingsActivity.SW_USE_SINGLE_SIGN_ON, false);
            editor.commit();

            resetDatabase();

            final ProgressDialog dialogLogin = buildPendingDialogWhileLoggingIn();
            dialogLogin.show();

            mApi.initApi(new NextcloudAPI.ApiConnectedListener() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "onConnected() called");
                    finishLogin(dialogLogin);
                }

                @Override
                public void onError(Exception ex) {
                    dialogLogin.dismiss();
                    Log.d(TAG, "onError() called with: ex = [" + ex + "]");
                    ShowAlertDialog(getString(R.string.login_dialog_title_error), ex.getMessage(), LoginDialogActivity.this);
                }
            });
		}
	}

	private void resetDatabase() {
        //Reset Database
        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(LoginDialogActivity.this);
        dbConn.resetDatabase();
    }

    private void finishLogin(final ProgressDialog dialogLogin) {
        mApi.getNewsAPI().version()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NextcloudNewsVersion>() {
                    boolean loginSuccessful = false;

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.v(TAG, "onSubscribe() called with: d = [" + d + "]");
                    }

                    @Override
                    public void onNext(@NonNull NextcloudNewsVersion version) {
                        Log.v(TAG, "onNext() called with: status = [" + version.version + "]");

                        loginSuccessful = true;
                        mPrefs.edit().putString(Constants.NEWS_WEB_VERSION_NUMBER_STRING, version.version).apply();

                        if(version.version.equals("0")) {
                            ShowAlertDialog(getString(R.string.login_dialog_title_error), getString(R.string.login_dialog_text_zero_version_code), LoginDialogActivity.this);
                            loginSuccessful = false;
                        }

                        importedAccount = null;
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        dialogLogin.dismiss();
                        Log.v(TAG, "onError() called with: e = [" + e + "]");

                        Throwable t = OkHttpSSLClient.HandleExceptions(e);

                        if(t instanceof NextcloudHttpRequestFailedException && ((NextcloudHttpRequestFailedException) t).getStatusCode() == 302) {
                            ShowAlertDialog(
                                    getString(R.string.login_dialog_title_error),
                                    getString(R.string.login_dialog_text_news_app_not_installed_on_server,
                                            "https://github.com/nextcloud/news/blob/master/docs/install.md#installing-from-the-app-store"),
                                    LoginDialogActivity.this);
                        } else {
                            ShowAlertDialog(getString(R.string.login_dialog_title_error), t.getMessage(), LoginDialogActivity.this);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dialogLogin.dismiss();

                        Log.v(TAG, "onComplete() called");

                        if(loginSuccessful) {
                            Intent returnIntent = new Intent();
                            setResult(RESULT_OK, returnIntent);

                            finish();
                        }
                    }
                });
    }

    public static void ShowAlertDialog(String title, String text, Activity activity) {
        // Linkify the message
        final SpannableString s = new SpannableString(text != null ? text : activity.getString(R.string.login_dialog_select_account_unknown_error_toast));
        Linkify.addLinks(s, Linkify.ALL);

        AlertDialog aDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(s)
                .setPositiveButton(activity.getString(android.R.string.ok), null)
                .create();
        aDialog.show();

        // Make the textview clickable. Must be called after show()
        ((TextView) aDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            AccountImporter.onActivityResult(requestCode, resultCode, data, LoginDialogActivity.this, account -> {
                LoginDialogActivity.this.importedAccount = account;
                loginSingleSignOn();
            });
        } catch (AccountImportCancelledException ignored) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}

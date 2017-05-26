/**
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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.luhmer.owncloud.accountimporter.ImportAccountsDialogFragment;
import de.luhmer.owncloud.accountimporter.helper.AccountImporter;
import de.luhmer.owncloud.accountimporter.helper.OwnCloudAccount;
import de.luhmer.owncloud.accountimporter.interfaces.IAccountImport;
import de.luhmer.owncloudnewsreader.authentication.AuthenticatorActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.model.NextcloudNewsVersion;
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
public class LoginDialogFragment extends DialogFragment implements IAccountImport {

    static LoginDialogFragment instance;
    public static LoginDialogFragment getInstance() {
        if(instance == null)
            instance = new LoginDialogFragment();
        return instance;
    }

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	@Inject ApiProvider mApi;
    @Inject SharedPreferences mPrefs;
	//private UserLoginTask mAuthTask = null;

	private Activity mActivity;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;
	private String mOc_root_path;
	private boolean mCbDisableHostnameVerification;
	private boolean showImportAccountButton;

	// UI references.
	@Bind(R.id.username) EditText mUsernameView;
	@Bind(R.id.password) EditText mPasswordView;
	@Bind(R.id.edt_owncloudRootPath) EditText mOc_root_path_View;
	@Bind(R.id.cb_AllowAllSSLCertificates) CheckBox mCbDisableHostnameVerificationView;
    @Bind(R.id.imgView_ShowPassword) ImageView mImageViewShowPwd;

    boolean mPasswordVisible = false;

    @Override
    public void accountAccessGranted(OwnCloudAccount account) {
        mUsernameView.setText(account.getUsername());
        mPasswordView.setText(account.getPassword());
        mOc_root_path_View.setText(account.getUrl());
    }

    public interface LoginSuccessfullListener {
		void LoginSucceeded();
	}
	LoginSuccessfullListener listener;


	public LoginDialogFragment() {

	}

	public void setActivity(Activity mActivity) {
		this.mActivity = mActivity;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(LoginSuccessfullListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		((NewsReaderApplication) getActivity().getApplication()).getAppComponent().injectFragment(this);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		showImportAccountButton = AccountImporter.findAccounts(getActivity()).size() > 0;

		//setRetainInstance(true);

        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_signin, null);
        ButterKnife.bind(this, view);

        builder.setView(view);
		builder.setTitle(getString(R.string.action_sign_in_short));

		builder.setPositiveButton(getString(R.string.action_sign_in_short), null);

		if(showImportAccountButton) {
			builder.setNeutralButton(getString(R.string.import_account), null);
		}

        mImageViewShowPwd.setOnClickListener(ImgViewShowPasswordListener);
		mPasswordView.addTextChangedListener(PasswordTextChangedListener);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUsername = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, "");
        mPassword = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, "");
        mOc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
        mCbDisableHostnameVerification = mPrefs.getBoolean(SettingsActivity.CB_DISABLE_HOSTNAME_VERIFICATION_STRING, false);

		if(!mPassword.isEmpty()) {
            mImageViewShowPwd.setVisibility(View.GONE);
        }

    	// Set up the login form.
 		mUsernameView.setText(mUsername);
 		mPasswordView.setText(mPassword);
 		mOc_root_path_View.setText(mOc_root_path);

 		mCbDisableHostnameVerificationView.setChecked(mCbDisableHostnameVerification);
 		mCbDisableHostnameVerificationView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				mPrefs.edit()
					.putBoolean(SettingsActivity.CB_DISABLE_HOSTNAME_VERIFICATION_STRING, isChecked)
					.commit();
			}
		});

		AlertDialog dialog = builder.create();
		// Set dialog to resize when soft keyboard pops up
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return dialog;
    }

	@Override
	public void onStart() {
		super.onStart();
		final AlertDialog dialog = (AlertDialog) getDialog();
		// Override the onClickListeners, as the default implementation would dismiss the dialog
		if (dialog != null) {
			if (showImportAccountButton) {
				Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
				neutralButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ImportAccountsDialogFragment.show(getActivity(), LoginDialogFragment.this);
					}
				});
				// Limit button width to not push positive button out of view
				neutralButton.setMaxEms(10);
			}
			Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					attemptLogin();
				}
			});
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if(mActivity instanceof AuthenticatorActivity)
			mActivity.finish();
	}

	private TextWatcher PasswordTextChangedListener = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
            if(s.toString().isEmpty()) {
                mImageViewShowPwd.setVisibility(View.VISIBLE);
            }
		}
	};

	private View.OnClickListener ImgViewShowPasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPasswordVisible = !mPasswordVisible;

            if(mPasswordVisible) {
                mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }
    };

	private ProgressDialog BuildPendingDialogWhileLoggingIn()
	{
		ProgressDialog pDialog = new ProgressDialog(getActivity());
        pDialog.setTitle(getString(R.string.login_progress_signing_in));
        return pDialog;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		//if (mAuthTask != null) {
		//	return;
		//}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);
		mOc_root_path_View.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString().trim();
		mPassword = mPasswordView.getText().toString();
		mOc_root_path = mOc_root_path_View.getText().toString().trim();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}
		// Check for a valid email address.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (TextUtils.isEmpty(mOc_root_path)) {
			mOc_root_path_View.setError(getString(R.string.error_field_required));
			focusView = mOc_root_path_View;
			cancel = true;
		} else {
			try {
				URL url = new URL(mOc_root_path);
				if(!url.getProtocol().equals("https"))
					ShowAlertDialog(getString(R.string.login_dialog_title_security_warning),
							getString(R.string.login_dialog_text_security_warning), getActivity());
			} catch (MalformedURLException e) {
				mOc_root_path_View.setError(getString(R.string.error_invalid_url));
				focusView = mOc_root_path_View;
				cancel = true;
				//e.printStackTrace();
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
            editor.commit();

            // Re-init API
            mApi.initApi();

            //((NewsReaderApplication) getActivity().getApplication()).initDaggerAppComponent();
            //((NewsReaderApplication) getActivity().getApplication()).getAppComponent().injectFragment(this);


			final String TAG = LoginDialogFragment.class.getCanonicalName();
            final ProgressDialog mDialogLogin = BuildPendingDialogWhileLoggingIn();
            mDialogLogin.show();


			mApi.getAPI().version()
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
									   Log.v(TAG, "onNext() called with: status = [" + version + "]");

                                       loginSuccessful = true;
                                       mPrefs.edit().putString(Constants.NEWS_WEB_VERSION_NUMBER_STRING, version.version).apply();

                                       if(version.version.equals("0")) {
                                           ShowAlertDialog(getString(R.string.login_dialog_title_error), getString(R.string.login_dialog_text_zero_version_code), getActivity());
                                           loginSuccessful = false;
                                       }
								   }

								   @Override
								   public void onError(@NonNull Throwable e) {
                                       mDialogLogin.dismiss();
									   Log.v(TAG, "onError() called with: e = [" + e + "]");

                                       Throwable t = OkHttpSSLClient.HandleExceptions(e);
                                       ShowAlertDialog(getString(R.string.login_dialog_title_error), t.getMessage(), getActivity());
								   }

								   @Override
								   public void onComplete() {
                                       mDialogLogin.dismiss();

									   Log.v(TAG, "onComplete() called");

                                       if(loginSuccessful) {
                                           //Reset Database
                                           DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getActivity());
                                           dbConn.resetDatabase();

                                           listener.LoginSucceeded();
                                           LoginDialogFragment.this.getDialog().cancel();
                                           if(mActivity instanceof AuthenticatorActivity)
                                               mActivity.finish();
                                       }
                                   }
							   });
		}
	}

	public static void ShowAlertDialog(String title, String text, Activity activity)
	{
		// Linkify the message
		final SpannableString s = new SpannableString(text);
		Linkify.addLinks(s, Linkify.ALL);

		AlertDialog aDialog = new AlertDialog.Builder(activity)
				.setTitle(title)
				.setMessage(s)
				.setPositiveButton(activity.getString(android.R.string.ok) , null)
				.create();
		aDialog.show();

		// Make the textview clickable. Must be called after show()
		((TextView)aDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
	}
}

package de.luhmer.owncloudnewsreader.authentication;

import android.content.Context;

import de.luhmer.owncloudnewsreader.R;

public class AccountGeneral {

	/**
	 * Account name
	 */
	public static final String ACCOUNT_NAME = "ownCloud News";

	
	/**
	 * Auth token types
	 */
	public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
	public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an Nextcloud News account";

	public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
	public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an Nextcloud News account";

	/**
	 * Account type id
	 */
	public static String getAccountType(Context context) {
		return context.getString(R.string.account_type);
	}
}

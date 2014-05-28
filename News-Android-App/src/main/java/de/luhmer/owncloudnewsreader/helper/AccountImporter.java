package de.luhmer.owncloudnewsreader.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;

/**
 * Created by david on 28.05.14.
 */
public class AccountImporter {



    public static void findAccounts(Activity activity) {
        final Handler handler = new Handler();

        final AccountManager accMgr = AccountManager.get(activity);
        Account[] accounts;
        final Account account;
        final AccountManagerFuture<Bundle> amf;
        String authTokenType;

        List<String> accountsAvailable = new ArrayList<String>();
        //Remove all accounts first
        accounts = accMgr.getAccounts();
        for (int index = 0; index < accounts.length; index++) {
            if (accounts[index].type.intern() == AccountGeneral.ACCOUNT_TYPE) {
                accountsAvailable.add(accounts[index].name);
                accountsAvailable.add(accounts[index].type);
            }
        }


        authTokenType = "com.google";
        accounts = accMgr.getAccountsByType(authTokenType);
        account = accounts[2];

        amf = accMgr.getAuthToken(account, authTokenType, true,
                new AccountManagerCallback<Bundle>() {

                    @Override
                    public void run(AccountManagerFuture<Bundle> arg0) {

                        try {
                            Bundle result;
                            Intent i;
                            String token;

                            result = arg0.getResult();
                            if (result.containsKey(AccountManager.KEY_INTENT)) {
                                i = (Intent) result.get(AccountManager.KEY_INTENT);
                                if (i.toString().contains("GrantCredentialsPermissionActivity")) {
                                    // Will have to wait for the user to accept
                                    // the request therefore this will have to
                                    // run in a foreground application
                                    //cbt.startActivity(i);
                                } else {
                                    //cbt.startActivity(i);
                                }

                            } else {
                                token = (String) result.get(AccountManager.KEY_AUTHTOKEN);

                                /*
                                 * work with token
                                 */

                                // Remember to invalidate the token if the web service rejects it
                                // if(response.isTokenInvalid()){
                                //    accMgr.invalidateAuthToken(authTokenType, token);
                                // }

                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                }, handler
        );

    }

}

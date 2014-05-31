package de.luhmer.owncloudnewsreader.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.interfaces.IAccountImport;

/**
 * Created by david on 28.05.14.
 */
public class AccountImporter {

    public static List<Account> findAccounts(Activity activity) {
        final AccountManager accMgr = AccountManager.get(activity);
        final Account[] accounts = accMgr.getAccounts();

        List<Account> accountsAvailable = new ArrayList<Account>();
        for (Account account : accounts) {
            String aType = account.type.intern();

            if (//aType.equals("org.dmfs.caldav.account") ||
                // aType.equals("org.dmfs.carddav.account") ||
                    aType.equals("de.luhmer.tasksync") ||
                            aType.equals("owncloud")) {
                //accountsAvailable.add(accounts[index].type);
                accountsAvailable.add(account);
            }
        }

        return accountsAvailable;
    }




    public static void getAuthTokenForAccount(Activity activity, final Account account, final IAccountImport accountImport) {


        final AccountManager accMgr = AccountManager.get(activity);
        /*
        accMgr.getAuthToken(account, AccountGeneral.ACCOUNT_TYPE, null, activity, new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    // If the user has authorized your application to use the tasks API
                    // a token is available.
                    String token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                    // Now you can use the Tasks API...
                    if(accountImport != null) {
                        accountImport.accountAccessGranted(account, token);
                    }
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                    // TODO: The user has denied you access to the API, you should handle that
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
        */


        /*
        if(accountImport != null) {
            accountImport.accountAccessGranted(account, null);
        }
        */

        //accMgr.invalidateAuthToken(account.type, AccountGeneral.ACCOUNT_TYPE);
        //accMgr.invalidateAuthToken(account.type, null);

        final AlertDialog aDialog = new AlertDialog.Builder(activity)
                                    .setTitle("Account Importer")
                                    .setMessage("Please grant access to the selected account in the notification bar!")
                                    .create();

        aDialog.show();

        String authTokenType;
        if(account.type.equals("owncloud")) {
            authTokenType = "owncloud.password";
        } else {
            authTokenType = "de.luhmer.tasksync";
        }


        final Handler handler = new Handler();
        accMgr.getAuthToken(account, authTokenType, true,
                new AccountManagerCallback<Bundle>() {

                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {

                        try {
                            // If the user has authorized your application to use the tasks API
                            // a token is available.
                            //String token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                            // Now you can use the Tasks API...
                            if(accountImport != null) {
                                accountImport.accountAccessGranted(account, future.getResult());
                            }
                        } catch (OperationCanceledException e) {
                            e.printStackTrace();
                            // TODO: The user has denied you access to the API, you should handle that
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        aDialog.dismiss();

                    }
                }, handler

        );
    }
}

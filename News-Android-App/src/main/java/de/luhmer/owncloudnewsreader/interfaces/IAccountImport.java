package de.luhmer.owncloudnewsreader.interfaces;

import android.accounts.Account;
import android.os.Bundle;

/**
 * Created by David on 28.05.2014.
 */
public interface IAccountImport {
    public void accountAccessGranted(Account account, Bundle data);
}

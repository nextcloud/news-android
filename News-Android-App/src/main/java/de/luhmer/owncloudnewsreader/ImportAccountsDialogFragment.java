package de.luhmer.owncloudnewsreader;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Checkable;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.owncloudnewsreader.ListView.AccountImporterAdapter;
import de.luhmer.owncloudnewsreader.helper.AccountImporter;
import de.luhmer.owncloudnewsreader.interfaces.IAccountImport;

/**
 * Created by David on 16.05.2014.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ImportAccountsDialogFragment extends DialogFragment {

    public IAccountImport accountImport;

    static ImportAccountsDialogFragment newInstance() {
        return new ImportAccountsDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.import_accounts_fragment, null);
        ButterKnife.inject(this, view);

        accountImport = LoginDialogFragment.getInstance();


        final List<Account> accounts = AccountImporter.findAccounts(getActivity());
        List<AccountImporterAdapter.SingleAccount> accountList = new ArrayList<AccountImporterAdapter.SingleAccount>();
        for(Account account : accounts)
            accountList.add(new AccountImporterAdapter.SingleAccount(account.type, account.name, false));

        //lvAccounts.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_single_choice, accountNames));
        lvAccounts.setAdapter(new AccountImporterAdapter(getActivity(), accountList.toArray(new AccountImporterAdapter.SingleAccount[accountList.size()]), lvAccounts));

        lvAccounts.setItemsCanFocus(false);
        lvAccounts.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.import_account_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int x) {

                        for (int i = 0; i < lvAccounts.getAdapter().getCount(); i++) {
                            if (lvAccounts.getChildAt(i) instanceof Checkable && ((Checkable) lvAccounts.getChildAt(i)).isChecked()) {

                                AccountImporter.getAuthTokenForAccount(getActivity(), accounts.get(i), accountImport);

                                /*
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                intent.putExtra(LoginActivity.NEW_ACCOUNT, true);
                                intent.putExtra(URL_STRING, calendars.get(i));
                                intent.putExtra(USERNAME_STRING, username);
                                intent.putExtra(PASSWORD_STRING, password);
                                startActivity(intent);
                                */
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create();
    }


    @InjectView(R.id.lvAccounts) ListView lvAccounts;
    //@InjectView(R.id.pbProgress) ProgressBar pbProgress;

    static final Pattern RemoveAllDoubleSlashes = Pattern.compile("(?<!:)\\/\\/");
    public static String validateURL(String url) {
        return RemoveAllDoubleSlashes.matcher(url).replaceAll("/");
    }

}

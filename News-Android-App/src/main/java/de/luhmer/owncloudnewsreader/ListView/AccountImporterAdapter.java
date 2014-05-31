package de.luhmer.owncloudnewsreader.ListView;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.helper.CheckableLinearLayout;

/**
 * Created by David on 29.05.2014.
 */
public class AccountImporterAdapter extends ArrayAdapter<AccountImporterAdapter.SingleAccount> implements AdapterView.OnItemClickListener {

    Context context;
    //SingleAccount[] accounts;
    LayoutInflater inflater;

    public AccountImporterAdapter(Activity context, SingleAccount[] accounts, ListView listView) {
        super(context, R.layout.simple_list_item_single_choice, accounts);
        this.context = context;

        listView.setOnItemClickListener(this);

        inflater = context.getLayoutInflater();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.simple_list_item_single_choice, parent, false);
            holder = new ViewHolder(view);

            /*
            holder.cbChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(compoundButton.isChecked()) {
                        for (int i = 0; i < getCount(); i++) {
                            getItem(i).checked = false;
                        }
                        getItem(position).checked = true;
                        notifyDataSetChanged();
                    }
                }
            });
            */

            view.setTag(holder);
        }

        holder.text1.setText(getItem(position).type);
        holder.text2.setText(getItem(position).url);
        holder.cbChecked.setChecked(getItem(position).checked);


        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int id, long l) {
        for (int i = 0; i < getCount(); i++) {
            getItem(i).checked = false;
        }
        ((CheckableLinearLayout)view).toggle();

        notifyDataSetChanged();
    }



    static class ViewHolder {
        @InjectView(R.id.text1) TextView text1;
        @InjectView(R.id.text2) TextView text2;
        @InjectView(R.id.checkbox) CheckBox cbChecked;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }





    public static class SingleAccount {

        public SingleAccount(String type, String url, Boolean checked) {
            this.type = type;
            this.url = url;
            this.checked = checked;
        }

        public String type;
        public String url;

        public boolean checked;
    }

}

package de.luhmer.owncloudnewsreader.cursor;

import de.luhmer.owncloudnewsreader.R;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SubscriptionCursorAdapter extends CursorAdapter {

	@SuppressWarnings("deprecation")
	public SubscriptionCursorAdapter(Context context, Cursor c) {
		super(context, c);		
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        TextView textViewPersonName = (TextView) view.findViewById(R.id.summary);
        textViewPersonName.setText(cursor.getString(1));
	}

	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup parent) {
		// when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.subscription_list_item, parent, false);
        retView.setTag(cursor.getString(0));
        return retView;
	}

}

package de.luhmer.owncloudnewsreader.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.model.Folder;

public class FolderArrayAdapter extends ArrayAdapter<Folder> {

   private LayoutInflater inflater;

    public FolderArrayAdapter(final Context context, final List<Folder> folders) {
        super(context, R.layout.dialog_list_folder, folders );
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Folder folder = getItem(position);

        View rowView = inflater.inflate(R.layout.dialog_list_folder, parent, false);
        TextView textView = rowView.findViewById(R.id.rowTextView);
        textView.setText(folder.getLabel());

        return rowView;
    }

}

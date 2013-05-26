package de.luhmer.owncloudnewsreader.ListView;

import android.view.View;
import de.luhmer.owncloudnewsreader.data.ConcreteSubscribtionItem;

public interface NewsReader_Row {
    public View getView(View convertView);
    public int getViewType();
    public ConcreteSubscribtionItem getSubscItem();
}
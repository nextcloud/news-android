package de.luhmer.owncloudnewsreader.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService {
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new WidgetTodoViewsFactory(this.getApplicationContext(), intent);
	}
}

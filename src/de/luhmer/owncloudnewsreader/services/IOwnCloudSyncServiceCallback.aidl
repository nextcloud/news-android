package de.luhmer.owncloudnewsreader.services;

oneway interface IOwnCloudSyncServiceCallback {
	//void doCallback(in String value);
	void startedSyncOfItemStates();
	void startedSyncOfFolders();
	void startedSyncOfFeeds();
	void startedSyncOfItems();
}
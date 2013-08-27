package de.luhmer.owncloudnewsreader.services;

import de.luhmer.owncloudnewsreader.helper.AidlException;

oneway interface IOwnCloudSyncServiceCallback {
	//void doCallback(in String value);
	void startedSyncOfItemStates();
	void finishedSyncOfItemStates();	
	
	void startedSyncOfFolders();
	void finishedSyncOfFolders();
	
	void startedSyncOfFeeds();
	void finishedSyncOfFeeds();
	
	void startedSyncOfItems();
	void finishedSyncOfItems();
	
	void throwException(out AidlException ex);
}
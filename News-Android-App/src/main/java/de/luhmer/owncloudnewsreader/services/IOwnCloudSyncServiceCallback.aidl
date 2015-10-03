package de.luhmer.owncloudnewsreader.services;

import de.luhmer.owncloudnewsreader.helper.AidlException;

oneway interface IOwnCloudSyncServiceCallback {
	//void doCallback(in String value);
	
	void startedSync();
	void finishedSync();

	void throwException(out AidlException ex);
}
package de.luhmer.owncloudnewsreader.services;

import de.luhmer.owncloudnewsreader.helper.AidlException;

oneway interface IOwnCloudSyncServiceCallback {
	//void doCallback(in String value);
	
	void startedSync(String sync_type);
	void finishedSync(String sync_type);	
		
	void throwException(out AidlException ex);
}
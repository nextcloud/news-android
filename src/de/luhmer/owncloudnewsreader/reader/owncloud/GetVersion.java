package de.luhmer.owncloudnewsreader.reader.owncloud;

import org.json.JSONObject;

public class GetVersion implements IHandleJsonObject{	

	String version;
	
	@Override
	public void performAction(JSONObject jObj) {		
		this.version = jObj.optString("version");
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	
}

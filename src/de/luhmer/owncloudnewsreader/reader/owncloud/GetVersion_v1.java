package de.luhmer.owncloudnewsreader.reader.owncloud;

import org.json.JSONObject;

public class GetVersion_v1 implements IHandleJsonObject {	

	String version;
	
	@Override
	public void performAction(JSONObject jObj) {		
		this.version = jObj.optJSONObject("ocs").optJSONObject("data").optString("version");
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	
}

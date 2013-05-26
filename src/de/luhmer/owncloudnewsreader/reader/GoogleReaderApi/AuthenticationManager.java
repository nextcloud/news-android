package de.luhmer.owncloudnewsreader.reader.GoogleReaderApi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class AuthenticationManager {
	
	
	public static String getGoogleAuthKey(String _USERNAME, String _PASSWORD) throws UnsupportedEncodingException, IOException {
		Document doc = Jsoup.connect(GoogleReaderConstants._GOOGLE_LOGIN_URL)
			.data("accountType", "GOOGLE",
				"Email", _USERNAME,
				"Passwd", _PASSWORD,
				"service", "reader",
				"source", GoogleReaderConstants.APP_NAME)
			.userAgent(GoogleReaderConstants.APP_NAME)
			.timeout(4000)
			.post();
	 
		// RETRIEVES THE RESPONSE TEXT inc SID and AUTH. We only want the AUTH key.
		String _AUTHKEY = doc.body().text().substring(doc.body().text().indexOf("Auth="), doc.body().text().length());
		_AUTHKEY = _AUTHKEY.replace( "Auth=","" );
		return _AUTHKEY;
    }
	
	public static String getGoogleToken(String _USERNAME, String _PASSWORD) throws UnsupportedEncodingException, IOException {
		Document doc = Jsoup.connect(GoogleReaderConstants._TOKEN_URL)
			.header("Authorization", GoogleReaderConstants._AUTHPARAMS + getGoogleAuthKey(_USERNAME,_PASSWORD))
			.userAgent(GoogleReaderConstants.APP_NAME)
			.timeout(4000)
			.get();
	 
		// RETRIEVES THE RESPONSE TOKEN
		String _TOKEN = doc.body().text();
		return _TOKEN;
	  }
	  
	  public static String getUserInfo(String _USERNAME, String _PASSWORD) throws UnsupportedEncodingException, IOException {
		Document doc = Jsoup.connect(GoogleReaderConstants._USER_INFO_URL)
			.header("Authorization", GoogleReaderConstants._AUTHPARAMS + getGoogleAuthKey(_USERNAME,_PASSWORD))
			.userAgent(GoogleReaderConstants.APP_NAME)
			.timeout(4000)
			.get();
	 
		  // RETRIEVES THE RESPONSE USERINFO
		  String _USERINFO = doc.body().text();
		  return _USERINFO;
	  }
	  
	  public static String getGoogleUserID(String _USERNAME, String _PASSWORD) throws UnsupportedEncodingException, IOException {
			/* USERINFO RETURNED LOOKS LIKE
			* {"userId":"14577161871823252783",
			* "userName":"<username>","userProfileId":"<21 numeric numbers",
			* "userEmail":"<username>@gmail.com",
			* "isBloggerUser":true,
			* "signupTimeSec":1159535065}
			*/
			String _USERINFO = getUserInfo(_USERNAME, _PASSWORD);
			String _USERID = (String) _USERINFO.subSequence(11, 31);
			return _USERID;
		}
	
 /*
	//Get google Auth key
	public static String getGoogleAuthKey(String email,String password)
	{
		try{
			Document doc = Jsoup.connect(Constants.GOOGLE_LOGIN_URL)
								.data("accountType","GOOGLE",
							  "Email",email,
						  "Passwd",password,
						  "service","reader",
						  "source", "MyApp")
						   .timeout(4000)
					   .post();
			return doc.body().text();
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return "NULL";
		}
	}
	 
	//Get token
	public static String getGoogleToken(String authkey)
	{
		try
		{
				Document doc = Jsoup.connect(Constants.TOKEN_URL)
									.header("Authorization",Constants.AUTHPARAMS + authkey)
									.timeout(4000)
									.get();
	 
			// Retrieve the response token
				String _TOKEN = doc.body().text();
				return _TOKEN;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	*/
}
/*
Copyright 2011 Christian Dadswell
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package de.luhmer.owncloudnewsreader.reader.GoogleReaderApi;

import android.annotation.SuppressLint;
import java.util.regex.Pattern;

@SuppressLint("DefaultLocale")
public class Func_Strings {

	// ARRAY TO STRING
	public static String arrayToString(String[] a, String separator) {
		StringBuffer result = new StringBuffer();
		if (a.length > 0) {
			result.append(a[0]);
			for (int i=1; i<a.length; i++) {
				result.append(separator);
				result.append(a[i]);
			}
		}
		return result.toString();
	}
	
	// FIND WORD IN STRING	
	public static boolean FindWordInString(String StringToSearch, String WordToFind) {

		int intIndex = StringToSearch.toLowerCase().indexOf(WordToFind.toLowerCase());
		if(intIndex == - 1){
			return false;
		}else{
			return true;
		}
	}
			
	//STRING REPLACE
	public static String StringToReplace(String string, String stringToReplace) {
			String str = string;
			String strreplace = string;
			String resultantString = str.replaceAll(stringToReplace, strreplace);
			return resultantString;
	}
	
	public static boolean compareStr(String str1, String str2){
			if(str1.equals(str2)) {
					return true;
			}else{
					return false;
			}
			
	}
	
	public static String[] arraySplit(String a, String splitter) {
			String[] x = Pattern.compile(splitter).split(a);
			for (int i=0; i<x.length; i++) {
					System.out.println(" \"" + x[i] + "\"");
	}
			return x;
}

public static String[] stringToArray( String s, String sep ) {
	// convert a String s to an Array, the elements
	// are delimited by sep
	// NOTE : for old JDK only (<1.4).
	//        for JDK 1.4 +, use String.split() instead
	StringBuffer buf = new StringBuffer(s);
	int arraysize = 1;
	for ( int i = 0; i < buf.length(); i++ ) {
	if ( sep.indexOf(buf.charAt(i) ) != -1 )
	  arraysize++;
	}
	String [] elements  = new String [arraysize];
	int y,z = 0;
	if ( buf.toString().indexOf(sep) != -1 ) {
	while (  buf.length() > 0 ) {
	  if ( buf.toString().indexOf(sep) != -1 ) {
		y =  buf.toString().indexOf(sep);
		if ( y != buf.toString().lastIndexOf(sep) ) {
		  elements[z] = buf.toString().substring(0, y ); 
		  z++;
		  buf.delete(0, y + 1);
		}
		else if ( buf.toString().lastIndexOf(sep) == y ) {
		  elements[z] = buf.toString().substring
			(0, buf.toString().indexOf(sep));
		  z++;
		  buf.delete(0, buf.toString().indexOf(sep) + 1);
		  elements[z] = buf.toString();z++;
		  buf.delete(0, buf.length() );
		}
	  }
	}
	}
	else {
	elements[0] = buf.toString(); 
	}
	buf = null;
	return elements;
	}
}
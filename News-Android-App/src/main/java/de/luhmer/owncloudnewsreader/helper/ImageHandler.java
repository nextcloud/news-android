/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.owncloudnewsreader.helper;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageHandler {
    private static final Pattern patternImg = Pattern.compile("<img[^>]*>");
    private static final Pattern patternImgSrcLink = Pattern.compile("src=\"(.*?)\"");

	public static List<String> getImageLinksFromText(String text)
	{
		List<String> links = new ArrayList<>();

		Matcher matcher = patternImg.matcher(text);
	    // Check all occurrences
	    while (matcher.find()) {
	    	Matcher matcherSrcLink = patternImgSrcLink.matcher(matcher.group());
	    	if(matcherSrcLink.find()) {
                String link = matcherSrcLink.group(1);
                if(link.startsWith("//")) { //Maybe the text contains image urls without http or https prefix.
                    link = "https:" + link;
                }
	    		links.add(link);
	    	}
	    }
	    return links;
	}

	public static void clearCache()
	{
        ImageLoader.getInstance().clearDiskCache();
        ImageLoader.getInstance().clearMemoryCache();
	}
}

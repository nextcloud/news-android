/*
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

import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageHandler {
    private static final String TAG = "[ImageHandler]";
    private static final Pattern patternImg = Pattern.compile("<img[^>]*>");
    private static final Pattern patternImgSrcLink = Pattern.compile("src=\"(.*?)\"");

	public static List<String> getImageLinksFromText(String articleUrl, String text)
	{
		List<String> links = new ArrayList<>();

		Matcher matcher = patternImg.matcher(text);
	    // Check all occurrences
	    while (matcher.find()) {
	    	Matcher matcherSrcLink = patternImgSrcLink.matcher(matcher.group());
	    	if(matcherSrcLink.find()) {
                String link = matcherSrcLink.group(1);
                if(link != null && link.startsWith("//")) { //Maybe the text contains image urls without http or https prefix.
                    link = "https:" + link;
                }

                // the android universal image loader doesn't support svg images. Therefore we don't want to load them through UIL
                if(link.endsWith(".svg")) {
                    Log.d(TAG, "detected unsupported svg image in article: " + articleUrl + " -> " + link);
                } else {
                    links.add(link);
                }
	    	}
	    }
	    return links;
	}

    public static String fixBrokenImageLinksInArticle(String articleUrl, String text)
    {
        Matcher matcher = patternImg.matcher(text);
        // Check all occurrences
        while (matcher.find()) {
            Matcher matcherSrcLink = patternImgSrcLink.matcher(matcher.group());
            if(matcherSrcLink.find()) {
                String link = matcherSrcLink.group(1);
                String originalLink = link;
                String originalArticleUrl = articleUrl;
                if(link != null) {
                    if(link.startsWith("//")) { //Maybe the text contains image urls without http or https prefix.
                        link = "https:" + link;
                    } else if (link.startsWith("/")) { // detected absolute url
                        try {
                            URI uri = new URI(articleUrl);
                            String domain = uri.getHost();
                            link = uri.getScheme() + "://" + domain + link;
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        }
                    } else {
                        // check if we have relative urls such as
                        // ./abc.jpeg or ./../abc.jpeg, ../abc.jpeg or ../../abc.jpeg
                        boolean linkNeedsHost = false;
                        if(link.startsWith("./")) {
                            link = link.substring(2); // remove ./ from link
                            linkNeedsHost = true;
                        }

                        // if link is relative without anything else in front (e.g. pix/wow.svg)
                        if(!link.startsWith("http") && !link.startsWith(".")) {
                            linkNeedsHost = true;
                            if(!articleUrl.endsWith("/")) {
                                // remove last part of article url to get a relative url
                                articleUrl = sliceLastPathOfUrl(articleUrl);
                            }
                        }

                        // in case the article url is of type articles/matrix-vs-xmpp.html we need to remove the file plus the first part of the url
                        if(link.startsWith("../") && !articleUrl.endsWith("/")) {
                            linkNeedsHost = true;
                            articleUrl = sliceLastPathOfUrl(articleUrl);
                            articleUrl = sliceLastPathOfUrl(articleUrl);
                            link = link.substring(3); // remove ../ from link
                        }

                        // if the article urls ends with an / we can just remove it piece by piece
                        while(link.startsWith("../")) {
                            linkNeedsHost = true;
                            articleUrl = sliceLastPathOfUrl(articleUrl);
                            link = link.substring(3); // remove ../ from link
                        }

                        if(linkNeedsHost) {
                            link = articleUrl + "/" + link;
                        }
                    }
                }

                if(!originalLink.equals(link)) {
                    Log.d(TAG, "Fixed link from: " + originalArticleUrl + " and " + originalLink + " -> " + link);
                    // text = text.replaceAll(originalLink, link); // this causes OutOfMemoryExceptions (https://github.com/nextcloud/news-android/issues/1055)

                    Pattern URL_PATTERN = Pattern.compile(originalLink);
                    Matcher urlMatcher = URL_PATTERN.matcher(text);
                    return urlMatcher.replaceAll(link);

                }
            }
        }
        return text;
    }

    private static String sliceLastPathOfUrl(String url) {
	    int idx = url.lastIndexOf("/");
        int countOfSlashes = url.split("/").length - 1;
        // Log.d(TAG, url + " " + countOfSlashes);
        // make sure we don't count into the domain name (at least two slashes for ://)
	    if(idx > 0 && countOfSlashes > 2) {
            return url.substring(0, idx);
        } else {
	        return url;
        }
    }

    public static void clearCache()
    {
        if(ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().clearDiskCache();
            ImageLoader.getInstance().clearMemoryCache();
        }
    }
}

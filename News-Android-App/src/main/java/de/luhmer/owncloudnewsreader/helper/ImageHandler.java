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

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageHandler {
    private static final String TAG = "[ImageHandler]";
    private static final Pattern patternImg = Pattern.compile("<img[^>]*>");
    private static final Pattern patternImgSrcLink = Pattern.compile("src=\"(.*?)\"");
    private static final Pattern patternHref = Pattern.compile("<a[^>]*>");
    private static final Pattern patternHrefLink = Pattern.compile("href=\"(.*?)\"");

    public static List<String> getImageLinksFromText(String articleUrl, String text) {
		List<String> links = new ArrayList<>();

		Matcher matcher = patternImg.matcher(text);
	    // Check all occurrences
	    while (matcher.find()) {
	    	Matcher matcherSrcLink = patternImgSrcLink.matcher(matcher.group());
	    	if(matcherSrcLink.find()) {
                String link = matcherSrcLink.group(1);

                if (link != null) {
                    if (link.startsWith("//")) { //Maybe the text contains image urls without http or https prefix.
                        link = "https:" + link;
                    }

                    // the android universal image loader doesn't support svg images. Therefore we don't want to load them through UIL
                    if (link.endsWith(".svg")) {
                        Log.d(TAG, "detected unsupported svg image in article: " + articleUrl + " -> " + link);
                    } else {
                        links.add(link);
                    }
                }
	    	}
	    }
	    return links;
	}

    public static String fixBrokenImageLinksInArticle(String articleUrl, String text) {
        return fixBrokenLinkInArticle(articleUrl, text, patternImg, patternImgSrcLink, "src");
    }

    public static String fixBrokenHrefInArticle(String articleUrl, String text) {
        return fixBrokenLinkInArticle(articleUrl, text, patternHref, patternHrefLink, "href");
    }

    public static String fixBrokenLinkInArticle(String articleUrl, String text, Pattern matcherElement, Pattern matcherLink, String htmlAttribut) {
        Matcher matcher = matcherElement.matcher(text);
        // Check all occurrences
        while (matcher.find()) {
            Matcher matcherSrcLink = matcherLink.matcher(matcher.group());
            if(matcherSrcLink.find()) {
                String link = matcherSrcLink.group(1);
                String originalLink = link;
                String originalArticleUrl = articleUrl;
                if(link != null) {
                    if(link.startsWith("//")) { //Maybe the text contains image urls without http or https prefix.
                        // System.out.println("CASE_MISSING_PROTOCOL");
                        link = "https:" + link;
                    } else if (link.startsWith("/")) { // detected absolute url
                        // System.out.println("CASE_ABSOLUTE_URL");
                        try {
                            URL uri = new URL(articleUrl);
                            String protocol = uri.getProtocol();
                            String authority = uri.getAuthority();
                            link = String.format("%s://%s", protocol, authority) + link;
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        }
                    } else {
                        // check if we have relative urls such as
                        // ./abc.jpeg or ./../abc.jpeg, ../abc.jpeg or ../../abc.jpeg
                        boolean linkNeedsHost = false;
                        if(link.startsWith("./")) {
                            //Log.d(TAG, "fix relative url (remove ./ in front)");
                            link = link.substring(2); // remove ./ from link
                            linkNeedsHost = true;
                        }

                        // if link is relative without anything else in front (e.g. pix/wow.svg)
                        if(!link.startsWith("http") && !link.startsWith(".") && !"about:blank".equals(articleUrl)) {
                            if(!link.contains("/")) {
                                // could be just a domain name or a reference to a file in the same directory (either way we should leave it as it is)
                                //System.out.println("CASE_RELATIVE_DOMAIN_OR_FILE");
                            } else {
                                String lastPartOfUrl = getFileName(link);

                                // the link ends with a filname (e.g. "test.jpg") - therefore we can assume that it is a relative url
                                if(lastPartOfUrl.contains(".")) {
                                    if(!articleUrl.endsWith("/")) {
                                        // the article contains a file in the end (doesn't end with "/") - therefore we need to remove the last part of the article URL
                                        // System.out.println("CASE_RELATIVE_FILE_END");
                                        // remove last part of article url to get a relative url
                                        articleUrl = sliceLastPathOfUrl(articleUrl);
                                        linkNeedsHost = true;
                                    } else {
                                        // article URL ends with a "/" so we can just append it
                                        // System.out.println("CASE_RELATIVE_ADD_HOST");
                                        linkNeedsHost = true;
                                    }
                                } else {
                                    // in case we have an url such as astralcodexten.substack.com/subscribe we assume that it is a path and we should not modify it
                                    // System.out.println("CASE_RELATIVE_DOMAIN_SUBPATH");
                                }
                            }
                        }

                        // in case the article url is of type articles/matrix-vs-xmpp.html we need to remove the file plus the first part of the url
                        if(link.startsWith("../") && !articleUrl.endsWith("/")) {
                            // System.out.println("CASE_RELATIVE_PARENT");
                            linkNeedsHost = true;
                            articleUrl = sliceLastPathOfUrl(articleUrl);
                            articleUrl = sliceLastPathOfUrl(articleUrl);
                            link = link.substring(3); // remove ../ from link
                        }

                        // if the article urls ends with an / we can just remove it piece by piece
                        while(link.startsWith("../")) {
                            // System.out.println("CASE_RELATIVE_PARENT");
                            linkNeedsHost = true;
                            articleUrl = sliceLastPathOfUrl(articleUrl);
                            link = link.substring(3); // remove ../ from link
                        }

                        if(linkNeedsHost) {
                            // concat article url + link (and make sure that we have only one /)
                            if(articleUrl.endsWith("/")) {
                                link = articleUrl + link;
                            } else {
                                link = articleUrl + "/" + link;
                            }
                        }
                    }
                }

                if(!originalLink.equals(link)) {
                    // String l = "Fixed link in article: " + originalArticleUrl + ": " + originalLink + " -> " + link;
                    // Log.d(TAG, l);
                    // text = text.replaceAll(originalLink, link); // this causes OutOfMemoryExceptions (https://github.com/nextcloud/news-android/issues/1055)

                    Pattern URL_PATTERN = Pattern.compile(String.format("%s=\"%s\"", htmlAttribut, originalLink));
                    Matcher urlMatcher = URL_PATTERN.matcher(text);
                    text = urlMatcher.replaceAll(String.format("%s=\"%s\"", htmlAttribut, link));
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

    private static String getFileName(String url) {
        int idx = url.lastIndexOf("/");
        int countOfSlashes = url.split("/").length - 1;
        if(idx > 0) {
            return url.substring(idx);
        } else {
            return url;
        }
    }

    public static void clearCache(Context context)
    {
        Glide.get(context).clearMemory();
        Glide.get(context).clearDiskCache();
    }
}

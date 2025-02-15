package de.luhmer.owncloudnewsreader.async_tasks;

import static de.luhmer.owncloudnewsreader.NewsDetailActivity.INCOGNITO_MODE_ENABLED;
import static de.luhmer.owncloudnewsreader.helper.ThemeChooser.THEME;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;


public class RssItemToHtmlTask extends AsyncTask<Void, Void, String> {

    private static final double BODY_FONT_SIZE = 1.1;
    private static final double HEADING_FONT_SIZE = 1.1;
    private static final double SUBSCRIPT_FONT_SIZE = 0.7;
    private static final String TAG = RssItemToHtmlTask.class.getCanonicalName();

    private static final Pattern PATTERN_PRELOAD_VIDEOS_REMOVE = Pattern.compile("(<video[^>]*)(preload=\".*?\")(.*?>)");
    private static final Pattern PATTERN_PRELOAD_VIDEOS_INSERT = Pattern.compile("(<video[^>]*)(.*?)(.*?>)");
    private static final Pattern PATTERN_AUTOPLAY_VIDEOS_1 = Pattern.compile("(<video[^>]*)(autoplay=\".*?\")(.*?>)");
    private static final Pattern PATTERN_AUTOPLAY_VIDEOS_2 = Pattern.compile("(<video[^>]*)(\\sautoplay)(.*?>)");
    // private static final Pattern PATTERN_AUTOPLAY_REGEX_CB = Pattern.compile("(.*?)^(Unser Feedsponsor:\\s*<\\/p><p>\\s*.*?\\s*<\\/p>)(.*)", Pattern.MULTILINE);
    private static final Pattern PATTERN_PRE_BLOCK = Pattern.compile("<pre>(.*?)</pre>", Pattern.MULTILINE | Pattern.DOTALL);

    private final RssItem mRssItem;
    private final Listener mListener;
    private final SharedPreferences mPrefs;
    private final boolean isRightToLeft;
    private final RequestManager mGlide;

    public interface Listener {
        /**
         * The RSS item has successfully been parsed.
         * @param htmlPage  RSS item as HTML string
         */
        void onRssItemParsed(String htmlPage);
    }


    public RssItemToHtmlTask(Context context, RssItem rssItem, Listener listener, SharedPreferences prefs) {
        this.mRssItem = rssItem;
        this.mListener = listener;
        this.mPrefs = prefs;
        this.mGlide = Glide.with(context);

        this.isRightToLeft = context.getResources().getBoolean(R.bool.is_right_to_left);
    }

    @Override
    protected String doInBackground(Void... params) {
        return getHtmlPage(this.mGlide, mRssItem, true, mPrefs, isRightToLeft);
    }

    @Override
    protected void onPostExecute(String htmlPage) {
        mListener.onRssItemParsed(htmlPage);
        super.onPostExecute(htmlPage);
    }

    public static String getHtmlPage(RequestManager glide, RssItem rssItem, boolean showHeader, SharedPreferences mPrefs, Context context) {
        return getHtmlPage(glide, rssItem, showHeader, mPrefs, context.getResources().getBoolean(R.bool.is_right_to_left));
    }

    /**
     * @param rssItem       item to parse
     * @param showHeader    true if a header with item title, feed title, etc. should be included
     * @return given RSS item as full HTML page
     */
    public static String getHtmlPage(RequestManager glide, RssItem rssItem, boolean showHeader, SharedPreferences mPrefs, boolean isRightToLeft) {
        boolean incognitoMode = mPrefs.getBoolean(INCOGNITO_MODE_ENABLED, false);

        String favIconUrl = null;

        Feed feed = rssItem.getFeed();

        //int feedColor = colors[0];
        if (feed != null) {
            favIconUrl = feed.getFaviconUrl();
        }

        if (favIconUrl != null) {
            favIconUrl = getCachedFavIcon(glide, favIconUrl);
        } else {
            favIconUrl = "file:///android_res/drawable/default_feed_icon_light.png";
        }

        String body_id = getSelectedTheme();
        Log.v(TAG, "Selected Theme: " + body_id);

        String rtlClass = isRightToLeft ? "rtl" : "";
        String rtlDir = isRightToLeft ? "rtl" : "ltr";

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("<html dir=\"%s\"><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=0\" />", rtlDir));
        builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"web.css\" />");

        // font size scaling
        // builder.append("<style type=\"text/css\">");
        // builder.append(getFontSizeScalingCss(mPrefs));
        // builder.append("</style>");

        builder.append(String.format("</head><body class=\"%s %s\">", body_id, rtlClass));

        if (showHeader) {
            builder.append(
                buildHeader(rssItem, body_id, favIconUrl)
            );
        }

        String description = rssItem.getBody();

        if (!description.isEmpty()) {
            description = removeLineBreaksFromHtml(description);
        }
        else if(rssItem.getMediaDescription() != null) {
            // in case the rss body is empty, fallback to the media description (e.g. youtube / ted talks)
            description = rssItem.getMediaDescription();
        }

        if(!incognitoMode) {
            // If incognito mode is disabled, try getting images from cache
            description = getDescriptionWithCachedImages(glide, rssItem.getLink(), description).trim();
        } else {
            // When incognito is on, we need to provide some error handling
            //description = description.replaceAll("<img", "<img onerror=\"this.style='width: 40px !important; height: 40px !important'\" ");
            description = description.replaceAll("<img", "<img onerror=\\\"this.onerror=null;this.src='file:///android_asset/broken-image.png';this.style='margin-left: 0px !important; width: 80px !important; height: 80px !important'\\\"");
        }
        description = replacePatternInText(PATTERN_PRELOAD_VIDEOS_REMOVE, description, "$1 $3"); // remove whatever preload is there
        description = replacePatternInText(PATTERN_PRELOAD_VIDEOS_INSERT, description, "$1 preload=\"metadata\" $3"); // add preload attribute
        description = replacePatternInText(PATTERN_AUTOPLAY_VIDEOS_1, description, "$1 $3");
        description = replacePatternInText(PATTERN_AUTOPLAY_VIDEOS_2, description, "$1 $3");

        //description = replacePatternInText(PATTERN_AUTOPLAY_REGEX_CB, description, "$1 $3");

        builder.append("<div id=\"content\">");
        builder.append(description);
        builder.append("</div>");

        builder.append("</body></html>");

        return builder.toString().replaceAll("\"//", "\"https://");
    }

    @VisibleForTesting()
    public static String removeLineBreaksFromHtml(String description) {
        // UUID is used so there is only a very small chance that the placeholder text actually exists in the article
        var uuid = UUID.randomUUID().toString();

        // pre-blocks shouldn't have their formatting changed
        var matcher = PATTERN_PRE_BLOCK.matcher(description);
        var preBlocks = new ArrayList<String>();

        while (matcher.find()) {
            var group = matcher.group();
            description = description.replaceFirst(Pattern.quote(group), "PRE_BLOCK_THAT_WILL_BE_REPLACED_" + uuid + "_" + preBlocks.size());
            preBlocks.add(group);
        }

        description = description
                .replaceAll("\n\n", "THIS_WILL_BE_BECOME_ONE_NEWLINE_LATER_" + uuid) // This is required because otherwise `\n\n` would become 2 spaces
                .replaceAll(">\n", ">") // The first character after a tag shouldn't have a space
                .replaceAll("\n", " ")
                .replaceAll("THIS_WILL_BE_BECOME_ONE_NEWLINE_LATER_" + uuid, "\n");

        for (int i = 0; i < preBlocks.size(); i++) {
            description = description.replaceFirst(
                    "PRE_BLOCK_THAT_WILL_BE_REPLACED_" + uuid + "_" + i,
                    Matcher.quoteReplacement(preBlocks.get(i))
            );
        }
        return description;
    }

    private static String getSelectedTheme() {
        THEME selectedTheme = ThemeChooser.getSelectedTheme();
        switch (selectedTheme) {
            case LIGHT:
                return "lightTheme";
            case DARK:
                return "darkTheme";
            case OLED:
                return "darkThemeOLED";
            default:
                return null;
        }
    }

    private static String buildHeader(RssItem rssItem, String body_id, String favIconUrl) {
        StringBuilder builder = new StringBuilder();

        builder.append("<div id=\"top_section\">");
        builder.append(String.format("<div id=\"header\" class=\"%s\">", body_id));
        String itemTitle = Html.escapeHtml(rssItem.getTitle());
        String linkToFeed = Html.escapeHtml(rssItem.getLink());
        builder.append(String.format("<a href=\"%s\">%s</a>", linkToFeed, itemTitle));
        builder.append("</div>");

        String authorLine = Html.escapeHtml(rssItem.getAuthor());
        if ("".equals(authorLine)) { // If author is empty, use name of feed instead
            Feed feed = rssItem.getFeed();
            if (feed != null) {
                authorLine = feed.getFeedTitle();
            }
        }

        builder.append("<div id=\"header_small_text\">");

        builder.append("<div id=\"subscription\">");
        builder.append(String.format("<img id=\"imgFavicon\" src=\"%s\" />", favIconUrl));
        builder.append(String.format("<span>%s</span>", authorLine.trim()));
        builder.append("</div>");

        Date date = rssItem.getPubDate();
        if (date != null) {
            String dateString = (String) DateUtils.getRelativeTimeSpanString(date.getTime());
            builder.append("<div id=\"datetime\">");
            builder.append(dateString);
            builder.append("</div>");
        }

        builder.append("</div>");
        builder.append("</div>");

        return builder.toString();
    }

    private static String getCachedFavIcon(RequestManager glide, String favIconUrl) {
        File file = null;
        try {
            file = glide
                    .asFile()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .onlyRetrieveFromCache(true)
                    .load(favIconUrl)
                    .submit()
                    .get();
        } catch (Exception e) {
            Log.w(TAG, "favicon is not cached");
        }

        if (file != null) {
            Log.d(TAG, "favicon is cached!");
            return "file://" + file.getAbsolutePath();
        } else {
            return favIconUrl; // Return favicon url if not cached
        }
    }

    /*
    private static String getFontSizeScalingCss(SharedPreferences mPrefs) {
        // font size scaling
        double scalingFactor = Float.parseFloat(mPrefs.getString(SettingsActivity.SP_FONT_SIZE, "1.0"));
        DecimalFormat fontFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
        return String.format(
            Locale.US,
            """
            :root {\s
                --fontsize-body: %sem;\s
                --fontsize-header: %sem;\s
                --fontsize-subscript: %sem;\s
            }
            """,
            fontFormat.format(scalingFactor * BODY_FONT_SIZE),
            fontFormat.format(scalingFactor * HEADING_FONT_SIZE),
            fontFormat.format(scalingFactor * SUBSCRIPT_FONT_SIZE)
        );
    }
    */

    private static String getDescriptionWithCachedImages(RequestManager glide, String articleUrl, String text) {
        List<String> links = ImageHandler.getImageLinksFromText(articleUrl, text);

        for(String link : links) {
            link = link.trim();
            try {
                File file = null;
                try {
                    file = glide
                            .asFile()
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .onlyRetrieveFromCache(true)
                            // .listener(rl)
                            .load(link)
                            .submit()
                            .get();
                    Log.d(TAG, "image is cached");
                } catch (Exception e) {
                    Log.w(TAG, "image is not cached");
                }
                if(file != null) {
                    text = text.replace(link, "file://" + file.getAbsolutePath());
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        return text;
    }

    private static final RequestListener<File> rl = new RequestListener<>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
            // Log the GlideException here (locally or with a remote logging framework):
            Log.e(TAG, "Load failed", e);

            // You can also log the individual causes:
            for (Throwable t : e.getRootCauses()) {
                Log.e(TAG, "Caused by", t);
            }
            // Or, to log all root causes locally, you can use the built in helper method:
            e.logRootCauses(TAG);

            return false; // Allow calling onLoadFailed on the Target.
        }

        @Override
        public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
            // Log successes here or use DataSource to keep track of cache hits and misses.
            return false; // Allow calling onResourceReady on the Target.
        }

    };

    private static String replacePatternInText(Pattern pattern, String text, String replacement) {
        Matcher m = pattern.matcher(text);
        return m.replaceAll(replacement);
    }
}

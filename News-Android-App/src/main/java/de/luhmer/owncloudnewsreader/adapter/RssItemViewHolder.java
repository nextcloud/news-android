package de.luhmer.owncloudnewsreader.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.regex.Pattern;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.ColorHelper;
import de.luhmer.owncloudnewsreader.helper.DateTimeFormatter;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;

public abstract class RssItemViewHolder<T extends ViewBinding> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    private final static String TAG = RssItemViewHolder.class.getCanonicalName();

    protected T binding;
    private static final SparseIntArray downloadProgressList = new SparseIntArray();
    private static FavIconHandler favIconHandler = null;
    protected final SharedPreferences mPrefs;
    @SuppressWarnings("FieldCanBeLocal")
    private final int LengthBody = 400;
    private final ForegroundColorSpan bodyForegroundColor;
    private RecyclerItemClickListener clickListener;
    private RssItem rssItem;
    private boolean stayUnread = false;
    private boolean playing;
    private int starColor;
    private int inactiveStarColor;
    protected RequestManager mGlide;

    private final SparseIntArray initalFontSizes = new SparseIntArray();

    RssItemViewHolder(@NonNull ViewBinding binding, SharedPreferences sharedPreferences) {
        super(binding.getRoot());
        this.binding = (T) binding;
        this.mPrefs = sharedPreferences;

        Context context = itemView.getContext();
        bodyForegroundColor = new ForegroundColorSpan(ContextCompat.getColor(context, android.R.color.secondary_text_dark));

        mGlide = Glide.with(context);

        if (favIconHandler == null) {
            favIconHandler = new FavIconHandler(context);
        }

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        extractInitialFontSize(getTextViewBody());
        extractInitialFontSize(getTextViewTitle());
        extractInitialFontSize(getTextViewSummary());
        extractInitialFontSize(getTextViewBody());
        extractInitialFontSize(getTextViewItemDate());
    }

    private void extractInitialFontSize(TextView tv) {
        if (tv != null) {
            initalFontSizes.append(tv.getId(), Math.round(tv.getTextSize()));
        }
    }

    /**
     * Apply scaling factor to TextView font size, based on app font-size preference.
     *
     * @param tv            TextView object to be scaled
     * @param initialTvSize app layout definition default size of TextView element
     * @param halfScale     if set to true, will only apply half of the scaling factor
     */
    private void scaleTextSize(TextView tv, int initialTvSize, boolean halfScale, SharedPreferences mPrefs) {
        float scalingFactor = Float.parseFloat(mPrefs.getString(SettingsActivity.SP_FONT_SIZE, "1.0"));
        if (halfScale) {
            scalingFactor = scalingFactor + (1 - scalingFactor) / 2;
        }

        if (initialTvSize < 0) {
            initialTvSize = Math.round(tv.getTextSize());
        }
        // float sp = initialSize / tv.getContext().getResources().getDisplayMetrics().scaledDensity;  // transform scaled pixels, device pixels
        int newSize = Math.round(initialTvSize * scalingFactor);

        // String name = tv.getResources().getResourceEntryName(tv.getId());
        // Log.d(TAG, name + " scale textsize from " + initialTvSize + " to " + newSize);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
    }

    /**
     * Return the number of rss item body text lines, depending on the currently selected font size/scale;
     * only meant to be used with thumbnail feed view.
     *
     * @return number of lines of rss item body text lines to be used in thumbnail feed view
     */
    private static int scaleTextLines(SharedPreferences prefs) {
        float scalingFactor = Float.parseFloat(prefs.getString(SettingsActivity.SP_FONT_SIZE, "1.0"));
        /* The following formula computes the number of text lines for Simple item view; it simply boils
         * down to a linear conversion from the font scaling factor from 0.8 -> 6 lines to 1.6 -> 3 lines
         */
        return Math.round((scalingFactor * -5) + 10);
    }

    abstract protected ImageView getImageViewFavIcon();

    abstract protected ImageView getStar();

    abstract protected ImageView getPlayPausePodcastButton();

    abstract protected View getColorFeed();

    abstract protected TextView getTextViewTitle();

    abstract protected TextView getTextViewSummary();

    abstract protected TextView getTextViewBody();

    abstract protected TextView getTextViewItemDate();

    abstract protected FrameLayout getPlayPausePodcastWrapper();

    abstract protected ProgressBar getPodcastDownloadProgress();

    @CallSuper
    public void bind(@NonNull RssItem rssItem) {
        this.rssItem = rssItem;

        if(getStar() != null) {
            int[] attribute = new int[]{R.attr.starredColor, R.attr.unstarredColor};
            TypedArray array = getStar().getContext().getTheme().obtainStyledAttributes(attribute);
            starColor = array.getColor(0, Color.TRANSPARENT);
            inactiveStarColor = array.getColor(1, Color.LTGRAY);
            array.recycle();
        }

        TextView textViewBody = getTextViewBody();

        String title = null;
        String favIconUrl = null;
        if (rssItem.getFeed() != null) {
            title = rssItem.getFeed().getFeedTitle();
            favIconUrl = rssItem.getFeed().getFaviconUrl();
        } else {
            Log.v(TAG, "Feed not found!!!");
        }

        setReadState(rssItem.getRead_temp());
        setStarred(rssItem.getStarred_temp());

        setFeedColor(ColorHelper.getFeedColor(itemView.getContext(), rssItem.getFeed()));

        TextView textViewSummary = getTextViewSummary();
        if (textViewSummary != null) {
            try {
                int textSizeSummary = initalFontSizes.get(getTextViewSummary().getId());
                textViewSummary.setText(Html.fromHtml(rssItem.getTitle()));
                scaleTextSize(textViewSummary, textSizeSummary, false, mPrefs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TextView textViewTitle = getTextViewTitle();
        TextView textViewItemDate = getTextViewItemDate();
        int sizeOfFavIcon = 32;
        int marginFavIcon = 0;
        if (textViewTitle != null && title != null) {
            if(textViewItemDate != null) {
                // we have seperate views for title and date
                textViewTitle.setText(Html.fromHtml(title));
            } else {
                // append date to title
                textViewTitle.setText(String.format("%s · %s", Html.fromHtml(title), DateTimeFormatter.getTimeAgo(rssItem.getPubDate())));
            }

            int textSizeTitle = initalFontSizes.get(textViewTitle.getId());
            scaleTextSize(textViewTitle, textSizeTitle, true, mPrefs);

            sizeOfFavIcon = textSizeTitle;
            marginFavIcon = Math.round(textViewTitle.getTextSize());
        }


        if (textViewItemDate != null) {
            int textSizeItemDate = initalFontSizes.get(getTextViewItemDate().getId());
            //textViewItemDate.setText(DateUtils.getRelativeTimeSpanString(rssItem.getPubDate().getTime()));
            textViewItemDate.setText(DateTimeFormatter.getTimeAgo(rssItem.getPubDate()));
            scaleTextSize(textViewItemDate, textSizeItemDate, true, mPrefs);

            sizeOfFavIcon = textSizeItemDate;
            marginFavIcon = Math.round(textViewItemDate.getTextSize());
        }



        ImageView imgViewFavIcon = getImageViewFavIcon();
        if (imgViewFavIcon != null) {
            favIconHandler.loadFavIconForFeed(favIconUrl, imgViewFavIcon, Math.round((marginFavIcon - sizeOfFavIcon) / 2f));
        }

        if (textViewBody != null) {
            int textSizeBody = initalFontSizes.get(textViewBody.getId());

            String body = rssItem.getMediaDescription();
            if (body == null || body.isEmpty()) {
                body = rssItem.getBody();
            }

            boolean limitLength = true;
            // Strip html from String
            if (this instanceof RssItemFullTextViewHolder) {
                textViewBody.setMaxLines(200);
                limitLength = false;
            } else if (this instanceof RssItemTextViewHolder) {
                textViewBody.setMaxLines(scaleTextLines(mPrefs));
                limitLength = false;
            }

            // long startTime = System.nanoTime();
            body = getBodyText(body, limitLength); // This is a bottleneck
            // long difference = System.nanoTime() - startTime;
            // Log.d(TAG, "Duration: " + difference / 1000 / 1000 + "ms");

            textViewBody.setText(Html.fromHtml(body));
            scaleTextSize(textViewBody, textSizeBody, false, mPrefs);
        }
    }

    @Override
    public void onClick(View v) {
        clickListener.onClick(this, getLayoutPosition());
    }

    public void setClickListener(RecyclerItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public boolean onLongClick(View v) {
        return clickListener.onLongClick(this, getLayoutPosition());
    }

    public void setStarred(boolean isStarred) {
        int color = isStarred ? starColor : inactiveStarColor;
        int contentDescriptionId = isStarred ?
                R.string.content_desc_remove_from_favorites :
                R.string.content_desc_add_to_favorites;
        ImageView star = getStar();
        if(star != null) {
            star.setColorFilter(color);
            star.setContentDescription(star.getContext().getString(contentDescriptionId));
        }
    }

    public RssItem getRssItem() {
        return rssItem;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldStayUnread() {
        return stayUnread;
    }

    public void setStayUnread(boolean shouldStayUnread) {
        this.stayUnread = shouldStayUnread;
    }

    private String getBodyText(String body, boolean limitLength) {
        if (body.startsWith("<![CDATA[")) {
            body = body.replaceFirst(Pattern.quote("<![CDATA["), "");
            body = body.replaceFirst("]]>", "");
        }

        body = body.replaceAll("<img[^>]*>", "");
        body = body.replaceAll("<video[^>]*>", "");

        SpannableString bodyStringSpannable = new SpannableString(Html.fromHtml(body));
        bodyStringSpannable.setSpan(bodyForegroundColor, 0, bodyStringSpannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        String bodyString = bodyStringSpannable.toString().trim();


        if (limitLength && bodyString.length() > LengthBody) {
            bodyString = bodyString.substring(0, LengthBody) + "...";
        }

        return bodyString;
    }

    private void setFeedColor(int color) {
        if (getColorFeed() != null) {
            getColorFeed().setBackgroundColor(color);
        }
    }

    public void setReadState(boolean isRead) {
        TextView textViewSummary = getTextViewSummary();
        if (textViewSummary != null) {
            float alpha = 1f;
            if (isRead) {
                textViewSummary.setTypeface(Typeface.DEFAULT);
                alpha = 0.7f;
            } else {
                textViewSummary.setTypeface(Typeface.DEFAULT_BOLD);
            }

            ((View) textViewSummary.getParent()).setAlpha(alpha);
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;

        int imageId = playing ? R.drawable.ic_action_pause_24 : R.drawable.ic_baseline_play_arrow_24;
        int contentDescriptionId = playing ? R.string.content_desc_pause : R.string.content_desc_play;

        ImageView playPause = getPlayPausePodcastButton();
        String contentDescription = playPause.getContext().getString(contentDescriptionId);
        playPause.setContentDescription(contentDescription);
        playPause.setImageResource(imageId);
    }

    public void setDownloadPodcastProgressbar() {
        float progress;
        if (PodcastDownloadService.PodcastAlreadyCached(itemView.getContext(), rssItem.getEnclosureLink())) {
            progress = 100;
        } else {
            progress = downloadProgressList.get(rssItem.getId().intValue(), 0);
        }
        getPodcastDownloadProgress().setProgress((int) progress);
        Log.v(TAG, "Progress of download2: " + progress);
    }

    @Subscribe
    public void onEvent(PodcastDownloadService.DownloadProgressUpdate downloadProgress) {
        downloadProgressList.put((int) downloadProgress.podcast.itemId, downloadProgress.podcast.downloadProgress);
        if (rssItem.getId().equals(downloadProgress.podcast.itemId)) {
            getPodcastDownloadProgress().setProgress(downloadProgress.podcast.downloadProgress);

            Log.v(TAG, "Progress of download1: " + downloadProgress.podcast.downloadProgress);
        }
    }
}
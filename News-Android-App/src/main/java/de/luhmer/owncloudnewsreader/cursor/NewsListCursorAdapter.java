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

package de.luhmer.owncloudnewsreader.cursor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoCheckBox;
import com.devspark.robototextview.widget.RobotoTextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.async_tasks.IGetTextForTextViewAsyncTask;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.FillTextForTextViewHelper;
import de.luhmer.owncloudnewsreader.helper.FontHelper;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;

@Deprecated
public class NewsListCursorAdapter extends CursorAdapter {
	//private static final String TAG = "NewsListCursorAdapter";
	DatabaseConnection dbConn;
	IReader _Reader;
    //SimpleDateFormat simpleDateFormat;
    final int LengthBody = 400;
    ForegroundColorSpan bodyForegroundColor;
    IOnStayUnread onStayUnread;

    PostDelayHandler pDelayHandler;

    int selectedDesign = 0;

	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	public NewsListCursorAdapter(Context context, Cursor c, IOnStayUnread onStayUnread) {
		super(context, c);

		this.onStayUnread = onStayUnread;

		pDelayHandler = new PostDelayHandler(context);

        //simpleDateFormat = new SimpleDateFormat("EEE, d. MMM HH:mm:ss");
        bodyForegroundColor = new ForegroundColorSpan(context.getResources().getColor(android.R.color.secondary_text_dark));

        _Reader = new OwnCloud_Reader();
		dbConn = new DatabaseConnection(context);

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		selectedDesign = Integer.valueOf(mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0"));
	}

	@Override
	public void bindView(final View view, final Context context, Cursor cursor) {
        final String idItemDb = cursor.getString(0);

        switch (selectedDesign) {
			case 0:
				setSimpleLayout(view, cursor);
				break;

			case 1:
				setExtendedLayout(view, cursor);
				break;

			case 2:
				setExtendedLayoutWebView(view, cursor);
				break;

			default:
				break;
	    }

        FontHelper fHelper = new FontHelper(context);
        fHelper.setFontForAllChildren(view, fHelper.getFont());

        RobotoCheckBox cbStarred = (RobotoCheckBox) view.findViewById(R.id.cb_lv_item_starred);
        if(ThemeChooser.isDarkTheme(mContext))
            cbStarred.setBackgroundResource(R.drawable.checkbox_background_holo_dark);
        /*
        //The default is white so we don't need to set it here again..
        else
            cbStarred.setBackgroundResource(R.drawable.checkbox_background_holo_light);*/

        cbStarred.setOnCheckedChangeListener(null);

        Boolean isStarred = dbConn.isFeedUnreadStarred(cursor.getString(0), false);//false => starred will be checked
        //Log.d("ISSTARRED", "" + isStarred + " - Cursor: " + cursor.getString(0));
        cbStarred.setChecked(isStarred);
        cbStarred.setClickable(true);
        cbStarred.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dbConn.updateIsStarredOfItem(idItemDb, isChecked);

                if(isChecked)
                	UpdateIsReadCheckBox(buttonView, idItemDb);

                pDelayHandler.DelayTimer();
			}
		});

        LinearLayout ll_cb_starred_wrapper = (LinearLayout) view.findViewById(R.id.ll_cb_starred_wrapper);
        if(ll_cb_starred_wrapper != null) {
            ll_cb_starred_wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RobotoCheckBox cbStarred = (RobotoCheckBox) view.findViewById(R.id.cb_lv_item_starred);
                    cbStarred.setChecked(!cbStarred.isChecked());
                }
            });
        }

        RobotoCheckBox cbRead = (RobotoCheckBox) view.findViewById(R.id.cb_lv_item_read);
        cbRead.setTag(idItemDb);
        cbRead.setOnCheckedChangeListener(null);
        Boolean isChecked = dbConn.isFeedUnreadStarred(cursor.getString(0), true);
        //Log.d("ISREAD", "" + isChecked + " - Cursor: " + cursor.getString(0));
        cbRead.setChecked(isChecked);
        if(!isChecked) {
        	RobotoTextView textView = (RobotoTextView) view.findViewById(R.id.summary);
        	fHelper.setFontStyleForSingleView(textView, fHelper.getFontUnreadStyle());
        }


        cbRead.setClickable(true);
        cbRead.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ChangeReadStateOfItem((RobotoCheckBox) buttonView, view, isChecked, context);
			}
		});


        String colorString = dbConn.getAvgColourOfFeedByDbId(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID)));
        View viewColor = view.findViewById(R.id.color_line_feed);
        if(colorString != null)
        	viewColor.setBackgroundColor(Integer.parseInt(colorString));
	}

    public void ChangeReadStateOfItem(RobotoCheckBox checkBox, View parentView, boolean isChecked, Context context) {

        dbConn.updateIsReadOfItem(checkBox.getTag().toString(), isChecked);

        UpdateListCursor(mContext);

        pDelayHandler.DelayTimer();

        RobotoTextView textView = (RobotoTextView) parentView.findViewById(R.id.summary);
        if(textView != null && parentView.getTop() >= 0)
        {
            FontHelper fHelper = new FontHelper(context);
            if(isChecked)
                fHelper.setFontStyleForSingleView(textView, fHelper.getFont());
                //textView.setTextAppearance(mContext, R.style.RobotoFontStyle);
            else {
                fHelper.setFontStyleForSingleView(textView, fHelper.getFontUnreadStyle());
                onStayUnread.stayUnread(checkBox);
            }
            //textView.setTextAppearance(mContext, R.style.RobotoFontStyleBold);

            textView.invalidate();
        }
    }

	public void setSimpleLayout(View view, Cursor cursor)
	{
        SimpleLayout simpleLayout = new SimpleLayout(view);

        simpleLayout.textViewSummary.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE))).toString());

        long pubDate = cursor.getLong(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_PUBDATE));
        String dateString = (String) DateUtils.getRelativeTimeSpanString(pubDate);
        simpleLayout.textViewItemDate.setText(dateString);

        simpleLayout.textViewTitle.setText(dbConn.getTitleOfSubscriptionByRowID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID))));
        simpleLayout.textViewSummary.setTag(cursor.getString(0));

        if(!ThemeChooser.isDarkTheme(mContext)) {
            simpleLayout.viewDivider.setBackgroundColor(mContext.getResources().getColor(R.color.divider_row_color_light_theme));
        }
	}

    static class SimpleLayout {
        @InjectView(R.id.divider) View viewDivider;
        @InjectView(R.id.summary) TextView textViewSummary;
        @InjectView(R.id.tv_item_date) TextView textViewItemDate;
        @InjectView(R.id.tv_subscription) TextView textViewTitle;

        SimpleLayout(View view) {
            ButterKnife.inject(this, view);
        }
    }

	public void setExtendedLayout(View view, Cursor cursor)
	{
        ExtendedLayout extendedLayout = new ExtendedLayout(view);

        extendedLayout.textViewSummary.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE))).toString());

        long pubDate = cursor.getLong(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_PUBDATE));
        //textViewItemDate.setText(simpleDateFormat.format(new Date(pubDate)));
        String dateString = (String) DateUtils.getRelativeTimeSpanString(pubDate);
        extendedLayout.textViewItemDate.setText(dateString);

        extendedLayout.textViewItemBody.setVisibility(View.INVISIBLE);
        String idItemDb = cursor.getString(0);
        IGetTextForTextViewAsyncTask iGetter = new DescriptionTextGetter(idItemDb);
        FillTextForTextViewHelper.FillTextForTextView(extendedLayout.textViewItemBody, iGetter, true);

        extendedLayout.textViewTitle.setText(dbConn.getTitleOfSubscriptionByRowID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID))));
        extendedLayout.textViewSummary.setTag(cursor.getString(0));

        if(!ThemeChooser.isDarkTheme(mContext)) {
            extendedLayout.textViewItemBody.setTextColor(mContext.getResources().getColor(R.color.extended_listview_item_body_text_color_light_theme));
            extendedLayout.viewDivider.setBackgroundColor(mContext.getResources().getColor(R.color.divider_row_color_light_theme));
        }
	}

    static class ExtendedLayout {
        @InjectView(R.id.divider) View viewDivider;
        @InjectView(R.id.summary) TextView textViewSummary;
        @InjectView(R.id.tv_item_date) TextView textViewItemDate;
        @InjectView(R.id.body) TextView textViewItemBody;
        @InjectView(R.id.tv_subscription) TextView textViewTitle;

        ExtendedLayout(View view) {
            ButterKnife.inject(this, view);
        }
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setExtendedLayoutWebView(View view, Cursor cursor)
	{
        WebView webViewContent = (WebView) view.findViewById(R.id.webView_body);
        webViewContent.setClickable(false);
        webViewContent.setFocusable(false);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        //	webViewContent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //webViewContent.loadDataWithBaseURL("", NewsDetailFragment.getHtmlPage(mContext, dbConn , cursor.getInt(0)), "text/html", "UTF-8", ""); //This line is needed to run the adapter
	}


	public void CloseDatabaseConnection()
	{
		if(dbConn != null)
			dbConn.closeDatabase();
	}

	private void UpdateIsReadCheckBox(View view, String idItemDb)
	{
		LinearLayout lLayout = (LinearLayout) view.getParent();
		Boolean isChecked = dbConn.isFeedUnreadStarred(idItemDb, true);
		RobotoCheckBox cbRead = (RobotoCheckBox) lLayout.findViewById(R.id.cb_lv_item_read);
        if(cbRead == null) {//In the default layout the star checkbox is nested two times.
            lLayout = (LinearLayout) lLayout.getParent();
            cbRead = (RobotoCheckBox) lLayout.findViewById(R.id.cb_lv_item_read);
        }
        cbRead.setChecked(isChecked);
	}

	public static void ChangeCheckBoxState(RobotoCheckBox cb, boolean state, Context context)
	{
		if(cb != null && cb.isChecked() != state)
            cb.setChecked(state);
	}

	public static void UpdateListCursor(Context context)
	{
		FragmentActivity sfa = (FragmentActivity) context;

		if(sfa instanceof NewsReaderListActivity && ((NewsReaderListActivity) sfa).isSlidingPaneOpen())
			((NewsReaderListActivity) sfa).updateAdapter();
	}


    private String getBodyText(String body)
    {
        body = body.replaceAll("<img[^>]*>", "");
        body = body.replaceAll("<video[^>]*>", "");

        SpannableString bodyStringSpannable = new SpannableString(Html.fromHtml(body));
        bodyStringSpannable.setSpan(bodyForegroundColor, 0, bodyStringSpannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        String bodyString = bodyStringSpannable.toString().trim();

        if(bodyString.length() > LengthBody)
            bodyString = bodyString.substring(0, LengthBody);

        return bodyString;
    }

	@Override
	public View newView(Context cont, Cursor cursor, ViewGroup parent) {
		// when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = null;

        switch (selectedDesign) {
			case 0:
				retView = inflater.inflate(R.layout.subscription_detail_list_item_simple, parent, false);
				break;
			case 1:
				retView = inflater.inflate(R.layout.subscription_detail_list_item_extended, parent, false);
				break;
			case 2:
				retView = inflater.inflate(R.layout.subscription_detail_list_item_extended_webview, parent, false);
				break;
        }


        if(retView != null)
        	retView.setTag(cursor.getString(0));

        return retView;
	}






    class DescriptionTextGetter implements IGetTextForTextViewAsyncTask {

        private String idItemDb;

        public DescriptionTextGetter(String idItemDb) {
            this.idItemDb = idItemDb;
        }

        @Override
        public String getText() {
            DatabaseConnection dbConn = new DatabaseConnection(mContext);

            Cursor cursor = dbConn.getItemByDbID(idItemDb);
            cursor.moveToFirst();
            String body = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_BODY));
            String result = getBodyText(body);
            cursor.close();

            return  result;
        }
    }



    /*
    class DescriptionTextLoaderTask extends AsyncTask<Void, Void, String> {
        private String idItemDb;
        private final WeakReference<TextView> textViewWeakReference;

        public DescriptionTextLoaderTask(TextView textView, String idItemDb) {
            textViewWeakReference = new WeakReference<TextView>(textView);
            this.idItemDb = idItemDb;
        }

        @Override
        // Actual download method, run in the task thread
        protected String doInBackground(Void... params) {

            DatabaseConnection dbConn = new DatabaseConnection(mContext);

            Cursor cursor = dbConn.getItemByDbID(idItemDb);
            cursor.moveToFirst();
            String body = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_BODY));
            String result = getBodyText(body);
            cursor.close();

            return result;
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(String text) {
            if (isCancelled()) {
                text = null;
            }

            if (textViewWeakReference != null) {
                TextView textView = textViewWeakReference.get();
                if (textView != null) {
                    textView.setText(text);

                    FadeInTextView(textView);
                }
            }
        }
    }
    */

    public static void FadeInTextView(final TextView textView)
    {
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(300);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                textView.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        textView.startAnimation(fadeOut);
    }
}

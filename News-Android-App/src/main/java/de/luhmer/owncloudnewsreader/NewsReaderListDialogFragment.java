package de.luhmer.owncloudnewsreader;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import de.luhmer.owncloudnewsreader.helper.ThemeChooser;


/**
 * Created by benson on 27/11/15.
 */
public class NewsReaderListDialogFragment extends DialogFragment{

    static NewsReaderListDialogFragment newInstance(String dialogTitle, String iconurl, String feedurl) {
        NewsReaderListDialogFragment f = new NewsReaderListDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", dialogTitle);
        args.putString("iconurl", iconurl);
        args.putString("feedurl", feedurl);

        f.setArguments(args);
        return f;
    }


    private String mDialogTitle;
    private String mDialogText;
    private String mDialogIconUrl;
    private URL mImageUrl;
    private LinkedHashMap<String, MenuAction> mMenuItems;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDialogTitle = getArguments().getString("title");
        mDialogIconUrl = getArguments().getString("iconurl");
        mDialogText = getArguments().getString("feedurl");
        mMenuItems = new LinkedHashMap<>();

        mMenuItems.put("Rename feed"/*getString(R.string.action_img_download)*/, new MenuAction() {
            @Override
            public void execute() {
                renameFeed();
            }
        });

        mMenuItems.put("Remove feed"/*getString(R.string.action_img_download)*/, new MenuAction() {
            @Override
            public void execute() {
                removeFeed();
            }
        });

        int style = DialogFragment.STYLE_NO_TITLE;
        int theme = ThemeChooser.isDarkTheme(getActivity())
                ? R.style.Theme_Material_Dialog_Floating
                : R.style.Theme_Material_Light_Dialog_Floating;
        setStyle(style, theme);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dialog_image, container, false);

        TextView tvTitle = (TextView) v.findViewById(R.id.ic_menu_title);
        TextView tvText = (TextView) v.findViewById(R.id.ic_menu_item_text);
        ImageView imgTitle = (ImageView) v.findViewById(R.id.ic_menu_gallery);


        tvTitle.setText(mDialogTitle);
        tvText.setText(mDialogText);


        ViewGroup.LayoutParams params=((View)imgTitle).getLayoutParams();
        params.width=80;
        params.height=80;
        imgTitle.setLayoutParams(params);

        if(mDialogIconUrl != null) {
            DiskCache diskCache = ImageLoader.getInstance().getDiskCache();
            File file = diskCache.get(mDialogIconUrl);
            if (file != null) {
                mDialogIconUrl = file.getAbsolutePath();
                imgTitle.setImageDrawable(new BitmapDrawable(mDialogIconUrl));
            } else {
                imgTitle.setImageResource(R.drawable.default_feed_icon_light);
            }
        } else {
            imgTitle.setImageResource(R.drawable.default_feed_icon_light);
        }

        ListView mListView = (ListView) v.findViewById(R.id.ic_menu_item_list);
        List<String> menuItemsList = new ArrayList<>(mMenuItems.keySet());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.fragment_dialog_listviewitem,
                menuItemsList);

        mListView.setAdapter(arrayAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String key = arrayAdapter.getItem(i);
                MenuAction mAction = mMenuItems.get(key);
                mAction.execute();
            }
        });

        return v;
    }




    private void renameFeed() {
        System.out.println("************** renameFeed");
    }

    private void removeFeed() {
        System.out.println("************** removeFeed");
    }

    interface MenuAction {
        void execute();
    }
}

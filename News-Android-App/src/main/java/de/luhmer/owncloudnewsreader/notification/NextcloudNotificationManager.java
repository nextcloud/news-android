package de.luhmer.owncloudnewsreader.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;

import de.luhmer.owncloudnewsreader.BuildConfig;
import de.luhmer.owncloudnewsreader.R;

public class NextcloudNotificationManager {

    private static final int ID_DownloadSingleImageComplete = 10;

    public static void ShowNotificationDownloadSingleImageComplete(Context context, File imagePath) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelDownloadImage = context.getString(R.string.action_img_download);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelDownloadImage, channelDownloadImage, importance);
            notificationManager.createNotificationChannel(channel);
        }


        // Load image, decode it to Bitmap and return Bitmap to callback
        ImageSize targetSize = new ImageSize(1024,512); // result Bitmap will be fit to this size
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync("file://" + imagePath.getAbsolutePath(), targetSize);

        // Uri imageUri = Uri.parse(imagePath);
        Uri imageUri = FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider",
                imagePath);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(imageUri, "image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelDownloadImage)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.toast_img_saved))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));

        notificationManager.notify(ID_DownloadSingleImageComplete, mBuilder.build());
    }

}

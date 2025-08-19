package ryo_original_app.musicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import ryo_original_app.musicplayer.R;
import ryo_original_app.musicplayer.constants.Constants;

/**
 * 通知作成サービス
 */
public class MediaPlaybackService extends Service {
    /** 通知用ID（Int型なので1001にして、加算できるよう調整） */
    public static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * 通知チャンネルを作成（Android 8.0以上）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.notificationId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    /**
     * 通知の作成
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /* Activityからの情報取得 */
        String title = intent.getStringExtra("title");
        String artist = intent.getStringExtra("artist");
        String artPath = intent.getStringExtra("artPath");
        Bitmap artBitmap = null;

        /* 一時保管しているアートファイルを参照する */
        if (artPath != null) {
            artBitmap = BitmapFactory.decodeFile(artPath);
        }

        /* 通知の作成 */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.notificationId)
                .setSmallIcon(android.R.drawable.ic_media_play) // 左上表示用のアイコン
                .setContentTitle(title != null ? title : "Unknown") // タイトル
                .setContentText(artist != null ? artist : "")   // アーティスト名
                .setOngoing(true)   // 常駐通知設定
                ;

        if (artBitmap != null) {
            builder.setLargeIcon(artBitmap);    // アートファイル
        }

        Notification notification = builder.build();

        /* サービス開始 */
        startForeground(NOTIFICATION_ID, notification);

        /* 逐一通知を更新する */
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
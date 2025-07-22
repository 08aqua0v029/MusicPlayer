package ryo_original_app.musicplayer;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;

/**
 * クラッシュ時のカスタムハンドラ
 * ログ出力を行う
 */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler defaultHandler;

    /**
     * ハンドラの保持
     * @Return なし
     */
    public CustomExceptionHandler() {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * クラッシュ情報のログ出力
     * @Return なし
     */
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        String currentScreen = ScreenTracker.getCurrentScreen();    // クラッシュ前に保存された画面名を取得
        Log.e("クラッシュハンドラ", "Uncaught Exception:"+ e);
        Log.e("クラッシュした画面", "App crashed on screen: " + currentScreen);
        Log.i("クラッシュ時刻", "Crashed time: " + LocalDateTime.now());
        Log.i("機種情報", "Build.MODEL: " + Build.MODEL);
        Log.i("OS情報", "Build.VERSION.BASE_OS: Android" + Build.VERSION.RELEASE);

        // 最後にデフォルトのハンドラを呼び出す（アプリを終了させるため）
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e);
        }
    }
}

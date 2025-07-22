package ryo_original_app.musicplayer.log;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;

import ryo_original_app.musicplayer.constants.Constants;

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
        Log.e(Constants.crashHandlerLogTag, Constants.crashHandlerLogMsg + e);
        Log.e(Constants.crashScreenLogTag, Constants.crashScreenLogMsg + currentScreen);
        Log.i(Constants.crashTimeLogTag, Constants.crashTimeLogMsg + LocalDateTime.now());
        Log.i(Constants.modelLogTag, Constants.modelLogMsg + Build.MODEL);
        Log.i(Constants.osVerLogTag, Constants.osVerLogMsg+ Build.VERSION.RELEASE);

        // 最後にデフォルトのハンドラを呼び出す（アプリを終了させるため）
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e);
        }
    }
}

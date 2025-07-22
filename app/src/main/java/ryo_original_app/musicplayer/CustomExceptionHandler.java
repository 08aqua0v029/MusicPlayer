package ryo_original_app.musicplayer;

import android.util.Log;

import androidx.annotation.NonNull;

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
        Log.e("CrashHandler", "Uncaught Exception:"+ e);

        // 最後にデフォルトのハンドラを呼び出す（アプリを終了させるため）
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e);
        }
    }
}

package ryo_original_app.musicplayer;

import android.util.Log;

import androidx.annotation.NonNull;

/*
* クラッシュ時のカスタムハンドラ
* ログ出力を行う
**/
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler defaultHandler;

    public CustomExceptionHandler() {
        // ハンドラの保持
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        // クラッシュ情報のログ出力
        Log.e("CrashHandler", "Uncaught Exception:"+ e);

        // 最後にデフォルトのハンドラを呼び出す（アプリを終了させるため）
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e);
        }
    }
}

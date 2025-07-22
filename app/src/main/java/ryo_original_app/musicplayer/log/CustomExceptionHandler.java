package ryo_original_app.musicplayer.log;

import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
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
     */
    public CustomExceptionHandler() {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * クラッシュ情報のログ出力
     */
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        String currentScreen = ScreenTracker.getCurrentScreen();    // クラッシュ前に保存された画面名を取得

        // エラー詳細抽出
        Throwable rootCause = getCause(e);
        String errorType = rootCause.getClass().getName();
        String message = rootCause.getMessage();
        String location = rootCause.getStackTrace().length > 0 ? rootCause.getStackTrace()[0].toString() : "検出できず";

        Log.e(Constants.crashHandler, Constants.errorTypeLogTag + errorType);               // エラー内容
        Log.e(Constants.crashHandler, Constants.errorDetailsLogTag + message);              // エラー詳細
        Log.e(Constants.crashHandler, Constants.crashLocationLogTag + location);            // クラッシュ箇所

        Log.i(Constants.crashHandler, Constants.crashTimeLogMsg + LocalDateTime.now());     // クラッシュ時刻
        Log.i(Constants.crashHandler, Constants.modelLogMsg + Build.MODEL);                 // クラッシュ端末
        Log.i(Constants.crashHandler, Constants.osVerLogMsg+ Build.VERSION.RELEASE);        // クラッシュOSバージョン

        // 最後にデフォルトのハンドラを呼び出す（アプリを終了させるため）
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e);
        }
    }

    /**
     * Caused byの最深部（最原因になりうる箇所）の情報を抽出する
     * @return Caused byの最重要情報
     */
    private Throwable getCause(Throwable throwable) {
        Throwable cause = throwable;

        /* Caused byを繰り返し抽出し、最後のCaused byをreturnする */
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}



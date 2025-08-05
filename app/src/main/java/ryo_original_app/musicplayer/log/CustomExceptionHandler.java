package ryo_original_app.musicplayer.log;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import ryo_original_app.musicplayer.constants.Constants;

/**
 * クラッシュ時のカスタムハンドラ
 * ログ出力を行う
 */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;

    /**
     * ハンドラの保持
     */
    public CustomExceptionHandler(Context context) {
        this.context = context;
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
        String errorType = rootCause.getClass().getName();  // エラー内容
        String message = rootCause.getMessage();    // エラー詳細
        // クラッシュ箇所（getCauseにてクラッシュ箇所が何らかの理由で0だった場合は検出できない旨を三項演算子で表現）
        String location = rootCause.getStackTrace().length > 0 ? rootCause.getStackTrace()[0].toString() : "検出できず";

        /* クラッシュ時刻を整形 */
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.formatDateTime);
        String formatCrashDateTime = formatter.format(LocalDateTime.now());

        /* クラッシュ詳細をログ出力 */
        Log.e(Constants.crashHandler, Constants.errorTypeLogKey + Constants.colonString + errorType);               // エラー内容
        Log.e(Constants.crashHandler, Constants.errorDetailsLogKey + Constants.colonString + message);              // エラー詳細
        Log.e(Constants.crashHandler, Constants.crashLocationLogKey + Constants.colonString + location);            // クラッシュ箇所
        Log.i(Constants.crashHandler, Constants.crashTimeLogKey + Constants.colonString + formatCrashDateTime);     // クラッシュ時刻
        Log.i(Constants.crashHandler, Constants.modelLogKey + Constants.colonString + Build.MODEL);                 // クラッシュ端末
        Log.i(Constants.crashHandler, Constants.osVerLogKey + Constants.colonString + Build.VERSION.RELEASE);        // クラッシュOSバージョン

        /* ログデータをJSON化 */
        try {
            JSONObject logJson = new JSONObject();
            logJson.put(Constants.errorTypeLogKey, errorType);
            logJson.put(Constants.errorDetailsLogKey, message);
            logJson.put(Constants.crashLocationLogKey, location);
            logJson.put(Constants.crashTimeLogKey, formatCrashDateTime);
            logJson.put(Constants.modelLogKey, Build.MODEL);
            logJson.put(Constants.osVerLogKey, "Android:" + Build.VERSION.RELEASE);

            /* クラッシュログの可視化（Logcat） */
            Log.e(Constants.crashLogFile, logJson.toString(4));

            /* ログファイルをローカルに保管するためにフォルダを作成する */
            File logDir = new File(context.getFilesDir(), Constants.logFolder);
            if (!logDir.exists()) logDir.mkdirs();

            /* ログファイルをローカルに保管する */
            File crashLog = new File(logDir, Constants.crashLogFile);
            FileOutputStream outputFile = new FileOutputStream(crashLog);
            outputFile.write(logJson.toString().getBytes());

        } catch (JSONException | IOException ex) {
            throw new RuntimeException(ex);
        }

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



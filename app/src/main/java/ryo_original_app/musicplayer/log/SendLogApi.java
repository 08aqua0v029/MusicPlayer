package ryo_original_app.musicplayer.log;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import ryo_original_app.musicplayer.constants.Constants;

public class SendLogApi {
    /**
     * JSONログをサーバーへ送信
     * @param context アプリの情報
     */
    public static void sendJsonLog(Context context, String apiUri) {
        /* ファイルの準備 */
        File inputFile = new File(context.getFilesDir(), Constants.logFolder + Constants.slashString + Constants.logFile);

        /* JSONファイルの存在有無チェック */
        if (inputFile.exists()){
            /* 非同期処理開始 */
            new Thread(() -> {
                /* HttpURLConnectionを使用したJSONファイル送信処理 */
                try {
                    /* JSONファイルをString化 */
                    String json = new String(Files.readAllBytes(inputFile.toPath()), StandardCharsets.UTF_8);

                    /* サーバーへ送信作業 */
                    URL url = new URL(apiUri);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setDoOutput(true);   // 送信の許可

                    /* Basic認証設定 */
                    final String userPass = Constants.basicUser + ":" + Constants.basicPass;    // ユーザー:パス形式の設定
                    String encodeAuthorization = Base64.getEncoder().encodeToString(userPass.getBytes());   // ユーザー:パスをBASE64形式に変換
                    connection.setRequestProperty("Authorization", "Basic " + encodeAuthorization);     // Basic認証設定をヘッダにセット

                    /* 送信したいデータ（バイト配列）を送信 */
                    OutputStream os = connection.getOutputStream();
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                    os.close();

                    /* レスポンス情報取得 */
                    int responseCode = connection.getResponseCode();
                    String contentType = connection.getHeaderField("Content-Type");
                    String serverStatus = connection.getHeaderField("Status");

                    /* レスポンスが200番台だったら成功とし、ファイルを削除 */
                    if (responseCode >= 200 && responseCode < 300) {
                        inputFile.delete();  // 削除
                        Log.d("SUCCESS", "response:" + String.valueOf(responseCode));
                        Log.d("SUCCESS", "contentType:" + contentType);
                        Log.d("SUCCESS", "serverStatus:" + serverStatus);
                    } else {
                        Log.e("ERROR", "response:" + String.valueOf(responseCode));
                        Log.e("ERROR", "contentType:" + contentType);
                        Log.e("ERROR", "serverStatus:" + serverStatus);
                    }
                } catch (Exception e) {
                    Log.e("ERROR", "サーバー停止などの理由で転送不可", e);
                }
            }).start();
        }else{
            Log.d("info", "ログファイルが存在しません");
        }
    }
}

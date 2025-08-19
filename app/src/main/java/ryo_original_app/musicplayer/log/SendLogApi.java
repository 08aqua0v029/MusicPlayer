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
     * 汎用ログ送信クラス
     * @param context アプリの情報
     * @param apiUri ログの送信先
     * @param basicUser Basic認証User名
     * @param basicPass Basic認証Pass
     */
    public static void sendJsonLog(Context context, String apiUri, String basicUser, String basicPass) {
        /* ファイルの準備 */
        File inputFile = new File(context.getFilesDir(), Constants.logFolder + Constants.slashString + Constants.crashLogFile);

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
                    /* TODO:一定の機能実装後、Tokenや署名などの認証方法も試してみる */
                    final String userPass = basicUser + ":" + basicPass;    // ユーザー:パス形式の設定
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
                        Log.d(Constants.successTag,
                                Constants.responseString + Constants.colonString + String.valueOf(responseCode));
                        Log.d(Constants.successTag,
                                Constants.contentTypeString + Constants.colonString + contentType);
                        Log.d(Constants.successTag,
                                Constants.serverStatusString + Constants.colonString + serverStatus);
                    } else {
                        Log.e(Constants.errorTag,
                                Constants.responseString + Constants.colonString + String.valueOf(responseCode));
                        Log.e(Constants.errorTag,
                                Constants.contentTypeString + Constants.colonString + contentType);
                        Log.e(Constants.errorTag,
                                Constants.serverStatusString + Constants.colonString + serverStatus);
                    }
                } catch (Exception e) {
                    Log.e(Constants.errorTag, Constants.severErrorSentence, e);
                }
            }).start();
        }else{
            Log.d(Constants.infoTag, Constants.nonLogFile);
        }
    }
}

package ryo_original_app.musicplayer.log;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import ryo_original_app.musicplayer.constants.Constants;

public class SendLogApi {
    /**
     * JSONログをサーバーへ送信
     * @param context アプリの情報
     */
    public static void sendJsonFile(Context context) {
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
                    URL url = new URL(Constants.localApiUri);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setDoOutput(true);   // 送信の許可

                    /* 送信したいデータ（バイト配列）を送信 */
                    OutputStream os = connection.getOutputStream();
                    os.write(json.getBytes(StandardCharsets.UTF_8));

                    int responseCode = connection.getResponseCode();
                    /* レスポンスが200番台だったら成功とし、ファイルを削除 */
                    if (responseCode >= 200 && responseCode < 300) {
                        inputFile.delete();  // 削除
                    } else {
                        Log.e("WARNING!!","送信エラー");
                    }
                    Log.d("SUCCESS", "サーバーへの転送完了");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }else{
            Log.d("info", "ログファイルが存在しません");
        }
    }
}

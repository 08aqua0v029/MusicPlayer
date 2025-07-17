package ryo_original_app.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /* UI関係 */
    private ImageButton _btPlay;            // 再生・停止ボタン
    private ImageButton _btBack;            // 戻るボタン
    private ImageButton _btNext;            // 次へボタン
    private ImageView _artFile;             // ジャケットファイル
    private TextView _tuneTitle;            // 楽曲タイトル
    private TextView _tuneTotalTime;        // 1楽曲の総時間
    private TextView _tuneNowTime;          // 1楽曲の今の再生時間

    /* 音楽ファイル関係 */
    private File fileDir;                   // 楽曲ディレクトリ
    private File tunesList[];                // 楽曲一覧
    private int totalTunesNum = 0;           // 総楽曲数
    private int nowTuneNum = 0;             // 楽曲番号

    /* 正規表現用String */
    private String mp3String = ".mp3";
    private String wavString = ".wav";
    private String m4aString = ".m4a";

    /*
     * 再生か一時停止化のフラグ
     * 0:再生
     * 1:一時停止
     **/
    private int btPlayFlag = 0;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());    // カスタムクラッシュハンドラを設定

        /* スプラッシュのための中断処理 */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            super.onDestroy();
        }

        /* 権限を得る処理 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        /* 以下メイン画面描画用処理 */
        setTheme(R.style.Base_Theme_MusicPlayer);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        /* ステータスバー削除処理 */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* アートファイル定義 */
        _artFile = findViewById(R.id.artFile);
        _artFile.setImageResource(R.drawable.test_art);

        /* 各種ボタン定義 */
        _btPlay = findViewById(R.id.btPlay);
        _btBack = findViewById(R.id.btBack);
        _btNext = findViewById(R.id.btNext);

    }

    /* requestPermissionsのコールバック */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "権限が許可されました", Toast.LENGTH_SHORT).show();
            tunesList = createTunesList();
        }  else {
            Toast.makeText(this, "音楽とオーディオの権限を許可してください", Toast.LENGTH_SHORT).show();
            String uriString = "package:" + getPackageName();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
            startActivity(intent);
        }
    }

    /* 再生・停止ボタン押下処理 */
    public void onPlayButton(View v) {
        if(btPlayFlag == 0) {
            _btPlay.setImageResource(R.drawable.stop);
            btPlayFlag = 1;
        } else if(btPlayFlag == 1) {
            _btPlay.setImageResource(R.drawable.start);
            btPlayFlag = 0;
        }
    }

    /* 戻るボタン押下処理 */
    public void onBackButton(View v) {
        /* TODO:実装後削除 */
        System.out.println("modoru");
    }

    /* 次ボタン押下処理 */
    public void onNextButton(View v) {
        /* TODO:実装後削除 */
        System.out.println("tsugihe");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btPlay){
            onPlayButton(v);
        } else if(v.getId() == R.id.btBack) {
            onBackButton(v);
        } else if(v.getId() == R.id.btNext) {
            onNextButton(v);
        }
    }

    /* 楽曲データ取得 */
    private File[] createTunesList() {
        String path = Environment.getExternalStorageDirectory().getPath();  // パス生成
        fileDir = new File(path + "/Music/");                      // Fileクラスのオブジェクトを生成する
        tunesList = fileDir.listFiles();
        File[] repairTunesList = null;                                       // 整形後の楽曲リストの定義
        totalTunesNum = 0;                                                   // 総楽曲数の初期化

        /* 楽曲がある場合、楽曲データの整形を行う */
        if (tunesList != null) {
            int repairTuneLength = 0;                                       // 整形後の楽曲数
            /* ディレクトリからファイルのみ抽出して、楽曲数を数える
            *  対応拡張子だけ抜粋
            * */
            for (File file : tunesList) {
                String filenameString = file.toString();
                if (file.isFile()
                        && (filenameString.toLowerCase().endsWith(mp3String)
                        || filenameString.toLowerCase().endsWith(wavString)
                        || filenameString.toLowerCase().endsWith(m4aString))) {
                    repairTuneLength++;
                }
            }
            repairTunesList = new File[repairTuneLength];                    // 正確な楽曲数を基にしたFile配列

            for (File file : tunesList) {
                String filenameString = file.toString();
                /* 非整形楽曲データ配列を整形用配列に放り込む
                *  対応拡張子だけ抜粋
                * */
                if (file.isFile()
                        && (filenameString.toLowerCase().endsWith(mp3String)
                        || filenameString.toLowerCase().endsWith(wavString)
                        || filenameString.toLowerCase().endsWith(m4aString))) {
                    repairTunesList[totalTunesNum] = file;
                    totalTunesNum++;
                }
            }

            /* 最初の楽曲データ取り出し */
            if (totalTunesNum > 0) {
                nowTune(0);
            }

        }

        return repairTunesList;
    }

    private void nowTune(int i) {
        /* 楽曲UI各種定義 */
        _tuneTitle = findViewById(R.id.tuneTitle);
        _tuneTotalTime = findViewById(R.id.tuneTotalTime);
        _tuneNowTime = findViewById(R.id.tuneNowTime);

        /* メタデータ取り出し */
        Uri mediaFileUri = Uri.parse(tunesList[i].toString());  // 音声ファイルのURI文字列を元にURIオブジェクトを生成
        MediaMetadataRetriever tuneData = new MediaMetadataRetriever(); // メタ情報取り出しのためのクラス
        tuneData.setDataSource(mediaFileUri.toString());        // URIをもとにデータをセットする
        String tuneTitle = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);         // 楽曲タイトル
        String tuneTotalTime = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);  // 楽曲時間（ミリ秒）

        /* 楽曲時間（ミリ秒）の変換作業 */
        int secTmp = Integer.parseInt(tuneTotalTime) / 1000;    // ミリ秒を秒に
        int tuneTotalTimeMin = (secTmp % 3600) / 60;            // 秒を分化し分のみを抽出
        int tuneTotalTimeSec = secTmp % 60;                     // 秒を60で割った余りが分を除いた秒になる
        tuneTotalTime = (tuneTotalTimeMin + ":" + tuneTotalTimeSec);

        _tuneTitle.setText(tuneTitle);
        _tuneTotalTime.setText(tuneTotalTime);

        /* アートファイルの導入 */
        byte[] data = tuneData.getEmbeddedPicture();    // メタファイルから取ったアートファイルをバイト配列に入れる
        if (null != data) { // データが無ければnullにする
            _artFile.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));   // 画像データの代入
        }
    }

    /*
     * TODO:全体実装後とりかかる
     * 楽曲データリピート
     **/
    private void repeatTuneData() {
    }

    /*
     * TODO:全体実装後とりかかる
     * 楽曲データシャッフル
     **/
    private void shuffleTuneData() {
    }

    /*
     * サブメニューボタン押下時の処理
     * 楽曲一覧画面の表示
     **/
    public void onSubMenu(View v) {

        String[] tunesListTitle = new String[totalTunesNum];  // 楽曲タイトルを配列化

        /* 楽曲タイトルをFile配列からString配列に代入 */
        for(int i = 0; i < tunesList.length; i++) {
            tunesListTitle[i] = tunesList[i].getName();
        }

        /* 次画面への準備 */
        Intent intent = new Intent(this, TunesList.class);
        intent.putExtra("tunesList", tunesListTitle);
        startActivity(intent);
    }
}

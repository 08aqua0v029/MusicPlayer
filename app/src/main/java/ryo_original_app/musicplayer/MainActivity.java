
package ryo_original_app.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    /* 音楽ファイル関係 */
    private File fileDir;                   // 楽曲ディレクトリ
    private File songList[];                // 楽曲一覧
    private int totalSongNum = 0;           // 総楽曲数
    private int nowSongNum = 0;             // 楽曲番号


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

        // カスタムクラッシュハンドラを設定
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());

        /* スプラッシュのための中断処理 */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            super.onDestroy();
        }

        // 権限を得る処理
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 1);

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

    // requestPermissionsのコールバック
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "権限が許可されました", Toast.LENGTH_SHORT).show();
            songList = createMusicList();
        }  else {
            Toast.makeText(this, "音楽とオーディオの権限を許可してください", Toast.LENGTH_SHORT).show();
            String uriString = "package:" + getPackageName();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
            startActivity(intent);
        }
    }

    /* 再生・停止ボタン押下処理 */
    public void onPlayButton(View v) {
        if(v.getId() == R.id.btPlay){
            if(btPlayFlag == 0) {
                _btPlay.setImageResource(R.drawable.stop);
                btPlayFlag = 1;
            } else if(btPlayFlag == 1) {
                _btPlay.setImageResource(R.drawable.start);
                btPlayFlag = 0;
            }
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
    }

    /* 楽曲データ取得 */
    private File[] createMusicList() {
        System.out.println("音楽リスト作成");

        // パス生成
        String path = Environment.getExternalStorageDirectory().getPath();
        // Fileクラスのオブジェクトを生成する
        fileDir = new File(path + "/Music/");
        File[] songList = fileDir.listFiles();
        // 整形後の楽曲リストの定義
        File[] repairSongList = null;

        // 楽曲がある場合、楽曲データの整形を行う
        if (songList != null) {
            int repairSongLength = 0;
            // ディレクトリからファイルのみ抽出して、楽曲数を数える
            for (File file : songList) {
                if (file.isFile()) {
                    repairSongLength++;
                }
            }
            // 正確な楽曲数を基にしたFile配列
            repairSongList = new File[repairSongLength];

            for (File file : songList) {
                // 非整形楽曲データ配列を整形用配列に放り込む
                if (file.isFile()) {
                    repairSongList[totalSongNum] = file;
                    totalSongNum++;
                }
            }
        }

        return repairSongList;
    }

    /*
     * TODO:全体実装後とりかかる
     * 楽曲データリピート
     **/
    private void repeatMusicData() {
    }

    /*
     * TODO:全体実装後とりかかる
     * 楽曲データシャッフル
     **/
    private void shuffleMusicData() {
    }

    /*
     * サブメニューボタン押下時の処理
     * 楽曲一覧画面の表示
     **/
    public void onSubMenu(View v) {

        // 楽曲タイトルを配列化
        String[] songListTitle = new String[totalSongNum];

        // 楽曲タイトルをFile配列からString配列に代入
        for(int i = 0; i < songList.length; i++) {
            songListTitle[i] = songList[i].getName();
        }

        // 次画面への準備
        Intent intent = new Intent(this, SongList.class);
        intent.putExtra("songList", songListTitle);
        startActivity(intent);
    }
}

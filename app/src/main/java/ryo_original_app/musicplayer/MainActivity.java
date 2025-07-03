package ryo_original_app.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton _btPlay;
    private ImageButton _btBack;
    private ImageButton _btNext;
    private ImageView _artFile;

    /*
    * 再生か一時停止化のフラグ
    * 0:再生
    * 1:一時停止
    **/
    private int btPlayFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* スプラッシュのための中断処理 */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            String error = e.toString();
            /* ログ出力 */
            LogArea.logInput(error);
            super.onDestroy();
        }

        /* TODO:削除予定　別クラス呼び出す場合 */
        LogArea.logInput("test");

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
    private void createMusicList() {
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
        startActivity(new Intent(this, MusicList.class));
    }
}
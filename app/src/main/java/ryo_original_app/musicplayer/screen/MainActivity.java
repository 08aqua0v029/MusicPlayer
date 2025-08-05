package ryo_original_app.musicplayer.screen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import ryo_original_app.musicplayer.Enum.MusicStatus;
import ryo_original_app.musicplayer.constants.Constants;
import ryo_original_app.musicplayer.log.CustomExceptionHandler;
import ryo_original_app.musicplayer.R;
import ryo_original_app.musicplayer.log.SendLogApi;

/**
 * メインクラス
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    /* UI関係 */
    /** 再生・停止ボタン */
    private ImageButton _btPlay;
    /** ジャケットファイル */
    private ImageView _artFile;
    /** 楽曲タイトル */
    private TextView _tuneTitle;
    /** 1楽曲の総時間 */
    private TextView _tuneTotalTime;
    /** 1楽曲の今の再生時間 */
    private TextView _tuneNowTime;

    /* 音楽ファイル関係 */
    /** メディアプレイヤーインスタンス */
    private MediaPlayer mediaPlayer;
    /** 楽曲ディレクトリ */
    private File fileDir;
    /** 楽曲一覧 */
    private File[] tunesList;
    /** 総楽曲数 */
    private int totalTunesNum = 0;
    /** 楽曲番号 */
    private int nowTuneNum = 0;

    /**
     * 再生状態
     * 0:停止　1:再生　2:一時停止
     */
    private int playState = MusicStatus.STOP.getId();

    /**
     * 生成処理
     * @param savedInstanceState Activity破棄時インスタンス状態を保存
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();

        /* カスタムクラッシュハンドラを設定（全画面対応のためここのみ記載で良い） */
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(context));

        /* ネットワーク接続状態なら、クラッシュJSONログのサーバー保存を行う */
        if(networkConnect()) {
            SendLogApi.sendJsonLog(context, Constants.ApiUri, Constants.crashLogBasicUser, Constants.crashLogBasicPass);
        }else{
            Log.d(Constants.networkString, Constants.nonNetwork);
        }

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

    }

    /**
     * Viewの再表示
     */
    @Override
    protected void onResume() {
        super.onResume();

        /* パーミッションを設定画面で許可した場合、アプリに戻ってきた際に、楽曲リストを取得する */
        if (Objects.isNull(tunesList) && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            tunesList = createTunesList();
        }
    }

    /**
     * requestPermissionsのコールバック（音楽ファイルパーミッション取得）
     * @param requestCode 未使用
     * @param permissions 未使用
     * @param grantResults OSが許可を出したかどうか
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, Constants.permissionSentence, Toast.LENGTH_SHORT).show();
            tunesList = createTunesList();  // 許可されたので、楽曲リストを作成する
        }  else {
            Toast.makeText(this, Constants.unauthorizedSentence, Toast.LENGTH_SHORT).show();

            /* 設定画面への強制移動 */
            String uriString = "package:" + getPackageName();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
            startActivity(intent);
        }
    }

    /**
     * ネットワーク接続状態チェック
     * @return 通信が可能な状態 or false
     */
    public boolean networkConnect() {
        /* ネットワークの状態チェック */
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        /* アクティブな（Wifi、モバイル）通信のチェック */
        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        /* 今まで取ってきたものは通信できるかどうかのチェック */
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /**
     * クリック処理
     * ※各ボタンのクリック処理を充てているので、反応しないようにしている
     * @param v View情報
     */
    @Override
    public void onClick(View v) {
    }

    /**
     * 楽曲データの存在チェック
     * @param task 実行するタスク
     * @return 処理のキャンセル
     */
    private void runMusicDataCheck(Runnable task) {
        /* 楽曲データの存在チェック */
        if(Objects.isNull(tunesList)){
            Toast.makeText(this, Constants.nonMusicDate, Toast.LENGTH_SHORT).show();
            return;
        }
        task.run();
    }

    /**
     * 再生・停止ボタン押下処理
     * @param v View情報
     */
    public void onPlayButton(View v) {
        /* 楽曲データの存在チェック */
        runMusicDataCheck(() -> {
            /*
             * 再生停止処理　以下状態
             * 0:停止　1:再生　2:一時停止
             * */
            if (playState == MusicStatus.STOP.getId()) {
                _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
                tuneSetup();            // 楽曲セットアップ
                mediaPlayer.start();    // プレイヤースタート
                playState = MusicStatus.START.getId();          // 再生状態にする
            } else if (playState == MusicStatus.START.getId()) {
                _btPlay.setImageResource(R.drawable.start); // ボタン画像を変える
                mediaPlayer.pause();    // プレイヤー一時停止
                playState = MusicStatus.PAUSE.getId();          // 一時停止状態にする
            } else if (playState == MusicStatus.PAUSE.getId()) {
                _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
                mediaPlayer.start();    // 楽曲セットアップをせずに、一時停止したところから再生
                playState = MusicStatus.START.getId();          // 再生状態にする
            }
        });
    }

    /**
     * 戻るボタン押下処理
     * @param v View情報
     */
    public void onBackButton(View v) {
        /* 楽曲データの存在チェック */
        runMusicDataCheck(() -> {
            /* TODO:実装後削除 */
            System.out.println("modoru");
        });
    }

    /**
     * 次ボタン押下処理
     * @param v View情報
     */
    public void onNextButton(View v) {
        /* 楽曲データの存在チェック */
        runMusicDataCheck(() -> {
            /* 楽曲データの存在チェック */
            if (Objects.isNull(tunesList)) {
                Toast.makeText(this, Constants.nonMusicDate, Toast.LENGTH_SHORT).show();
                return;
            }
            /* 再生中の場合は楽曲を念の為止める */
            if (playState == MusicStatus.START.getId()) {
                mediaPlayer.stop();
            }

            /* 総楽曲数まではnowTuneNumをカウントし、総楽曲数以上のカウントになった場合はカウントをリセット */
            /* 楽曲番号が０スタートのため、総楽曲数を -1 しないと整合性がとれない */
            /* TODO: ここの数字 -1 を消すと簡単にアプリをクラッシュできる！ */
            //if (totalTunesNum > nowTuneNum) {
            if (totalTunesNum -1 > nowTuneNum) {
                nowTuneNum++;
                _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
                nowTune(nowTuneNum);    // 楽曲データ取得
                tuneSetup();            // 楽曲セットアップ
                mediaPlayer.start();    // プレイヤースタート
                playState = MusicStatus.START.getId();          // 再生状態にする
            } else {
                /* TODO: リピート機能未実装のため、総楽曲一周したら一度停止処理をかます */
                nowTuneNum = 0;         // 楽曲番号のリセット
                _btPlay.setImageResource(R.drawable.start);  // ボタン画像を変える
                nowTune(nowTuneNum);    // 楽曲データ取得
                tuneSetup();            // 楽曲セットアップ
                mediaPlayer.stop();     // 楽曲停止
                playState = MusicStatus.STOP.getId();          // 停止状態にする
            }
        });
    }

    /**
     * 全楽曲データ取得
     * @return 正確な楽曲数を基にしたFile配列
     */
    private File[] createTunesList() {
        String path = Environment.getExternalStorageDirectory().getPath();   // パス生成
        fileDir = new File(path + Constants.musicFolder);           // Fileクラスのオブジェクトを生成する
        tunesList = fileDir.listFiles();                                     // フォルダ内データをリストに突っ込む
        File[] repairTunesList = null;                                       // 整形後の楽曲リストの定義
        totalTunesNum = 0;                                                   // 総楽曲数の初期化

        /* 楽曲がある場合、楽曲データの整形を行う */
        if (tunesList != null) {
            int repairTuneLength = getRepairTuneLength();
            repairTunesList = new File[repairTuneLength];                    // 正確な楽曲数を基にしたFile配列

            /* 非整形楽曲データ配列を整形用配列に放り込む　対応拡張子だけ抜粋 */
            for (File file : tunesList) {
                String filenameString = file.toString();

                if (file.isFile()
                        && (filenameString.toLowerCase().endsWith(Constants.mp3String)
                        || filenameString.toLowerCase().endsWith(Constants.wavString)
                        || filenameString.toLowerCase().endsWith(Constants.m4aString))) {
                    repairTunesList[totalTunesNum] = file;
                    totalTunesNum++;
                }
            }

            /* 最初の楽曲データ取り出し */
            if (totalTunesNum > 0) {
                nowTune(nowTuneNum);
            }

        }else{
            Toast.makeText(this, Constants.nonMusicDate, Toast.LENGTH_SHORT).show();
        }

        return repairTunesList;
    }

    /**
     * 正確な楽曲数の計算
     * @return 計算した正確な楽曲数
     */
    private int getRepairTuneLength() {
        int repairTuneLength = 0;                                       // 整形後の楽曲数
        /* ディレクトリからファイルのみ抽出して、楽曲数を数える　対応拡張子だけ抜粋 */
        for (File file : tunesList) {
            String filenameString = file.toString();
            if (file.isFile()
                    && (filenameString.toLowerCase().endsWith(Constants.mp3String)
                    || filenameString.toLowerCase().endsWith(Constants.wavString)
                    || filenameString.toLowerCase().endsWith(Constants.m4aString))) {
                repairTuneLength++;
            }
        }
        return repairTuneLength;
    }

    /**
     * 今の楽曲データ取得
     * @param i 楽曲番号
     */
    private void nowTune(int i) {
        /* 楽曲UI各種定義 */
        _tuneTitle = findViewById(R.id.tuneTitle);
        _tuneTotalTime = findViewById(R.id.tuneTotalTime);
        _tuneNowTime = findViewById(R.id.tuneNowTime);

        /* メタデータ取り出し */
        try(MediaMetadataRetriever tuneData = new MediaMetadataRetriever()) {   // メタ情報取り出しのためのクラス
            tuneData.setDataSource(tunesList[i].toString());        // URIをもとにデータをセットする
            String tuneTitle = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);         // 楽曲タイトル
            String tuneTotalTime = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);  // 楽曲時間（ミリ秒）

            /* 楽曲時間（ミリ秒）の変換作業 */
            if(tuneTotalTime != null) {
                int secTmp = Integer.parseInt(tuneTotalTime) / 1000;    // ミリ秒を秒に
                int tuneTotalTimeMin = (secTmp % 3600) / 60;            // 秒を分化し分のみを抽出
                int tuneTotalTimeSec = secTmp % 60;                     // 秒を60で割った余りが分を除いた秒になる
                tuneTotalTime = (tuneTotalTimeMin + Constants.colonString + tuneTotalTimeSec);    // 時間を整形
            } else {
                tuneTotalTime = Constants.initialTime;
            }
            /* UI側にテキストを代入 */
            _tuneTitle.setText(tuneTitle);
            _tuneTotalTime.setText(tuneTotalTime);

            /* アートファイルの導入 */
            byte[] data = tuneData.getEmbeddedPicture();    // メタファイルから取ったアートファイルをバイト配列に入れる
            if (null != data) { // データが無ければnullにする
                _artFile.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));   // 画像データの代入
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 楽曲再生準備
     */
    private void tuneSetup(){
        /* メディアプレイヤーが起動している場合release（オブジェクトの開放）と初期化 */
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        /* 楽曲データ取得 */
        mediaPlayer = new MediaPlayer();    // インスタンス化
        String nowTuneDir = tunesList[nowTuneNum].toString();   // 楽曲までのディレクトリをString化

        try {
            mediaPlayer.setDataSource(nowTuneDir);  // メディアプレイヤーに楽曲データをセット
            mediaPlayer.prepare();                  // 再生準備（同期）
        } catch (IOException e) {
            Toast.makeText(this, Constants.playErrorSentence + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * TODO:全体実装後とりかかる
     * 楽曲データリピート
     */
    private void repeatTuneData() {
    }

    /**
     * TODO:全体実装後とりかかる
     * 楽曲データシャッフル
     */
    private void shuffleTuneData() {
    }

    /**
     * サブメニューボタン押下時の処理（楽曲一覧画面の表示）
     * @param v View情報
     */
    public void onSubMenu(View v) {
        /* 楽曲データの存在チェック */
        runMusicDataCheck(() -> {
            String[] tunesListTitle = new String[totalTunesNum];  // 楽曲タイトルを配列化

            /* 楽曲タイトルをFile配列からString配列に代入 */
            for (int i = 0; i < tunesList.length; i++) {
                tunesListTitle[i] = tunesList[i].getName();
            }

            /* 次画面への準備 */
            Intent intent = new Intent(this, TunesList.class);
            intent.putExtra("tunesList", tunesListTitle);
            startActivity(intent);
        });
    }
}

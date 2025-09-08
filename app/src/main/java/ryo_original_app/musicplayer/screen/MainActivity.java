package ryo_original_app.musicplayer.screen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import ryo_original_app.musicplayer.Enum.MusicStatus;
import ryo_original_app.musicplayer.constants.Constants;
import ryo_original_app.musicplayer.convenience.DataShaping;
import ryo_original_app.musicplayer.convenience.MusicTimer;
import ryo_original_app.musicplayer.convenience.NetworkConnect;
import ryo_original_app.musicplayer.log.CustomExceptionHandler;
import ryo_original_app.musicplayer.R;
import ryo_original_app.musicplayer.log.SendLogApi;
import ryo_original_app.musicplayer.service.MediaPlaybackService;

/**
 * メインクラス
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;
    /** タイマークラス */
    private MusicTimer musicTimer;
    private ActivityResultLauncher<Intent> resultLauncher;

    /* UI関係 */
        /** 再生関連ボタン */
        private ImageButton _btPlay, _btBack, _btNext;
        /** ジャケットファイル */
        private ImageView _artFile;
        /** 楽曲タイトル */
        private TextView _tuneTitle;
        /** 楽曲アーティスト名 */
        private TextView _tuneArtist;
        /** 1楽曲の総時間 */
        private TextView _tuneTotalTime;
        /** 1楽曲の今の再生時間 */
        private TextView _tuneNowTime;
        /** シークバー */
        private SeekBar _seekBar;

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
        /** 戻るボタン押下時の時間計測（計算するメソッドがLong型対応なので、それに合わせている） */
        private long backButtonPressTime  = 0;
        /** 指定楽曲のメタデータ */
        MediaMetadataRetriever tuneData;
        /** アートファイルデータ */
        byte[] artFileData;
        /** 楽曲タイトル */
        String tuneTitle = "";
        /** 楽曲アーティスト名 */
        String tuneArtist = "";
        /** メディアファイルの準備完了フラグ */
        private boolean isPrepared = false;


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

        /* 以下メイン画面描画用処理 */
        setTheme(R.style.Base_Theme_MusicPlayer);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            /* フルスクリーン設定 */
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        /* 旧ビルドバージョン用設定 */
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        /* アクションバー非表示 */
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        /* カスタムクラッシュハンドラを設定（全画面対応のためここのみ記載で良い） */
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(context));

        /* ネットワーク接続状態なら、クラッシュJSONログのサーバー保存を行う */
        if(NetworkConnect.isConnected(context)) {
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

        /* 権限を得る処理（Android13未満はREAD_EXTERNAL_STORAGEだけでよい） */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_MEDIA_AUDIO,   // 音声データパーミッション
                            Manifest.permission.POST_NOTIFICATIONS  // 通知パーミッション
                    }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        /* 通知サービス（Foreground Service）の起動 */
        Intent serviceIntent = new Intent(this, MediaPlaybackService.class);
        startForegroundService(serviceIntent);  // Android 8以上必須

        /* ステータスバー削除処理 */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* アートファイル定義 */
        _artFile = findViewById(R.id.artFile);
        _artFile.setImageResource(R.drawable.test_art);

        /* 各種UI定義 */
        _btPlay = findViewById(R.id.btPlay);
        _btBack = findViewById(R.id.btBack);
        _btNext = findViewById(R.id.btNext);
        _seekBar = findViewById(R.id.seekbar);

        _btPlay.setOnClickListener(this);
        _btBack.setOnClickListener(this);
        _btNext.setOnClickListener(this);

        mediaPlayer = new MediaPlayer();    // メディアプレイヤー初期化

        /* 楽曲一覧画面から選択した楽曲番号を取得し再生する */
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result ->{

                    /* タイマー未起動時の処理で落ちるため、初期化処理 */
                    if(Objects.isNull(musicTimer)) {
                        musicTimer = new MusicTimer(this);
                    }
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent intent = result.getData();
                        /* 選択した楽曲番号を代入 */
                        nowTuneNum = intent.getIntExtra("selectTunesNum", 0);

                        _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
                        nowTune(nowTuneNum);    // 楽曲データ取得
                        tuneSetup();            // 楽曲セットアップ
                        mediaPlayer.start();    // プレイヤースタート
                        musicTimer.startTimer(mediaPlayer, tuneData);     // タイマー計測
                        playState = MusicStatus.START.getId();  // 再生状態にする
                    }
                });

        /* メディアファイルの準備完了 */
        mediaPlayer.setOnPreparedListener(mp -> {
            isPrepared = true;  // フラグを準備完了に
            _seekBar.setMax(mp.getDuration());   // シークバーの最大値をミリ秒で設定
        });

        /* シークバーの処理 */
        _seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
            * つまみが変更された時に処理が実行される
            */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /* シークバーを操作していないときはつまみが変更された際の処理を通さない
                 シークバーの位置のリアルタイム表示の多重起動を抑える */
                if (!fromUser) {return;}
                mediaPlayer.seekTo(progress); // つまみを移動した場所にシークバーの進捗をUIにセット（ミリ秒）
            }

            /**
            * ユーザーがタップ開始した時に処理が実行される
            */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            /**
            * ユーザーがタップ終了した時に処理が実行される
            */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
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
     * アプリ終了処理
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();

        musicTimer.stopTimer();

        /* メディアプレイヤーが起動している場合release（オブジェクトの開放）と初期化 */
        /* 端末によって、アプリのタスクキルをしても音楽が再生されっぱなしになる */
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
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
     * 通知情報の更新
     */
    private void updateNotification() {
        /* アートファイルを一時保存して、通知サービスに渡せるようにしている */
        File cacheArt = new File(getCacheDir(), "current_art.jpg");
        try (FileOutputStream fos = new FileOutputStream(cacheArt)) {
            fos.write(artFileData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* サービス側に情報を提供 */
        Intent intent = new Intent(this, MediaPlaybackService.class);
        intent.putExtra("title", tuneTitle);
        intent.putExtra("artist", tuneArtist);
        intent.putExtra("artPath", cacheArt.getAbsolutePath());

        /* サービスの開始 */
        startService(intent);
    }

    /**
     * クリック処理
     * @param v View情報
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btPlay) {
            onPlay();
        } else if (id == R.id.btBack) {
            onBack();
        } else if (id == R.id.btNext) {
            onNext();
        }
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
     * 再生・停止処理
     */
    public void onPlay() {
        /* 楽曲データの存在チェック */
        runMusicDataCheck(() -> {
            musicTimer = new MusicTimer(this);
            /*
             * 再生停止処理　以下状態
             * 0:停止　1:再生　2:一時停止
             * */
            if (playState == MusicStatus.STOP.getId()) {
                _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
                tuneSetup();            // 楽曲セットアップ
                mediaPlayer.start();    // プレイヤースタート
                musicTimer.startTimer(mediaPlayer, tuneData);  // タイマー計測
                playState = MusicStatus.START.getId();          // 再生状態にする
            } else if (playState == MusicStatus.START.getId()) {
                _btPlay.setImageResource(R.drawable.start); // ボタン画像を変える
                mediaPlayer.pause();    // プレイヤー一時停止
                playState = MusicStatus.PAUSE.getId();          // 一時停止状態にする
            } else if (playState == MusicStatus.PAUSE.getId()) {
                _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
                mediaPlayer.start();    // 楽曲セットアップをせずに、一時停止したところから再生
                musicTimer.startTimer(mediaPlayer, tuneData);     // タイマー計測
                playState = MusicStatus.START.getId();          // 再生状態にする
            }
        });
    }

    /**
     * 戻る再生処理
     */
    public void onBack() {

        long pressSystemTime = System.currentTimeMillis();    // 押下した時間計測のためシステムの時間を取り出す

        /* 楽曲データの存在チェック */
        runMusicDataCheck(() -> {
            musicTimer = new MusicTimer(this);   // タイマーの呼び出し

            /* メディアプレイヤーが起動していないのに戻るボタンを押下したら、処理を通さない */
            if(Objects.isNull(mediaPlayer)){
                return;
            }

            /* 再生中の場合は楽曲を念の為止める */
            if (playState == MusicStatus.START.getId()) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            }

            /* 1秒以内に2回押下したなら1曲前に戻る */
            if(pressSystemTime - backButtonPressTime < 1000) {
                mediaPlayer = new MediaPlayer();    // mediaPayer初期化
                /* 0番目の楽曲以外の場合1曲前へ戻す */
                if(nowTuneNum > 0){
                    nowTuneNum--;   // 1曲前へ
                }
                nowTune(nowTuneNum);    // 楽曲データ取得
                tuneSetup();            // 楽曲セットアップ
            }
            _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
            mediaPlayer.start();                        // プレイヤースタート
            musicTimer.startTimer(mediaPlayer, tuneData);         // タイマー計測
            playState = MusicStatus.START.getId();      // 再生状態にする
            backButtonPressTime = pressSystemTime;      // 時刻の更新（押下した際のシステム時間を挿入）
        });
    }

    /**
     * 次再生処理
     */
    public void onNext() {
        /* 楽曲データの存在チェック */
        runMusicDataCheck(() -> {
            musicTimer = new MusicTimer(this);   // タイマーの呼び出し

            /* 再生中の場合は楽曲を念の為止める */
            if (playState == MusicStatus.START.getId()) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            }

            mediaPlayer = new MediaPlayer();    // MediaPlayerの初期化

            /* 総楽曲数まではnowTuneNumをカウントし、総楽曲数以上のカウントになった場合はカウントをリセット */
            /* 楽曲番号が0スタートのため、総楽曲数を -1 しないと整合性がとれない */
            /* TODO: ここの数字 -1 を消すと簡単にアプリをクラッシュできる！検証用に使う */
            //if (totalTunesNum > nowTuneNum) {
            if (totalTunesNum -1 > nowTuneNum) {
                nowTuneNum++;
                _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
                nowTune(nowTuneNum);    // 楽曲データ取得
                tuneSetup();            // 楽曲セットアップ
                mediaPlayer.start();    // プレイヤースタート
                musicTimer.startTimer(mediaPlayer, tuneData);  // タイマー計測
                playState = MusicStatus.START.getId();         // 再生状態にする
            } else {
                /* TODO: リピート機能未実装のため、総楽曲一周したら一度停止処理をかます */
                nowTuneNum = 0;         // 楽曲番号のリセット
                _btPlay.setImageResource(R.drawable.start);    // ボタン画像を変える
                nowTune(nowTuneNum);    // 楽曲データ取得
                tuneSetup();            // 楽曲セットアップ
                _seekBar.setProgress((int) 0);                 // シークバーの進捗をUIにセット
                _tuneNowTime.setText(Constants.initialTime);   // 再生時間をUIにセット
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
        _tuneArtist = findViewById(R.id.tuneArtist);

        /* メタ情報取り出しのためのクラス */
        tuneData = new MediaMetadataRetriever();

        /* メタデータ取り出し */
        try{
            tuneData.setDataSource(tunesList[i].toString());        // URIをもとにデータをセットする
            tuneTitle = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);         // 楽曲タイトル
            tuneArtist = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);       // アーティスト名
            String tuneTotalTime = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);  // 楽曲時間（ミリ秒）

            /* 楽曲時間（ミリ秒）の変換作業 */
            if(tuneTotalTime != null) {
                DataShaping shaping = new DataShaping();
                tuneTotalTime = shaping.timeFormat(tuneTotalTime);
            } else {
                tuneTotalTime = Constants.initialTime;
            }
            /* UI側にテキストを代入 */
            _tuneTitle.setText(tuneTitle);
            _tuneArtist.setText(tuneArtist);
            _tuneTotalTime.setText(tuneTotalTime);

            /* アートファイルの導入 */
            artFileData = tuneData.getEmbeddedPicture();    // メタファイルから取ったアートファイルをバイト配列に入れる
            if (null != artFileData) { // データが無ければnullにする
                _artFile.setImageBitmap(BitmapFactory.decodeByteArray(artFileData, 0, artFileData.length));   // 画像データの代入
            }

            updateNotification();

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

            /* 再生終了時処理 */
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    onNext(); // 次の楽曲へ
                }
            });
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
            resultLauncher.launch(intent);  // launchを行うことで、遷移先から戻る際、情報を持ってこれる
            // startActivity(intent); // 本来の画面遷移（後学用に残している）
        });
    }
}

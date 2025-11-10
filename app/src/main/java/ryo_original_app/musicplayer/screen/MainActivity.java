package ryo_original_app.musicplayer.screen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;

import ryo_original_app.musicplayer.Enum.MusicStatus;
import ryo_original_app.musicplayer.Enum.RepeatStatus;
import ryo_original_app.musicplayer.Enum.ShuffleStatus;
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

    /** コンテキスト */
    private Context context;
    /** タイマークラス */
    private MusicTimer musicTimer;
    /** アクティビティ保持用 */
    private ActivityResultLauncher<Intent> resultLauncher;
    /** インテント */
    private Intent serviceIntent;

    /* UI関係 */
        /** 再生関連ボタン */
        private ImageButton _btPlay, _btBack, _btNext, _btRepeat, _btShuffle;
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
        /** 一時保管用楽曲一覧 */
        private File[] tmpTunesList;
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
        /** 楽曲情報を詰めた配列 */
        JSONObject nowPlayingJson;
        /** メディアファイルの準備完了フラグ */
        private boolean isPrepared = false;

    /**
     * 再生状態
     * 0:停止　1:再生　2:一時停止
     */
    private int playState = MusicStatus.STOP.getId();

    /**
     * リピート状態
     * 0:リピート無効 1:全曲リピート 2:1曲リピート
     */
    private int repeatState = RepeatStatus.NO_REPEAT.getId();

    /**
     * シャッフル状態
     * 0:シャッフル無効 1:シャッフル有効
     */
    private int shuffleState = ShuffleStatus.NO_SHUFFLE.getId();

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
            /* ファイルの準備 */
            File inputFile = new File(context.getFilesDir(), Constants.logFolder + Constants.slashString + Constants.crashLogFile);
            /* JSONファイルをString化 */
            String jsonFile = null;
            try {
                jsonFile = new String(Files.readAllBytes(inputFile.toPath()), StandardCharsets.UTF_8);
                SendLogApi.sendJsonLog(jsonFile, Constants.crashLogApiUri, Constants.crashLogBasicUser, Constants.crashLogBasicPass);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        serviceIntent = new Intent(this, MediaPlaybackService.class);
        startForegroundService(serviceIntent);  // Android 8以上必須

        /* ステータスバー削除処理 */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* アートファイル定義 */
        _artFile = findViewById(R.id.artFile);
        _artFile.setImageResource(R.drawable.default_art);

        /* 各種UI定義 */
        _btPlay = findViewById(R.id.btPlay);
        _btBack = findViewById(R.id.btBack);
        _btNext = findViewById(R.id.btNext);
        _btRepeat = findViewById(R.id.btRepeat);
        _btShuffle = findViewById(R.id.btShuffle);
        _seekBar = findViewById(R.id.seekbar);

        _btPlay.setOnClickListener(this);
        _btBack.setOnClickListener(this);
        _btNext.setOnClickListener(this);
        _btRepeat.setOnClickListener(this);
        _btShuffle.setOnClickListener(this);

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
                        musicTimer.startTimer(mediaPlayer, nowPlayingJson, context);     // タイマー計測
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

            boolean networkConnected = NetworkConnect.isConnected(context);

            /* サーバー側のNowPlaying機能の表示データを初期化する */
            cleanMetaData();

            /* ネットワークが接続していればNowPlayingAPIへログを送信 */
            if(networkConnected) {
                SendLogApi.sendPlayingLog(nowPlayingJson, Constants.initialTime);
            }

            /* 通知を消す */
            stopService(serviceIntent);
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

            boolean networkConnected = NetworkConnect.isConnected(context);
            /* ネットワークが接続していればNowPlayingAPIへログを送信 */
            /* 楽曲データが存在しない場合は通さないようにする */
            if(networkConnected && tunesList.length > 0) {
                SendLogApi.sendPlayingLog(nowPlayingJson, Constants.initialTime);
            }
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

        /* 楽曲が存在すればボタン押下可能、存在しなければ押下禁止 */
        if(tunesList.length > 0){
            /* リピートボタン押下処理 */
            if(id == R.id.btRepeat){
                onRepeat();
            }

            /* 楽曲一覧シャッフルボタン押下処理 */
            if(id == R.id.btShuffle){
                onShuffle();
            }

            /* 再生関連各種ボタン押下処理 */
            if(id == R.id.btPlay){
                onPlay();
            }else if(id == R.id.btBack){
                onBack();
            }else if(id == R.id.btNext){
                onNext();
            }
        }else if(Objects.isNull(tunesList)){
            Toast.makeText(this, Constants.notTouchButton, Toast.LENGTH_SHORT).show();
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
             */
            if (playState == MusicStatus.STOP.getId()) {
                _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
                tuneSetup();            // 楽曲セットアップ
                mediaPlayer.start();    // プレイヤースタート
                musicTimer.startTimer(mediaPlayer, nowPlayingJson, context);  // タイマー計測
                playState = MusicStatus.START.getId();          // 再生状態にする
            } else if (playState == MusicStatus.START.getId()) {
                _btPlay.setImageResource(R.drawable.start); // ボタン画像を変える
                mediaPlayer.pause();    // プレイヤー一時停止
                playState = MusicStatus.PAUSE.getId();          // 一時停止状態にする
            } else if (playState == MusicStatus.PAUSE.getId()) {
                _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
                mediaPlayer.start();    // 楽曲セットアップをせずに、一時停止したところから再生
                musicTimer.startTimer(mediaPlayer, nowPlayingJson, context);     // タイマー計測
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
                /* 0番目の楽曲、1楽曲リピートをしていない場合は1曲前へ戻す */
                if(nowTuneNum > 0  && repeatState != RepeatStatus.ONE_REPEAT.getId()){
                    nowTuneNum--;   // 1曲前へ
                }
                nowTune(nowTuneNum);    // 楽曲データ取得
                tuneSetup();            // 楽曲セットアップ
            }
            _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
            mediaPlayer.start();                        // プレイヤースタート
            musicTimer.startTimer(mediaPlayer, nowPlayingJson, context);         // タイマー計測
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

            /*
             * 総楽曲数まではnowTuneNumをカウントし、総楽曲数以上のカウントになった場合はカウントをリセット
             * リピート機能が効いている場合、再生のまま、効いていない場合は1曲目で停止
             * 1楽曲リピート機能が効いている場合は、次の楽曲には進まない
             * 楽曲番号が0スタートのため、総楽曲数を -1 しないと整合性がとれない
             */
            /* TODO: ここの数字 -1 を消すと簡単にアプリをクラッシュできる！検証用に使う */
            //if (totalTunesNum > nowTuneNum) {
            if (totalTunesNum -1 > nowTuneNum) {
                /* 1楽曲リピートされていない場合、次の楽曲へ移る処理を通す */
                if(repeatState != RepeatStatus.ONE_REPEAT.getId()) {
                    nowTuneNum++;
                }
                onNextPlay();
            }else if(repeatState == RepeatStatus.ALL_REPEAT.getId()) {
                /* 全楽曲がリピートされている場合 */
                nowTuneNum = 0;         // 楽曲番号のリセット
                onNextPlay();
            }else{
                if(repeatState == RepeatStatus.ONE_REPEAT.getId()) {
                    /* 最終楽曲が1楽曲リピートされている場合 */
                    onNextPlay();
                }else{
                    /* 以下リピートされていない場合 */
                    nowTuneNum = 0;         // 楽曲番号のリセット
                    _btPlay.setImageResource(R.drawable.start);    // ボタン画像を変える
                    nowTune(nowTuneNum);    // 楽曲データ取得
                    tuneSetup();            // 楽曲セットアップ
                    _seekBar.setProgress((int) 0);                 // シークバーの進捗をUIにセット
                    _tuneNowTime.setText(Constants.initialTime);   // 再生時間をUIにセット
                    playState = MusicStatus.STOP.getId();          // 停止状態にする
                }

                /* ネットワークが接続していればNowPlayingAPIへログを送信 */
                boolean networkConnected = NetworkConnect.isConnected(context);
                if(networkConnected) {
                    SendLogApi.sendPlayingLog(nowPlayingJson, Constants.initialTime);
                }
            }
        });
    }

    public void onNextPlay(){
        _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
        nowTune(nowTuneNum);    // 楽曲データ取得
        tuneSetup();            // 楽曲セットアップ
        mediaPlayer.start();    // プレイヤースタート
        musicTimer.startTimer(mediaPlayer, nowPlayingJson, context);  // タイマー計測
        playState = MusicStatus.START.getId();         // 再生状態にする
    }

    /**
     * リピートボタン押下処理
     */
    public void onRepeat() {
        if(repeatState == RepeatStatus.NO_REPEAT.getId()){
            repeatState = RepeatStatus.ALL_REPEAT.getId();          // ステータスを変更
            _btRepeat.setImageResource(R.drawable.all_repeat);      // ボタン画像を変える
        }else if(repeatState == RepeatStatus.ALL_REPEAT.getId()){
            repeatState = RepeatStatus.ONE_REPEAT.getId();          // ステータスを変更
            _btRepeat.setImageResource(R.drawable.one_repeat);      // ボタン画像を変える
        }else if(repeatState == RepeatStatus.ONE_REPEAT.getId()){
            repeatState = RepeatStatus.NO_REPEAT.getId();           // ステータスを変更
            _btRepeat.setImageResource(R.drawable.no_repeat);       // ボタン画像を変える
        }
    }

    /**
     * シャッフルボタン押下処理（シャッフル機能を使うと、1曲目に戻す仕様）
     */
    public void onShuffle() {
        if(shuffleState == ShuffleStatus.NO_SHUFFLE.getId()){
            /* 楽曲リストをシャッフルする */
            tmpTunesList = Arrays.copyOf(tunesList, tunesList.length);  // シャッフル初期化用にリストを一時保管
            Collections.shuffle(Arrays.asList(tunesList));          // リストをシャッフル
            shuffleState = ShuffleStatus.SHUFFLE.getId();           // ステータスを変更
            _btShuffle.setImageResource(R.drawable.shuffle);        // ボタン画像を変える
        }else if(shuffleState == ShuffleStatus.SHUFFLE.getId()){
            tunesList = Arrays.copyOf(tmpTunesList, tmpTunesList.length);  // リストを元に戻す
            shuffleState = ShuffleStatus.NO_SHUFFLE.getId();        // ステータスを変更
            _btShuffle.setImageResource(R.drawable.no_shuffle);     // ボタン画像を変える
        }

        musicTimer = new MusicTimer(this);   // タイマーの呼び出し

        /* 再生中の場合は楽曲を念の為止める */
        if (playState == MusicStatus.START.getId()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }

        mediaPlayer = new MediaPlayer();    // MediaPlayerの初期化

        _btPlay.setImageResource(R.drawable.stop);  // ボタン画像を変える
        nowTuneNum = 0;
        nowTune(nowTuneNum);           // 楽曲データ取得（使用上1曲目に戻す）
        tuneSetup();            // 楽曲セットアップ
        mediaPlayer.start();    // プレイヤースタート
        musicTimer.startTimer(mediaPlayer, nowPlayingJson, context);  // タイマー計測
        playState = MusicStatus.START.getId();         // 再生状態にする
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
                    /* ファイル破損チェック */
                    if (damageFileCheck(file.getAbsolutePath())) {
                        repairTunesList[totalTunesNum] = file;
                        totalTunesNum++;
                    } else {
                        Log.w("MusicScan", "破損または非対応形式: " + file.getName());
                    }
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
        _tuneArtist = findViewById(R.id.tuneArtist);

        /* メタ情報取り出しのためのクラス */
        tuneData = new MediaMetadataRetriever();

        /* メタデータ取り出し */
        try{
            tuneData.setDataSource(tunesList[i].toString());        // URIをもとにデータをセットする

            /* 楽曲タイトルを取り出す nullなら規定文字を入れる */
            tuneTitle = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if(Objects.isNull(tuneTitle)){
                tuneTitle = Constants.unknown;
            }
            /* 楽曲アーティストを取り出す nullなら規定文字を入れる */
            tuneArtist = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if(Objects.isNull(tuneArtist)){
                tuneArtist = Constants.unknown;
            }
            /* 楽曲時間を取り出す（ミリ秒） */
            String tuneTotalTime = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            /* 楽曲時間（ミリ秒からXX:XX）の変換作業 */
            if(!Objects.isNull(tuneTotalTime)) {
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
            String base64artFile = "";

            /* アートファイルが存在しなければ、デフォルト画像(png)を入れる */
            if (Objects.isNull(artFileData)) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_art);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                artFileData = os.toByteArray();
            }

            base64artFile = Base64.getEncoder().encodeToString(artFileData); // 外部に送るためBase64形式に変換
            _artFile.setImageBitmap(BitmapFactory.decodeByteArray(artFileData, 0, artFileData.length));   // 画像データの代入

            // MIMEType取得
            ByteArrayInputStream is = new ByteArrayInputStream(artFileData);
            String mimeType = URLConnection.guessContentTypeFromStream(is);

            /* メタデータを配列に詰める */
            nowPlayingJson = new JSONObject();
            nowPlayingJson.put(Constants.tuneTitleKey, tuneTitle);
            nowPlayingJson.put(Constants.tuneArtistKey, tuneArtist);
            nowPlayingJson.put(Constants.tuneNowTimeKey, Constants.initialTime);
            nowPlayingJson.put(Constants.tuneTotalTimeKey, tuneTotalTime);
            nowPlayingJson.put(Constants.tuneArtMimeTypeKey, mimeType);
            nowPlayingJson.put(Constants.tuneArtKey, base64artFile);

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
     * 音楽ファイル破損チェック
     */
    private boolean damageFileCheck(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            /* 再生時間を取得することで、ファイルの破損チェックを行う */
            String playTime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return playTime != null;    // 再生時間があればtrue、nullならfalse
        } catch (Exception e) {
            return false;   // エラーがあった場合は問答無用でfalse
        } finally {
            /* MediaMetadataRetrieverを確実に開放する */
            try {
                retriever.release();
            } catch (Exception ignored) {}
        }
    }

    /**
     * メタデータ初期化
     *
     */
    public void cleanMetaData() {
        try {
            nowPlayingJson = new JSONObject();
            nowPlayingJson.put(Constants.tuneTitleKey, Constants.unknown);
            nowPlayingJson.put(Constants.tuneArtistKey, Constants.unknown);
            nowPlayingJson.put(Constants.tuneNowTimeKey, Constants.initialTime);
            nowPlayingJson.put(Constants.tuneTotalTimeKey, Constants.initialTime);
            nowPlayingJson.put(Constants.tuneArtMimeTypeKey, Constants.jpegMimeType);
            nowPlayingJson.put(Constants.tuneArtKey, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            for (int i = 0; i < totalTunesNum; i++) {

                /* メタ情報取り出しのためのクラス */
                tuneData = new MediaMetadataRetriever();

                /* メタデータ取り出し */
                try {
                    tuneData.setDataSource(tunesList[i].toString());        // URIをもとにデータをセットする

                    /* 楽曲タイトルを取り出す nullなら規定文字を入れる */
                    tuneTitle = tuneData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    System.out.println(tuneTitle);
                    if (Objects.isNull(tuneTitle)) {
                        tuneTitle = Constants.unknown;
                    }

                    tunesListTitle[i] = tuneTitle;  // 配列に楽曲タイトルを代入
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            /* 次画面への準備 */
            Intent intent = new Intent(this, TunesList.class);
            intent.putExtra("tunesListTitle", tunesListTitle);
            resultLauncher.launch(intent);  // launchを行うことで、遷移先から戻る際、情報を持ってこれる
            // startActivity(intent); // 本来の画面遷移（後学用に残している）
        });
    }
}
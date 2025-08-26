package ryo_original_app.musicplayer.convenience;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;
import android.widget.TextView;

import ryo_original_app.musicplayer.R;

/**
 * 楽曲再生時間計測クラス
 * シークバー含む
 */
public class MusicTimer {

    /** 1楽曲の今の再生時間 */
    private TextView _tuneNowTime;
    private SeekBar _seekbar;
    /** UI関連操作（ハンドラ） */
    private Handler handler = new Handler(Looper.getMainLooper());
    /** マルチスレッドのタスク（この場合、時間計測） */
    private Runnable timerTask;
    /** 1楽曲の再生時間 */
    private String tuneTotalTime;
    double seekPercentage;
    /** 任意の画面のアクティビティ */
    private Activity activity;


    /**
     * コンストラクタ（初期処理でアクティビティを取得）
     * @param activity 任意の画面のアクティビティ
     */
    public MusicTimer(Activity activity) {
        this.activity = activity;
        _tuneNowTime = activity.findViewById(R.id.tuneNowTime); // UIの再生時間のidを取得
        _seekbar = activity.findViewById(R.id.seekbar); // UIのシークバーのidを取得
    }

    /**
     * 時間計測処理
     * @param mediaPlayer メディアプレイヤー
     */
    public void startTimer(MediaPlayer mediaPlayer) {

        /* タスクが残っていれば初期化 */
        if(timerTask != null) {
            handler.removeCallbacks(timerTask);
        }

        int totalTime = mediaPlayer.getDuration();  // ミリ秒単位で楽曲の総時間を取得
        _seekbar.setMax(totalTime); // シークバーの最大値をミリ秒で設定

        /* マルチスレッド開始 */
        timerTask = new Runnable() {
            @Override
            public void run() {
                /* メディアプレイヤーが起動しており、再生中であれば時間計測 */
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int nowTime = mediaPlayer.getCurrentPosition();     // ミリ秒単位で楽曲の今の時間を取得

                    /* ミリ秒を変換し、文字列の00:00形式に変換 */
                    DataShaping shaping = new DataShaping();
                    tuneTotalTime = shaping.timeFormat(String.valueOf(nowTime));

                    _tuneNowTime.setText(tuneTotalTime);         // 再生時間をUIにセット
                    _seekbar.setProgress(nowTime);  // シークバーの進捗をUIにセット（ミリ秒）
                    handler.postDelayed(this, 1000); // 1秒ごとに更新
                }else{
                    handler.removeCallbacks(this);
                }
            }

        };
        handler.post(timerTask);
    }

    /**
     * アプリDestroy時、バグを防ぐため、タイマーを止める処理
     */
    public void stopTimer() {
        if (timerTask != null) {
            handler.removeCallbacks(timerTask);
            timerTask = null;
        }
    }
}

package ryo_original_app.musicplayer.convenience;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import ryo_original_app.musicplayer.R;

public class MusicTimer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerTask;
    private String tuneTotalTime;
    /** 1楽曲の今の再生時間 */
    private TextView _tuneNowTime;
    private Activity activity;

    public MusicTimer(Activity activity) {
        this.activity = activity;
    }

    public void startTimer(MediaPlayer mediaPlayer) {

        if(timerTask != null) {
            handler.removeCallbacks(timerTask);
        }

        timerTask = new Runnable() {
            @Override
            public void run() {
                _tuneNowTime = activity.findViewById(R.id.tuneNowTime);
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int nowTime = mediaPlayer.getCurrentPosition(); // ミリ秒単位で楽曲の時間を取得

                    DataShaping shaping = new DataShaping();
                    tuneTotalTime = shaping.timeFormat(String.valueOf(nowTime));

                    _tuneNowTime.setText(tuneTotalTime);
                    handler.postDelayed(this, 1000); // 1秒ごとに更新
                }
            }

        };
        handler.post(timerTask);
    }
}

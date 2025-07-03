package ryo_original_app.musicplayer;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MusicList extends AppCompatActivity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* 楽曲リスト画面遷移 */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_list);
    }

    /* バツボタン押下時の処理　メイン画面に戻る */
    public void onMenuCancel(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
    }
}

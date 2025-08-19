package ryo_original_app.musicplayer.screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.List;

import ryo_original_app.musicplayer.R;
import ryo_original_app.musicplayer.constants.Constants;

/**
 * 楽曲リスト表示画面
 */
public class TunesList extends AppCompatActivity implements View.OnClickListener{

    /**
     * 生成処理
     * @param savedInstanceState Activity破棄時インスタンス状態を保存
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* 楽曲リスト画面遷移 */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tunes_list);

        // 前画面から楽曲リストを取得
        Intent intent = getIntent();
        String[] tunesList = intent.getStringArrayExtra("tunesList");

        // 画面定義
        ListView setTunesList = findViewById(R.id.tunesListMenu);
        TextView setTotalTunesNum = findViewById(R.id.tunesTotalNum);

        List<String> list = Arrays.asList(tunesList);

        ArrayAdapter adapter = new ArrayAdapter(TunesList.this,android.R.layout.simple_list_item_1,list);
        setTunesList.setAdapter(adapter);
        setTotalTunesNum.setText(String.format(Constants.tunesListSentence,tunesList.length));
    }

    /**
     * バツボタン押下時の処理　メイン画面に戻る
     * @param v View情報
     */
    public void onMenuCancel(View v) {
        finish();
    }

    /**
     * クリック処理
     * @param v View情報
     */
    @Override
    public void onClick(View v) {
    }
}

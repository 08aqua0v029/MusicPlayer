package ryo_original_app.musicplayer.screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
public class TunesList extends AppCompatActivity implements ListView.OnItemClickListener{

    private ListView _setTunesList;
    private TextView _setTotalTunesNum;

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
        _setTunesList = findViewById(R.id.tunesListMenu);
        _setTotalTunesNum = findViewById(R.id.tunesTotalNum);

        List<String> list = Arrays.asList(tunesList);

        ArrayAdapter adapter = new ArrayAdapter(TunesList.this,android.R.layout.simple_list_item_1,list);
        _setTunesList.setAdapter(adapter);
        _setTotalTunesNum.setText(String.format(Constants.tunesListSentence,tunesList.length));

        /* リスナーのセット */
        _setTunesList.setOnItemClickListener(this);
    }

    /**
     * バツボタン押下時の処理　メイン画面に戻る
     * @param v View情報
     */
    public void onMenuCancel(View v) {
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /* 次画面への準備 */
        Intent intent = new Intent();
        intent.putExtra("selectTunesNum", position);
        setResult(RESULT_OK, intent);
        finish();
    }
}

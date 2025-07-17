package ryo_original_app.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class TunesList extends AppCompatActivity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* 楽曲リスト画面遷移 */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tunes_list);

        // 前画面から楽曲リストを取得
        Intent intent = getIntent();
        String[] tuneList = intent.getStringArrayExtra("tunesList");

        // 画面定義
        ListView setTunesList = findViewById(R.id.tunesListMenu);
        TextView setTotalTunesNum = findViewById(R.id.tunesTotalNum);

        List<String> list = Arrays.asList(tuneList);

        ArrayAdapter adapter = new ArrayAdapter(TunesList.this,android.R.layout.simple_list_item_1,list);
        setTunesList.setAdapter(adapter);
        setTotalTunesNum.setText("楽曲リスト:全" + tuneList.length + "曲");
    }

    /* バツボタン押下時の処理　メイン画面に戻る */
    public void onMenuCancel(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
    }
}

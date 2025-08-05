package ryo_original_app.musicplayer.convenience;

import ryo_original_app.musicplayer.constants.Constants;

/**
 * データ加工クラス
 */
public class DataShaping {

    /**
     * 楽曲時間のフォーマット（00:00形式）
     * @param tuneTotalTime ミリ秒形式の楽曲時間
     * @return フォーマット後の時間
     */
    public String timeFormat(String tuneTotalTime){
        int secTmp = Integer.parseInt(tuneTotalTime) / 1000;    // ミリ秒を秒に
        int min = (secTmp % 3600) / 60;            // 秒を分化し分のみを抽出
        int sec = secTmp % 60;                     // 秒を60で割った余りが分を除いた秒になる

        /* 文字列形式の定義 */
        String tuneTotalTimeMin;
        String tuneTotalTimeSec;

        /* 10分未満であれば、十の位に0を加える */
        if(min < 10){
            tuneTotalTimeMin = "0" + String.valueOf(min);
        } else {
            tuneTotalTimeMin = String.valueOf(min);
        }
        /* 10秒未満であれば、十の位に0を加える */
        if(sec < 10){
            tuneTotalTimeSec = "0" + String.valueOf(sec);
        } else {
            tuneTotalTimeSec = String.valueOf(sec);
        }

        /* 文字列形式への整形 */
        tuneTotalTime = (tuneTotalTimeMin + Constants.colonString + tuneTotalTimeSec);    // 時間を整形
        return tuneTotalTime;
    }
}

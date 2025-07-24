package ryo_original_app.musicplayer.constants;

import ryo_original_app.musicplayer.screen.TunesList;

/**
 * 定数クラス
 */
public class Constants {
    /* URI関連 */
        /** ローカル環境のURI */
        /*
         * adb -s ZY22GXG7JX reverse tcp:8000 tcp:8000
         * でAndroid実機でもローカルアクセスが簡単にできる
         * ZY22GXG7JXは端末シリアル名
         */
        public static final String localApiUri = "http://localhost:8000/logApi.php";
        /** 本番環境のURI */
        public static final String ApiUri = "";

    /* フォーマット */
        /** フォーマットとした日時 */
        public static final String formatDateTime = "yyyy-MM-dd HH:mm:ss";

    /* 正規表現 */
        /** 正規表現用String mp3 */
        public static final String mp3String = ".mp3";
        /** 正規表現用String wav */
        public static final String wavString = ".wav";
        /** 正規表現用String m4a */
        public static final String m4aString = ".m4a";

    /* 文章、String */
        /** コロンString */
        public static final String colonString = ":";
        /** スラッシュ */
        public static final String slashString = "/";
        /** 楽曲等時間初期値 */
        public static final String initialTime = "00:00";
        /** パーミッション許可文 */
        public static final String permissionSentence = "権限が許可されました";
        /** パーミッション非許可文 */
        public static final String unauthorizedSentence = "音楽とオーディオの権限を許可してください";
        /** 再生エラー文 */
        public static final String playErrorSentence = "再生エラー: ";
        /** 楽曲リスト曲数表示テキスト */
        public static final String tunesListSentence = "楽曲リスト:全 %s曲";

    /* フォルダ名、ファイル名 */
        /** 音楽保管フォルダ */
        public static final String musicFolder = "/Music/";
        /** ログ保管フォルダ */
        public static final String logFolder = "log";
        /** ログファイル */
        public static final String logFile = "crash_log.json";

    /* クラス名 */
        /** クラス名:MainActivity */
        public static final String mainActivityClass = "MainActivity";
        /** クラス名:TunesList */
        public static final String tunesListClass = "TunesList";

    /* ログ関連 */
        /** クラッシュハンドラ */
        public static final String crashHandler = "CrashHandler";
        /** エラー内容ログ用キー */
        public static final String errorTypeLogKey = "エラー内容";
        /** エラー詳細ログ用キー */
        public static final String errorDetailsLogKey = "クラッシュ箇所";
        /** クラッシュ画面ログ用キー */
        public static final String crashLocationLogKey = "クラッシュ箇所";
        /** クラッシュ時刻ログ用キー */
        public static final String crashTimeLogKey = "Crashed time";
        /** 機種情報ログ用キー */
        public static final String modelLogKey = "Build.MODEL";
        /** OSバージョンログ用キー */
        public static final String osVerLogKey = "Build.VERSION.BASE_OS:Android";
}

package ryo_original_app.musicplayer.constants;

/**
 * 定数クラス
 */
public class Constants {
    /* クラッシュログAPI */
        /** ローカル環境のURI */
        public static final String ApiUri = "http://localhost:8000/crashLogApi.php";
        /** 本番環境のURI */
        // public static final String ApiUri = "";

    /* BASIC認証用 */
    /* TODO:一定の機能実装後、ユーザー、パスワードの管理方法を検討し直す */
    /* TODO:.env ファイルでの保管で、gitには公開させない  */
        /** クラッシュログ用ユーザー名 */
        public static final String crashLogBasicUser = "u6Kg5t2c";
        /** クラッシュログ用パスワード */
        public static final String crashLogBasicPass = "Zp6uaUzY";

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
        /** ネットワーク */
        public static final String networkString = "Network";
        /** 成功タグ */
        public static final String successTag = "SUCCESS";
        /** エラータグ */
        public static final String errorTag = "ERROR";
        /** インフォメーションタグ */
        public static final String infoTag = "INFO";
        /** レスポンス */
        public static final String responseString = "response";
        /** コンテンツタイプ */
        public static final String contentTypeString = "contentType";
        /** サーバーステータス */
        public static final String serverStatusString = "serverStatus";
        /** パーミッション許可文 */
        public static final String permissionSentence = "権限が許可されました";
        /** パーミッション非許可文 */
        public static final String unauthorizedSentence = "音楽とオーディオの権限を許可してください";
        /** 楽曲データがない場合のエラー文 */
        public static final String nonMusicDate = "楽曲が存在しないため、各種機能が使えません";
        /** 再生エラー文 */
        public static final String playErrorSentence = "再生エラー: ";
        /** 楽曲リスト曲数表示テキスト */
        public static final String tunesListSentence = "楽曲リスト:全 %s曲";
        /** サーバーエラー文 */
        public static final String severErrorSentence = "サーバー停止などの理由で転送不可";
        /** ネットワーク未接続文 */
        public static final String nonNetwork = "ネットワークに接続されていません";
        /** ログファイルが存在しない場合の文 */
        public static final String nonLogFile = "ログファイルが存在しません";

    /* フォルダ名、ファイル名 */
        /** 音楽保管フォルダ */
        public static final String musicFolder = "/Music/";
        /** ログ保管フォルダ */
        public static final String logFolder = "log";
        /** ログファイル */
        public static final String crashLogFile = "crash_log.json";

    /* クラス名 */
        /** クラス名:MainActivity */
        public static final String mainActivityClass = "MainActivity";
        /** クラス名:TunesList */
        public static final String tunesListClass = "TunesList";

    /* IDなどの通称 */
        public static final String notificationId = "sound_notification_id";

    /* ログ関連 */
        /** クラッシュハンドラ */
        public static final String crashHandler = "crashHandler";
        /** エラー内容ログ用キー */
        public static final String errorTypeLogKey = "crashType";
        /** エラー詳細ログ用キー */
        public static final String errorDetailsLogKey = "crashDetails";
        /** クラッシュ画面ログ用キー */
        public static final String crashLocationLogKey = "crashLocation";
        /** クラッシュ時刻ログ用キー */
        public static final String crashTimeLogKey = "crashedTime";
        /** 機種情報ログ用キー */
        public static final String modelLogKey = "buildModel";
        /** OSバージョンログ用キー */
        public static final String osVerLogKey = "buildOsVersion";
}

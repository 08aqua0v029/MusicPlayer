package ryo_original_app.musicplayer.constants;

import ryo_original_app.musicplayer.screen.TunesList;

/**
 * 定数クラス
 */
public class Constants {
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

    /* フォルダ名 */
        /** 音楽保管フォルダ */
        public static final String musicFolder = "/Music/";

    /* クラス名 */
        /** クラス名:MainActivity */
        public static final String mainActivityClass = "MainActivity";
        /** クラス名:TunesList */
        public static final String tunesListClass = "TunesList";

    /* ログ関連 */
        /** クラッシュハンドラログ用タグ */
        public static final String crashHandlerLogTag = "クラッシュハンドラ";
        /** クラッシュハンドラログ用メッセージ */
        public static final String crashHandlerLogMsg = "Uncaught Exception:";
        /** クラッシュ画面ログ用タグ */
        public static final String crashScreenLogTag = "クラッシュした画面";
        /** クラッシュ画面ログ用メッセージ */
        public static final String crashScreenLogMsg = "App crashed on screen:";
        /** クラッシュ時刻ログ用タグ */
        public static final String crashTimeLogTag = "クラッシュ時刻";
        /** クラッシュ時刻ログ用メッセージ */
        public static final String crashTimeLogMsg = "Crashed time:";
        /** 機種情報ログ用タグ */
        public static final String modelLogTag = "機種情報";
        /** 機種情報ログ用メッセージ */
        public static final String modelLogMsg = "Build.MODEL:";
        /** OSバージョンログ用タグ */
        public static final String osVerLogTag = "OS情報";
        /** OSバージョンログ用メッセージ */
        public static final String osVerLogMsg = "Build.VERSION.BASE_OS:Android";
}

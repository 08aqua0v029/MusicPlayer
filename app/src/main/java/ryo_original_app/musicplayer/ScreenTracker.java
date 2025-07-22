package ryo_original_app.musicplayer;

/**
 * 画面名を保存するクラス
 */
public class ScreenTracker {
    /** 現在の画面 */
    private static String currentScreen = "---";

    /**
     * 現在の画面名を保管する
     * @Param screenName 画面名
     */
    public static void setCurrentScreen(String screenName) {
        currentScreen = screenName;
    }

    /**
     * 現在の画面名を取得する
     * @Return currentScreen 現在の画面名
     */
    public static String getCurrentScreen() {
        return currentScreen;
    }
}

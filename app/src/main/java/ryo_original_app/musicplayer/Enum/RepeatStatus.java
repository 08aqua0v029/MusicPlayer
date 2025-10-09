package ryo_original_app.musicplayer.Enum;

public enum RepeatStatus {
    // 0:リピート無効　1:全曲リピート　2:1曲リピート
    NO_REPEAT("リピート無効", 0),
    ALL_REPEAT("全曲リピート", 1),
    ONE_REPEAT("1曲リピート", 2);

    private final String label;
    private final int id;

    private RepeatStatus(String label, int id) {	//コンストラクタはprivateで宣言
        this.label = label;
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public int getId() {
        return id;
    }
}

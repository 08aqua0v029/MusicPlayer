package ryo_original_app.musicplayer.Enum;

public enum ShuffleStatus {
    // 0:シャッフル無効　1:シャッフル有効
    NO_SHUFFLE("シャッフル無効", 0),
    SHUFFLE("シャッフル有効", 1);

    private final String label;
    private final int id;

    private ShuffleStatus(String label, int id) {	//コンストラクタはprivateで宣言
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

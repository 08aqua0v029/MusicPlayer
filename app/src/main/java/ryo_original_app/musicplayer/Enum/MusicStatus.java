package ryo_original_app.musicplayer.Enum;

public enum MusicStatus {
    // 0:停止　1:再生　2:一時停止
    STOP("停止", 0),
    START("再生", 1),
    PAUSE("一時停止", 2);

    private final String label;
    private final int id;


    private MusicStatus(String label, int id) {	//コンストラクタはprivateで宣言
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

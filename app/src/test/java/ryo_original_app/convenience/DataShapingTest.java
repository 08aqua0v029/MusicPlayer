package ryo_original_app.convenience;

import static org.junit.Assert.assertThrows;
import junit.framework.TestCase;

import ryo_original_app.musicplayer.convenience.DataShaping;

public class DataShapingTest extends TestCase {

    /** 正常系テスト */
    public void testNormalTimeFormat() {
        DataShaping dataShaping = new DataShaping();

        /*期待値と実測値が等しいか確認*/
        assertEquals("00:00", dataShaping.timeFormat("0"));
        assertEquals("00:01", dataShaping.timeFormat("1000"));
        assertEquals("00:59", dataShaping.timeFormat("59000"));
        assertEquals("01:00", dataShaping.timeFormat("60000"));
        assertEquals("01:35", dataShaping.timeFormat("95000"));
    }

    /** 異常系テスト */
    public void testAbnormalTimeFormat() {
        DataShaping dataShaping = new DataShaping();

        /* 異常系チェック */
        assertThrows(NumberFormatException.class, () -> {
            dataShaping.timeFormat(null);
        });
        assertThrows(NumberFormatException.class, () -> {
            dataShaping.timeFormat("abc");
        });
    }
}
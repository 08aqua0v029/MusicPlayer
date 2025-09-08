package ryo_original_app.musicplayer.convenience;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import java.util.Objects;

public class NetworkConnect {
    /**
     * ネットワーク接続状態チェック
     * @return 通信が可能な状態 or false
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        if (Objects.isNull(cm)) return false;

        /* アクティブな（Wifi、モバイル）通信のチェック */
        android.net.Network network = cm.getActiveNetwork();
        if (Objects.isNull(network)) return false;

        /* 今まで取ってきたものは通信できるかどうかのチェック */
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}

package ryo_original_app.convenience;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import static org.mockito.Mockito.*;
import junit.framework.TestCase;

import org.junit.Test;

import ryo_original_app.musicplayer.convenience.NetworkConnect;

public class NetworkConnectTest extends TestCase {
    public void testTrueCaseIsConnected() {
        Context context = mock(Context.class);
        ConnectivityManager cm = mock(ConnectivityManager.class);
        Network network = mock(Network.class);
        NetworkCapabilities capabilities = mock(NetworkCapabilities.class);

        when(context.getSystemService(ConnectivityManager.class)).thenReturn(cm);
        when(cm.getActiveNetwork()).thenReturn(network);
        when(cm.getNetworkCapabilities(network)).thenReturn(capabilities);
        when(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true);

        assertTrue(NetworkConnect.isConnected(context));
    }

    public void testFalseCaseCapabilitiesIsConnected() {
        Context context = mock(Context.class);
        ConnectivityManager cm = mock(ConnectivityManager.class);
        Network network = mock(Network.class);

        when(context.getSystemService(ConnectivityManager.class)).thenReturn(cm);
        when(cm.getActiveNetwork()).thenReturn(network);
        when(cm.getNetworkCapabilities(network)).thenReturn(null);

        assertFalse(NetworkConnect.isConnected(context));
    }

    public void testFalseCaseNetworkIsConnected() {
        Context context = mock(Context.class);
        ConnectivityManager cm = mock(ConnectivityManager.class);

        when(context.getSystemService(ConnectivityManager.class)).thenReturn(cm);
        when(cm.getActiveNetwork()).thenReturn(null);

        assertFalse(NetworkConnect.isConnected(context));
    }
}
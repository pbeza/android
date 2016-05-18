package pl.edu.pw.mini.intercom.connection.socket;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class EchoServiceConnection implements ServiceConnection {

    private EchoService echoService;

    @Override
    public void onServiceConnected(ComponentName className, IBinder binder) {
        echoService = ((EchoService.EchoServiceBinder) binder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        echoService = null;
    }

    public EchoService getEchoService() {
        return echoService;
    }
}
package pl.edu.pw.mini.intercom.connection.socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class EchoServiceMessageQueueHandler extends Handler {
    public void handleMessage(Message message) {
        Bundle data = message.getData();
    }
}
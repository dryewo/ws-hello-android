package dryewo.ws_hello_android.app;

import android.os.Handler;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;

public class ReconnectingSocket {
    private volatile WebSocket webSocket;
    private volatile boolean paused;

    private final String url;
    private final int timeoutMs;
    private final StringCallback stringCallback;

    private final Handler handler = new Handler();

    private volatile StatusCallback statusCallback;

    public static final int CLOSED = 0;
    public static final int OPEN = 1;

    public interface StringCallback {
        public void onString(String string);
    }

    public interface StatusCallback {
        public void onStatus(int status);
    }

    public void setStatusCallback(StatusCallback statusCallback) {
        this.statusCallback = statusCallback;
    }

    public ReconnectingSocket(String url, int timeoutMs, StringCallback stringCallback) {
        this.url = url;
        this.timeoutMs = timeoutMs;
        this.stringCallback = stringCallback;
    }

    public boolean send(String str) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.send(str);
            return true;
        }
        return false;
    }

    public void resume() {
        paused = false;
        reconnect.run();
    }

    public void pause() {
        paused = true;
        handler.removeCallbacks(reconnect);
        webSocket.close();
        webSocket = null;
    }

    private Runnable reconnect = new Runnable() {
        @Override
        public void run() {
            AsyncHttpClient.getDefaultInstance().websocket(new AsyncHttpGet(url),
                    null, new AsyncHttpClient.WebSocketConnectCallback() {
                        @Override
                        public void onCompleted(Exception ex, WebSocket ws) {
                            if (ex != null) {
                                handler.postDelayed(reconnect, timeoutMs);
                                return;
                            }

                            webSocket = ws;
                            webSocket.setStringCallback(new WebSocket.StringCallback() {
                                public void onStringAvailable(final String s) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (stringCallback != null)
                                                stringCallback.onString(s);
                                        }
                                    });
                                }
                            });
                            webSocket.setClosedCallback(new CompletedCallback() {
                                public void onCompleted(Exception ex) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (statusCallback != null)
                                                statusCallback.onStatus(CLOSED);
                                        }
                                    });
                                    if (!paused)
                                        handler.postDelayed(reconnect, timeoutMs);
                                }
                            });
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (statusCallback != null)
                                        statusCallback.onStatus(OPEN);
                                }
                            });
                        }
                    }
            );
        }

    };
}

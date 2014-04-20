package dryewo.ws_hello_android.app;

import android.os.Handler;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;

public class ReconnectingSocket {
    private WebSocket webSocket;
    private Handler handler = new Handler();

    private final String url;
    private final int timeoutMs;
    private final StringCallback callback;

    public interface StringCallback {
        public void onString(String string);
    }

    public ReconnectingSocket(String url, int timeoutMs, StringCallback callback) {
        this.url = url;
        this.timeoutMs = timeoutMs;
        this.callback = callback;
    }

    public boolean send(String str) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.send(str);
            return true;
        }
        return false;
    }

    public void resume() {
        reconnect.run();
    }

    public void pause() {
        webSocket.close();
        webSocket = null;
        handler.removeCallbacks(reconnect);
    }

    private Runnable reconnect = new Runnable() {
        @Override
        public void run() {
            Log.i(MainActivity.TAG, "Reconnecting");
            AsyncHttpClient.getDefaultInstance().websocket(new AsyncHttpGet(url),
                    null, new AsyncHttpClient.WebSocketConnectCallback() {
                        @Override
                        public void onCompleted(Exception ex, WebSocket ws) {
                            Log.i(MainActivity.TAG, "WebSocketConnectCallback: " + ex + ", " + ws);
                            if (ex != null) {
                                handler.postDelayed(reconnect, timeoutMs);
                                return;
                            }

                            webSocket = ws;
                            webSocket.setStringCallback(new WebSocket.StringCallback() {
                                public void onStringAvailable(String s) {
                                    if (callback != null)
                                        callback.onString(s);
                                }
                            });
                            webSocket.setClosedCallback(new CompletedCallback() {
                                public void onCompleted(Exception ex) {
                                    Log.i(MainActivity.TAG, "ClosedCallback");
                                    handler.postDelayed(reconnect, timeoutMs);
                                }
                            });
                        }
                    }
            );
        }

    };
}

package dryewo.ws_hello_android.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private ReconnectingSocket socket = new ReconnectingSocket("http://192.168.1.187:4040/write", 1000,
            new ReconnectingSocket.StringCallback() {
                @Override
                public void onString(String string) {
                    Log.i(TAG, "Got string: " + string);
                }
            }
    );

    @Override
    protected void onResume() {
        super.onResume();
        socket.resume();
    }

    @Override
    protected void onPause() {
        socket.pause();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_test) {
            test();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void test() {
        socket.send("bingo");
    }
}

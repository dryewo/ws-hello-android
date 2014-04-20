package dryewo.ws_hello_android.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;


public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    private TextView readerCountText;
    private ImageView connectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readerCountText = (TextView) findViewById(R.id.readerCount);
        connectionStatus = (ImageView) findViewById(R.id.connectionStatus);

        socket.setStatusCallback(new ReconnectingSocket.StatusCallback() {
            private final Drawable openImage = new IconDrawable(MainActivity.this, Iconify.IconValue.fa_check)
                    .actionBarSize().color(Color.GREEN);
            private final Drawable closedImage = new IconDrawable(MainActivity.this, Iconify.IconValue.fa_times)
                    .actionBarSize().color(Color.RED);

            @Override
            public void onStatus(int status) {
                if (status == ReconnectingSocket.CLOSED) {
                    connectionStatus.setImageDrawable(closedImage);
                    if (readerCountText != null)
                        readerCountText.setText("");
                } else {
                    connectionStatus.setImageDrawable(openImage);
                }
            }
        });
    }

    private ReconnectingSocket socket = new ReconnectingSocket("http://192.168.1.187:4040/write", 1000,
            new ReconnectingSocket.StringCallback() {
                @Override
                public void onString(String string) {
                    Log.i(TAG, "Got string: " + string);
                    if (readerCountText != null)
                        readerCountText.setText(string);
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

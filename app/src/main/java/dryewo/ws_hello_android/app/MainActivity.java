package dryewo.ws_hello_android.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    private TextView readerCountText;
    private ImageView connectionStatus;
    private EditText editText;
    private TextView locationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readerCountText = (TextView) findViewById(R.id.readerCount);
        connectionStatus = (ImageView) findViewById(R.id.connectionStatus);
        editText = (EditText) findViewById(R.id.editText);
        locationText = (TextView) findViewById(R.id.locationText);

        final Location lastLocation = getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null)
            locationText.setText(formatLocation(lastLocation));

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (socket.send(v.getText().toString()))
                    v.setText("");
                return true;
            }
        });

        socket.setStatusCallback(new ReconnectingSocket.StatusCallback() {
            private final Drawable openImage = new IconDrawable(MainActivity.this, Iconify.IconValue.fa_check)
                    .actionBarSize().color(0xFF008000);
            private final Drawable closedImage = new IconDrawable(MainActivity.this, Iconify.IconValue.fa_times)
                    .actionBarSize().color(0xFF800000);

            @Override
            public void onStatus(int status) {
                if (status == ReconnectingSocket.CLOSED) {
                    connectionStatus.setImageDrawable(closedImage);
                    readerCountText.setText("");
                } else {
                    connectionStatus.setImageDrawable(openImage);
                }
            }
        });
    }

    private ReconnectingSocket socket = new ReconnectingSocket("http://ws-hello.herokuapp.com/write", 1000,
            new ReconnectingSocket.StringCallback() {
                @Override
                public void onString(String string) {
                    Log.i(TAG, "Got string: " + string);
                    readerCountText.setText(string);
                }
            }
    );

    private String formatLocation(Location location) {
        final DateFormat fmt = new SimpleDateFormat("HH:mm:ss.SS");
        final String locationTime = fmt.format(new Date(location.getTime()));
        final String locationStr = String.format("(%.6f;%.6f) %s", location.getLatitude(), location.getLongitude(), locationTime);
        return locationStr;
    }

    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            final String locationStr = formatLocation(location);
            Log.i(TAG, "Location: " + locationStr);
            locationText.setText(locationStr);
            socket.send(locationStr);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    private LocationManager getLocationManager() {
        return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private void startLocationWatch() {
        getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
    }

    private void stopLocationWatch() {
        getLocationManager().removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        socket.resume();
        startLocationWatch();
    }

    @Override
    protected void onPause() {
        stopLocationWatch();
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

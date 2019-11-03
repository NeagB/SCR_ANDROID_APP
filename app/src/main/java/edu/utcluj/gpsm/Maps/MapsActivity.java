package edu.utcluj.gpsm.Maps;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import edu.utcluj.gpsm.Login.LoginActivity;
import edu.utcluj.gpsm.R;
import edu.utcluj.gpsm.Register.RegisterActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private TextView tvLongitude, tvLatitude, tvTerminalId;

    private Button btnSend, btnSet;

    private Double latitude = 0.0, longitude = 0.0;

    private String latitudeString;
    private String longitudeString;
    private String terminalId;


    private EditText etLongitude, etLatitude;

    private Executor executor = Executors.newFixedThreadPool(1);
    private volatile Handler msgHandler;

    private static final String STATIC_LOCATION = "{" +
            "\"terminalId\":\"%s\"," +
            "\"latitude\":\"%s\"," +
            "\"longitude\":\"%s\"" +
            "}";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnSet = (Button)findViewById(R.id.btnSet);
        btnSet.setOnClickListener(this);

        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        tvTerminalId = (TextView) findViewById(R.id.tvTerminalId);
        tvTerminalId.setText("Terminal Id: "+Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID));
        tvLatitude.setText("Latitude ");
        tvLongitude.setText("Longitude ");

        etLatitude = (EditText) findViewById(R.id.etLatitude);
        etLongitude = (EditText) findViewById(R.id.etLongitude);

        msgHandler = new MapsActivity.MsgHandler(this);


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(15);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(true);




        LatLng cluj = new LatLng(46.795593, 23.522238);
        mMap.addMarker(new MarkerOptions().position(cluj).title("Marker in Cluj"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cluj));




}

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSend: {
                terminalId = tvTerminalId.getText().toString();
                longitudeString = etLongitude.getText().toString();
                latitudeString = etLatitude.getText().toString();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = msgHandler.obtainMessage();
                        msg.arg1 = doSend(terminalId,longitudeString,latitudeString)?1:0;
                        msgHandler.sendMessage(msg);

                    }
                });
            }
                break;
                case R.id.btnSet:{

                    mMap.setMinZoomPreference(15);
                    latitude = Double.parseDouble(etLatitude.getText().toString());
                    longitude = Double.parseDouble(etLongitude.getText().toString());
                    LatLng position = new LatLng(latitude,longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(position);
                    markerOptions.draggable(true);
                    markerOptions.title("You want here");
                    mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                }
                break;

        }

    }

    private boolean doSend(String terminalId, String longitudeString, String latitudeString) {

        HttpURLConnection connection = null;
        try {
            String BASE_URL = "http://192.168.0.103:8082/position/1/addPos";
            URL url = new URL(BASE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            os.write(String.format(STATIC_LOCATION, terminalId,longitudeString,latitudeString).getBytes());
            os.flush();
            os.close();
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader((new InputStreamReader(connection.getInputStream())));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return false;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static class MsgHandler extends Handler {
        private final WeakReference<Activity> sendActivity;

        public MsgHandler(Activity activity) {
            sendActivity = new WeakReference<>(activity);
        }

        public void handleMessage(Message msg) {
            if (msg.arg1 == 1) {
                Toast.makeText(sendActivity.get().getApplicationContext(),
                        "Success!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(sendActivity.get().getApplicationContext(),
                        "Error!", Toast.LENGTH_LONG).show();
            }
        }
    }
}

package edu.utcluj.gpsm.Maps;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.InetAddresses;
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
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import edu.utcluj.gpsm.Login.LoginActivity;
import edu.utcluj.gpsm.R;
import edu.utcluj.gpsm.Register.RegisterActivity;
import edu.utcluj.gpsm.RestClient.ResPosition;
import edu.utcluj.gpsm.RestClient.dto.PositionDTO;
import edu.utcluj.gpsm.RestClient.retrofit.client.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private TextView tvLongitude, tvLatitude, tvTerminalId;

    private Button btnSend, btnSet;

    private Double latitude = 0.0, longitude = 0.0;

    private String latitudeString;
    private String longitudeString;
    private String terminalId;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private EditText etLongitude, etLatitude, etEmailTerminal;




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

        tvLatitude.setText("Latitude ");
        tvLongitude.setText("Longitude ");



        etLatitude = (EditText) findViewById(R.id.etLatitude);
        etLongitude = (EditText) findViewById(R.id.etLongitude);




        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            String email = extras.getString("Email");
            tvTerminalId.setText(email);
        }


        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                LatLng position = new LatLng(latitude,longitude);

                mMap.addMarker(new MarkerOptions().position(position).title("You might be here!"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));

                etLongitude.setText(String.valueOf(location.getLongitude()));
                etLatitude.setText(String.valueOf(location.getLatitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        CheckPermission();


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setMinZoomPreference(15);
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.draggable(true);
//
//
//
//
//        LatLng cluj = new LatLng(46.795593, 23.522238);
//        etLatitude.setText("46.795593");
//        etLongitude.setText("23.52238");
//        latitude = Double.parseDouble(etLatitude.getText().toString());
//        longitude = Double.parseDouble(etLongitude.getText().toString());
//
//        mMap.addMarker(new MarkerOptions().position(cluj).title("Marker in Cluj"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(cluj));




}

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSend: {
                terminalId = tvTerminalId.getText().toString();
                longitudeString = etLongitude.getText().toString();
                latitudeString = etLatitude.getText().toString();
                doSend(terminalId,longitudeString,latitudeString);
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



    private void doSend(String terminalId, String longitudeString, String latitudeString) {

        PositionDTO positionDTO = new PositionDTO();
        positionDTO.setLatitude(latitudeString);
        positionDTO.setLongitude(longitudeString);
        positionDTO.setTerminalId(terminalId);

        String BASE_URL = "http://192.168.0.102:8082/";

        RetrofitClient client = new RetrofitClient(BASE_URL);
        Bundle exstras = getIntent().getExtras();
        String credentials = exstras.getString("credentials");

        client.getUserService().addPostion(positionDTO,credentials).enqueue(new Callback<ResPosition>() {
            @Override
            public void onResponse(Call<ResPosition> call, Response<ResPosition> response) {
                if (response.code() == 200) {
                    if (response.isSuccessful()) {
                        ResPosition resPosition = (ResPosition) response.body();
                        Toast.makeText(MapsActivity.this, "Succes! Position saved !", Toast.LENGTH_LONG).show();
                    }}
                else {
                        Toast.makeText(MapsActivity.this, "Error"+response.code(), Toast.LENGTH_SHORT).show();
                    }
            }

            @Override
            public void onFailure(Call<ResPosition> call, Throwable t) {
                Toast.makeText(MapsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }



    public void CheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.INTERNET}, 1);
            return;
        }else {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }


}

package com.absolute.maps_demo;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static android.Manifest.permission.CALL_PHONE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    Double lat = 0.0;
    Double lon = 0.0;
    Double proporsion = 2.0;
    Double lat_dest = 0.0;
    Double lon_dest = 0.0;
    Boolean your_position = false;
    int knn = 3;
    ProgressBar indicator;
    public static String AssetJSONFile (String filename, Context context) throws IOException {
        AssetManager manager = context.getAssets();
        InputStream file = manager.open(filename);
        byte[] formArray = new byte[file.available()];
        file.read(formArray);
        file.close();

        return new String(formArray);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        final Button select = (Button) findViewById(R.id.select_button);
        final Button sos = (Button) findViewById(R.id.sos_btn);
        indicator = (ProgressBar) findViewById(R.id.indicator);
        mapFragment.getMapAsync(this);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                your_position = true;
                select.setVisibility(View.INVISIBLE);
                Toast.makeText(MapsActivity.this, "Выберите пункт назначения", Toast.LENGTH_LONG).show();

            }

        });
        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:112"));
                if (ContextCompat.checkSelfPermission(MapsActivity.this, CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                } else {
                    requestPermissions(new String[]{CALL_PHONE}, 1);
                }

            }

        });
        Toast.makeText(MapsActivity.this, "Выберите Ваше местоположение", Toast.LENGTH_LONG).show();

    }

    public class PointData {
        private Double dest;
        private String label;
        public void set_dest(Double new_dest){
            dest = new_dest;
        }
        public void set_label(String new_label){
            label = new_label;
        }
        public Double get_dest(){
            return dest;
        }
        public String get_label(){
            return  label;
        }


        // standard getters and setters
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng point) {
                // Drawing marker on the map
                try {
                    mMap.clear();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(point);
                    JSONObject jObject = new JSONObject(AssetJSONFile("data.json", MapsActivity.this));
                    Iterator keys = jObject.keys();
                    String title = "Безопасно";
                    LinkedList<PointData> points = new LinkedList<>();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        JSONArray v2 = jObject.getJSONArray(key);
                        double dist = 0.0;
                        double p = 2.0;
                        double[] v1 = {point.latitude, point.longitude};

                        for (int i = 0; i < v1.length; i++) {
                            dist += Math.pow(Math.abs((v1[i]) - (v2.getDouble(i))), p);
                        }
                        dist = Math.pow(dist, 1 / p);
                        String label = "Безопасно";
                        if (v2.getDouble(2) > 500 || v2.getDouble(3) > 500) {
                            label = "Опасно";
                        }
                        LatLng p_m = new LatLng(v2.getDouble(0), v2.getDouble(1));
                        if(label.equals("Безопасно")) {
                            mMap.addMarker(new MarkerOptions().position(p_m).title(key).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        }
                        else{
                            mMap.addMarker(new MarkerOptions().position(p_m).title(key).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                        PointData p_data = new PointData();
                        p_data.set_dest(dist);
                        p_data.set_label(label);
                        points.add(p_data);

                    }

                    //sort
                    int n = points.size();
                    PointData temp;
                    for(int i=0; i < n; i++){
                        for(int j=1; j < (n-i); j++){
                            if(points.get(j-1).get_dest() > points.get(j).get_dest()){
                                //swap elements
                                temp = points.get(j-1);
                                points.set(j-1, points.get(j));
                                points.set(j, temp);
                            }

                        }
                    }
                    for (int i = 0; i < points.size(); i++) {

                        Log.d(String.valueOf(i), points.get(i).get_dest().toString());

                    }
                    int dang = 0;
                    for (int i = 0; i < knn; i++) {
                        PointData s_p = points.get(i);
                        String label = s_p.label;
                        Log.d(String.valueOf(i), s_p.get_dest().toString()+": "+label);
                        if (label.equals("Опасно")) {
                            dang += 1;
                        }
                    }
                    if (dang > knn / proporsion) {
                        title = "Опасно";
                    }
                    if (your_position) {

                        LatLng you = new LatLng(lat, lon);
                        mMap.addMarker(new MarkerOptions().position(you).title("Ваша позиция").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        lat_dest = point.latitude;
                        lon_dest = point.longitude;








                        markerOptions.title("Место назначения: " + title);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(point));
                        mMap.addMarker(markerOptions);

                    } else {
                        lat = point.latitude;
                        lon = point.longitude;
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        markerOptions.title("Ваша позиция:"+ title);

                        mMap.animateCamera(CameraUpdateFactory.newLatLng(point));
                        mMap.addMarker(markerOptions);
                    }

                }
                catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        LocationListener locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onLocationChanged(Location location) {
                // TODO Auto-generated method stub
                if(lat == 0.0) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    LatLng you = new LatLng(lat, lon);
                    mMap.addMarker(new MarkerOptions().position(you).title("Ваша позиция").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(you));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(you,50));
                    indicator.setVisibility(View.INVISIBLE);
                }

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("not granted", "location not allowed by user");
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, locationListener);
        }

    }







}
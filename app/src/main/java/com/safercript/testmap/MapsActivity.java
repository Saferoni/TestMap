package com.safercript.testmap;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.safercript.testmap.entity.LatLngAddress;
import com.safercript.testmap.entity.ListAddresses;
import com.safercript.testmap.fragments.AddressListFragment;
import com.safercript.testmap.utils.DirectionsJSONParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener,
        AddressListFragment.OnListFragmentInteractionListener{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final String[] NAME_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};
    public static final String MAP_MARKER_FROM = "From";
    public static final String MAP_MARKER_TO = "To";
    public static final int ADDRESS_FROM = 0;
    public static final int ADDRESS_TO = 1;

    private GoogleMap mMap;

    private ImageView mButtonLocation;
    private ImageView mButtonPosition;

    private EditText edFrom;
    private EditText edTo;
    private ImageView mButtonFrom;
    private ImageView mButtonTo;
    private Button mButtonDirection;

    private MyDirection myDirection;
    private Snackbar snackbar;
    private SharedPreferences mPrefs;
    private Fragment fragment;
    private LinkedList<LatLngAddress> latLngAddresses = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPrefs = getPreferences(MODE_PRIVATE);

        mButtonLocation = (ImageView) findViewById(R.id.imgMyLocationButton);
        mButtonPosition = (ImageView) findViewById(R.id.imgPositionButton);
        edFrom = (EditText) findViewById(R.id.etFrom);
        edTo = (EditText) findViewById(R.id.etTo);
        mButtonFrom = (ImageView) findViewById(R.id.ivFrom);
        mButtonTo = (ImageView) findViewById(R.id.ivTo);
        mButtonDirection = (Button) findViewById(R.id.buttonDirection);
        mButtonLocation.setOnClickListener(this);
        mButtonPosition.setOnClickListener(this);
        mButtonFrom.setOnClickListener(this);
        mButtonTo.setOnClickListener(this);
        mButtonDirection.setOnClickListener(this);

        myDirection = new MyDirection();

        //Test DATA
//        for (int i = 0; i < 10; i++) {
//            LatLngAddress latLngAddress = new LatLngAddress(i + 50, i + 30, "Address " + i);
//            latLngAddresses.add(latLngAddress);
//        }
        latLngAddresses = getListFromSharedPref();
    }

    @Override
    protected void onResume() {
        super.onResume();
        edFrom.setFocusable(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapClickListener(this);
        camera(12, null);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (edFrom.hasFocus()){
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(MAP_MARKER_FROM));
            myDirection.setAddressFrom(new LatLngAddress(
                    marker.getPosition().latitude,
                    marker.getPosition().longitude,
                    getAddressFromMarker(marker)));
        }
        if (edTo.hasFocus()){
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(MAP_MARKER_TO));
            myDirection.setAddressTo(new LatLngAddress(
                    marker.getPosition().latitude,
                    marker.getPosition().longitude,
                    getAddressFromMarker(marker)));
        }
        updateMap();
    }

    private String getAddressFromMarker(Marker marker){
        String filterAddress = "No address";
        Geocoder geoCoder = new Geocoder(this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);

            if (addresses.isEmpty()){
                filterAddress = marker.getPosition().latitude +", " + marker.getPosition().longitude;
                return filterAddress;
            }else {
                if (addresses.size() > 0) {
                    filterAddress = addresses.get(0).getAddressLine(0);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return filterAddress;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgMyLocationButton:
                showSnackbarPermission();
                break;
            case R.id.imgPositionButton:
                if (myDirection.getAddressFrom().getLatitude() != 0){
                    camera(15, myDirection.getLatLngFrom());
                }else {
                    Toast.makeText(this, R.string.not_start_position, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ivFrom:
                fragment = AddressListFragment.newInstance(ADDRESS_FROM);
                openFragmentWithArgument(fragment, ADDRESS_FROM);
                break;
            case R.id.ivTo:
                fragment = AddressListFragment.newInstance(ADDRESS_TO);
                openFragmentWithArgument(fragment, ADDRESS_TO);

                break;
            case R.id.buttonDirection:
                // Getting URL to the Google Directions API
                if (myDirection.getAddressFrom().getLatitude() != 0 && myDirection.getAddressTo().getLatitude() != 0){

                    if (!checkContains(latLngAddresses, myDirection.addressFrom)){
                        latLngAddresses.addFirst(myDirection.addressFrom);
                    }
                    if (!checkContains(latLngAddresses, myDirection.addressTo)){
                        latLngAddresses.addFirst(myDirection.addressTo);
                    }
                    while (latLngAddresses.size() > 10){
                        latLngAddresses.removeLast();
                    }

                    setListToSharedPref(latLngAddresses);

                    String url = getDirectionsUrl(myDirection.getLatLngFrom(), myDirection.getLatLngTo());

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }else {
                    Toast.makeText(this, R.string.you_address, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private boolean checkContains(LinkedList<LatLngAddress> latLngAddresses, LatLngAddress newAddress){
        boolean b = false;
        LatLngAddress temp = new LatLngAddress(0,0,"null");
        for (LatLngAddress address : latLngAddresses){
            if (address.getFullAddress().equals(newAddress.getFullAddress())) {
                b = true;
                temp = address;
            }
        }
        if (b){
            latLngAddresses.remove(temp);
            latLngAddresses.addFirst(newAddress);
        }
        return b;
    }

    private void openFragmentWithArgument(Fragment fragment, int i) {
        Bundle arg = new Bundle();
        arg.putInt("key", i);
        fragment.setArguments(arg);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.list, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(null)
                .commit();
    }
    private void closeFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().hide(fragment).commit();
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    @Override
    public void onListFrom(LatLngAddress item) {
        closeFragment(fragment);
        myDirection.setAddressFrom(new LatLngAddress(item));
        updateMap();
        edFrom.requestFocus();
    }

    @Override
    public void onListTo(LatLngAddress item) {
        closeFragment(fragment);
        myDirection.setAddressTo(new LatLngAddress(item));
        edTo.requestFocus();
        updateMap();
    }


    //Request Permission myLocation
    private void showSnackbarPermission() {
        snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.profile_location_snackbar_permission, Snackbar.LENGTH_SHORT);

        snackbar.setAction(android.R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MapsActivity.this, NAME_PERMISSION, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }).show();
        snackbar.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }
    private void enableMyLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showSnackbarPermission();
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, NAME_PERMISSION, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mButtonLocation.setVisibility(View.INVISIBLE);
        }

    }



    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }
    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
                camera(12, myDirection.getLatLngFrom());
            }
        }
    }
    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



    private void updateMap() {
        mMap.clear();
        if (myDirection.getAddressFrom().getLatitude() != 0){
            mMap.addMarker(new MarkerOptions()
                    .position(myDirection.getLatLngFrom())
                    .title(MAP_MARKER_FROM));
            ListIterator<LatLngAddress> iterator = latLngAddresses.listIterator();
            edFrom.setText(myDirection.getAddressFrom().getFullAddress());
        }
        if (myDirection.getAddressTo().getLatitude() != 0) {
            mMap.addMarker(new MarkerOptions()
                    .position(myDirection.getLatLngTo())
                    .title(MAP_MARKER_TO));
            edTo.setText(myDirection.getAddressTo().getFullAddress());
        }
    }

    private void camera(int zoom , LatLng place) {
        LatLng chosenPlace = place;
        if (chosenPlace != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(chosenPlace)
                    .zoom(zoom)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            chosenPlace = new LatLng(50.448950, 30.522815);
            camera(12, chosenPlace);
        }
    }

    public List<LatLngAddress> getLatLngLngAddresses(){
        return latLngAddresses;
    }

    private void setListToSharedPref(LinkedList<LatLngAddress> addresses){
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        ListAddresses list = new ListAddresses(addresses);
        String json = gson.toJson(list);
        Log.d("Json list Addresses", json);
        prefsEditor.putString("MyObject", json);
        prefsEditor.commit();
    }

    private LinkedList<LatLngAddress> getListFromSharedPref(){
        Gson gson = new Gson();
        String json = mPrefs.getString("MyObject", "");
        ListAddresses list = gson.fromJson(json, ListAddresses.class);
        if (list != null){
            return list.getList();
        }
        return new LinkedList<>();
    }

    private class MyDirection {
        private LatLngAddress addressFrom;
        private LatLngAddress addressTo;

        private MyDirection() {
            addressFrom = new LatLngAddress(0,0,"null 0");
            addressTo = new LatLngAddress(0,0,"null 1");
        }

        private LatLngAddress getAddressFrom() {
            return addressFrom;
        }

        private void setAddressFrom(LatLngAddress address) {
            this.addressFrom = address;
        }

        private LatLngAddress getAddressTo() {
            return addressTo;
        }

        private void setAddressTo(LatLngAddress address) {
            addressTo = address;
        }

        //short access methods
        private LatLng getLatLngFrom(){
            return new LatLng(addressFrom.getLatitude(), addressFrom.getLongitude());
        }

        private LatLng getLatLngTo(){
            return new LatLng(addressTo.getLatitude(), addressTo.getLongitude());
        }

        private void setLatLngFrom(LatLng latLng){
            addressFrom.setLatitude(latLng.latitude);
            addressFrom.setLongitude(latLng.longitude);
        }

        private void setLatLngTo(LatLng latLng){
            addressTo.setLatitude(latLng.latitude);
            addressTo.setLongitude(latLng.longitude);
        }
    }
}

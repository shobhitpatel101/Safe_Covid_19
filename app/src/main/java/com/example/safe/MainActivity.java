package com.example.safe;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends Activity {

    Button btn_sethome, req;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    FirebaseAuth fAuth;
    DatabaseReference FD, LD;
    TextView editlong, editlati,editismarked,editdistance,txtll;
    TextView tvCases,tvRecovered,tvTotalDeaths;
    private FirebaseAuth mAuth;

    // GPSTracker class
    GPSTracker gps;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FD = FirebaseDatabase.getInstance().getReference("users");
        LD = FirebaseDatabase.getInstance().getReference("Hotspot_Requested");
//####################################### login check ####################################################

       if(currentUser == null){
            //Intent i = new Intent(MainActivity.this, LogIn.class);
            startActivity(new Intent(getApplicationContext(), LogIn.class));
            //finish();
        }

//#######################################################################################################
        tvCases = findViewById(R.id.txtcase);
        tvRecovered = findViewById(R.id.txtreco);
        tvTotalDeaths = findViewById(R.id.txtdt);
        req = findViewById(R.id.request);
        fetchData();

        editlati = (TextView) findViewById(R.id.edit_lati);
        editlong = (TextView) findViewById(R.id.edit_longi);
        editismarked = (TextView) findViewById(R.id.edit_isinside);
        editdistance = (TextView) findViewById(R.id.edit_distance);

        txtll = (TextView) findViewById(R.id.txtll);
        txtll.setTextColor(Color.GREEN);

        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will
                //execute every time, else your else part will work
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getlocation();

        gps = new GPSTracker(MainActivity.this);
        Location gps_curr = gps.getLocationvar();
        Location gps_home = gps.getLocationvar();

        double lati = Double.parseDouble(showPreferences("lati"));
        double longi = Double.parseDouble(showPreferences("long"));

        //linear layout set background
        boolean ismarked = isMarkerOutsideCircle(gps_curr, lati, longi , 500); //500 meter
        if(!ismarked) {
            editismarked.setText("No");
            txtll.setTextColor(Color.GREEN);

        }
        else{
            editismarked.setText("Yes");
            txtll.setTextColor(Color.RED);

        }

        //set home location button
        btn_sethome = (Button) findViewById(R.id.btn_ssethome);
        btn_sethome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    SavePreferences("long", gps.getLongitude() + "");
                    SavePreferences("lati", gps.getLatitude() + "");
                    HashMap<String, Float>loc_map = new HashMap<>();
                    loc_map.put("Latitude", (float)gps.getLatitude());
                    loc_map.put("Longitude", (float)gps.getLongitude());
                    Toast.makeText(MainActivity.this, "Home Location set as, Longitude:"+ gps.getLongitude()
                            + " Latitude:" + gps.getLatitude() ,Toast.LENGTH_LONG).show();
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                //doWhatEveryYouWant :)

            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            //doWhatEveryYouWant :)

        } else {
            checkPermission();
        }
    }

    // get location
    public void getlocation(){
        // create class object
        gps = new GPSTracker(MainActivity.this);

        // check if GPS enabled
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            editlati.setText(latitude + "");
            editlong.setText(longitude + "");
            // \n is for new line
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
            //       + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    // check for circle parameter
    private boolean isMarkerOutsideCircle(Location curr, double lati, double longi, double radius) {
        float[] distances = new float[1];
        Location.distanceBetween(curr.getLongitude(),
                curr.getLatitude(),
                longi,
                lati, distances);

        editdistance.setText(distances[0]+"");
        return radius < distances[0];
    }

    //save home location in local strage
    public void SavePreferences(String key, String value) {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    // load from local storage
    public String showPreferences(String key){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String savedPref = sharedPreferences.getString(key, "0");
        return savedPref;
    }

    // for fetching the details from api
    private void fetchData() {

        //String url  = "https://corona.lmao.ninja/v2/all/";
        String url  = "https://corona.lmao.ninja/v2/countries/india";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response.toString());

                            tvCases.setText(jsonObject.getString("cases"));
                            tvRecovered.setText(jsonObject.getString("recovered"));
                            tvTotalDeaths.setText(jsonObject.getString("deaths"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);


    }
}

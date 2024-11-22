package com.example.projetmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.health.connect.datatypes.ExerciseRoute;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codebyashish.googledirectionapi.AbstractRouting;
import com.codebyashish.googledirectionapi.ErrorHandling;
import com.codebyashish.googledirectionapi.RouteDrawing;
import com.codebyashish.googledirectionapi.RouteInfoModel;
import com.codebyashish.googledirectionapi.RouteListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.GeoApiContext;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity implements OnMapReadyCallback, RouteListener {
    private final int FINE_PERMISSION_CODE=1;
    private GoogleMap myMap;
    private Handler handler;
    private Polyline polyline;
    private long refreshtime=60000;
    private Runnable runnable;
    private Marker movableMarker,fixedMarker;
    private EditText latitude,longitude,numero,pseudo;
    private Button button;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(AddActivity.this);
        handler=new Handler();
        getLastLocation();
        handler.postDelayed(runnable=new Runnable() {
            @Override
            public void run() {
//                handler.postDelayed(runnable,refreshtime);
                getLastLocation();
                handler.postDelayed(runnable,refreshtime);
            }
        },refreshtime);
//        getLastLocation();
        latitude=findViewById(R.id.latitudeadd);
        longitude=findViewById(R.id.longitudeadd);
        pseudo=findViewById(R.id.pseudoadd);
        numero=findViewById(R.id.numeroadd);
        button=findViewById(R.id.buttonadd);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PostRequestTask().execute();
                Intent intent = new Intent(AddActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void getLastLocation(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.SEND_SMS,
                            android.Manifest.permission.RECEIVE_SMS
                    },
                    1); // Replace with your custom request code
            return;
        }
        Task<Location>task=fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location!=null){
                    currentLocation=location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(AddActivity.this);
                    Log.i("curlocation",(currentLocation.getLatitude()-1.6094136)+" "+(currentLocation.getLongitude()+132.722302));
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng sousse = new LatLng(currentLocation.getLatitude()-1.6094136, currentLocation.getLongitude()+132.722302);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    1001);
            return;
        }
        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);
        MarkerOptions markerOptions=new MarkerOptions().position(sousse).icon(getBitmapDescriptorFromVector(getApplicationContext(), R.drawable.baseline_my_location_24));
        if (fixedMarker != null) {
            fixedMarker.remove();
            fixedMarker = null; // Optional: Clear the reference.
        }
        fixedMarker=myMap.addMarker(markerOptions);
        if (movableMarker == null) {
            movableMarker = myMap.addMarker(new MarkerOptions().position(sousse).draggable(true));
        }
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sousse,17));
        myMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if(polyline!=null){
                    polyline.remove();
                }
                // Handle drag start (e.g., show a toast or log it)
                Log.d("Marker", "Drag started at: " + marker.getPosition().toString());
            }
            @Override
            public void onMarkerDrag(Marker marker) {
                // Handle the marker while dragging (e.g., show a temporary location)
                Log.d("Marker", "Dragging at: " + marker.getPosition().toString());
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                 // Handle drag end (e.g., save the new position)
                 LatLng newPosition = marker.getPosition();
                 Log.d("Marker", "Drag ended at: " + newPosition.toString());
                 latitude.setText(newPosition.latitude+"");
                 longitude.setText(newPosition.longitude+"");
                 getRoute(sousse,newPosition);
                 polyline = myMap.addPolyline(new PolylineOptions()
                        .clickable(true) // Makes the polyline clickable
                        .add(sousse, newPosition) // Add points to the polyline
                        .width(10f) // Set width
                        .color(0xFFFF0000));
                 // You can also update the marker's position elsewhere in your app or database
            }
        });
    }
    private void getRoute(LatLng from,LatLng to){
        RouteDrawing routeDrawing = new RouteDrawing.Builder()
                .context(AddActivity.this)  // pass your activity or fragment's context
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this).alternativeRoutes(true)
                .waypoints(from, to)
                .build();
        routeDrawing.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==FINE_PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }
            else {
                Toast.makeText(this, "Location permission is denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRouteFailure(ErrorHandling e) {
        Toast.makeText(AddActivity.this, "route failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRouteStart() {
        Toast.makeText(AddActivity.this, "route started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRouteSuccess(ArrayList<RouteInfoModel> list, int indexing) {
        Toast.makeText(AddActivity.this, "route success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRouteCancelled() {

    }

    private class PostRequestTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://10.0.2.2/servicephp/add_position.php");
                StringRequest stringRequest=new StringRequest(Request.Method.POST, url.toString(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("res", response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("err", error.toString());
                            }
                        }
                ){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String>params=new HashMap<>();
                        params.put("latitude",latitude.getText().toString());
                        params.put("longitude",longitude.getText().toString());
                        params.put("numero",numero.getText().toString());
                        params.put("pseudo",pseudo.getText().toString());
                        return params;
                    }
                };
                RequestQueue requestQueue= Volley.newRequestQueue(AddActivity.this);
                requestQueue.add(stringRequest);
                return "success";
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setDoOutput(true);
//                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                String formData = "longitude=" + longitude.getText() +
//                        "&latitude=" + latitude.getText() +
//                        "&numero=" + numero.getText() +
//                        "&pseudo=" + pseudo.getText();
//                OutputStream os = connection.getOutputStream();
//                os.write(formData.getBytes());
//                os.flush();
//                os.close();
//                int responseCode = connection.getResponseCode();
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    return "Data sent successfully.";
//                } else {
//                    return "Error: " + responseCode;
//                }
                //connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }
    }
    private BitmapDescriptor getBitmapDescriptorFromVector(Context context, int vectorResId) {
        // Get the vector drawable
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // Set the drawable to the appropriate size
        int height = 50;  // Set desired height
        int width = 50;   // Set desired width
        vectorDrawable.setBounds(0, 0, width, height);

        // Create a bitmap from the vector drawable
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        // Return the BitmapDescriptor
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
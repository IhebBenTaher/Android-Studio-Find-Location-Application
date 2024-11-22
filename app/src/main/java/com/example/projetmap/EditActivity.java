package com.example.projetmap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity implements OnMapReadyCallback {
    private EditText latitude,longitude,numero,pseudo;
    private double longitudev,latitudev;
    private int idposv;
    private GoogleMap myMap;
    private Button btnEdit;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map3);
        mapFragment.getMapAsync(EditActivity.this);
        latitude=findViewById(R.id.latitudeedit);
        longitude=findViewById(R.id.longitudeedit);
        pseudo=findViewById(R.id.pseudoedit);
        numero=findViewById(R.id.numeroedit);
        btnEdit=findViewById(R.id.buttonedit);
        idposv = getIntent().getIntExtra("idPosition",0);
        String pseudov = getIntent().getStringExtra("pseudo");
        String numerov = getIntent().getStringExtra("numero");
        longitudev = getIntent().getDoubleExtra("longitude",0);
        latitudev = getIntent().getDoubleExtra("latitude",0);
        pseudo.setText(pseudov);
        numero.setText(numerov);
        longitude.setText(longitudev+"");
        latitude.setText(latitudev+"");
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new EditActivity.PostRequestTask().execute();
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng sousse = new LatLng(latitudev, longitudev);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);
        MarkerOptions markerOptions=new MarkerOptions().position(sousse).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).draggable(true);
        myMap.addMarker(markerOptions);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sousse,17));
        myMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
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
            }
        });
    }
    private class PostRequestTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://10.0.2.2/servicephp/edit_position.php");
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
                        params.put("idPosition",""+idposv);
                        params.put("longitude",longitude.getText().toString());
                        params.put("numero",numero.getText().toString());
                        params.put("pseudo",pseudo.getText().toString());
                        return params;
                    }
                };
                RequestQueue requestQueue= Volley.newRequestQueue(EditActivity.this);
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
}
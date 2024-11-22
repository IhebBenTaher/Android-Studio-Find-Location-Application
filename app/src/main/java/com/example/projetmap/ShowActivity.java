package com.example.projetmap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShowActivity extends AppCompatActivity implements OnMapReadyCallback {
    private TextView idshow,pseudoshow,numeroshow;
    private ImageView backButton;
    private GoogleMap myMap;
    private double longitude,latitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(ShowActivity.this);
        idshow=findViewById(R.id.idposshow);
        pseudoshow=findViewById(R.id.pseudoshow);
        numeroshow=findViewById(R.id.numeroshow);
        backButton = findViewById(R.id.backimage);
        int idpos = getIntent().getIntExtra("idPosition",0);
        String pseudo = getIntent().getStringExtra("pseudo");
        String numero = getIntent().getStringExtra("numero");
        longitude = getIntent().getDoubleExtra("longitude",0);
        latitude = getIntent().getDoubleExtra("latitude",0);
        idshow.setText(""+idpos);
        pseudoshow.setText(pseudo);
        numeroshow.setText(numero);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowActivity.this, MainActivity.class);
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
        LatLng sousse = new LatLng(latitude, longitude);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);
        MarkerOptions markerOptions=new MarkerOptions().position(sousse).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
//        if (fixedMarker != null) {
//            fixedMarker.remove();
//            fixedMarker = null; // Optional: Clear the reference.
//        }
        myMap.addMarker(markerOptions);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sousse,17));
    }
}
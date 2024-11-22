package com.example.projetmap;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmap.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private RecyclerView recyclerView;
    private PositionAdapter positionAdapter;
    private List<Position> positionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        positionList = new ArrayList<>();
        positionAdapter = new PositionAdapter(positionList,MainActivity.this);
        recyclerView.setAdapter(positionAdapter);
        ImageView navigateButton = findViewById(R.id.addbtn);
        navigateButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            startActivity(intent);
        });

        // Fetch data
        new FetchPositionsTask().execute("http://10.0.2.2/servicephp/get_all.php");

//        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(binding.navView, navController);
    }
    private class FetchPositionsTask extends AsyncTask<String, Void, List<Position>> {

        @Override
        protected List<Position> doInBackground(String... urls) {
            List<Position> positions = new ArrayList<>();
            try {
                // Connect to the server using the provided URL
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                Log.i("datadata","a");
                connection.connect();
                Log.i("datadata","b");
                // Read the input stream
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                StringBuilder stringBuilder = new StringBuilder();
                int data = reader.read();
                while (data != -1) {
                    stringBuilder.append((char) data);
                    data = reader.read();
                }

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(stringBuilder.toString());
                if (jsonResponse.getInt("success") == 1) {
                    // Extract the positions array
                    JSONArray positionsArray = jsonResponse.getJSONArray("positions");

                    // Loop through the positions array and create Position objects
                    for (int i = 0; i < positionsArray.length(); i++) {
                        JSONObject positionObject = positionsArray.getJSONObject(i);

                        Position position = new Position(
                                positionObject.getInt("idPosition"),
                                positionObject.getDouble("longitude"),
                                positionObject.getDouble("latitude"),
                                positionObject.getString("numero"),
                                positionObject.getString("pseudo")
                        );
                        positions.add(position);
                    }
                } else {
                    // Handle case where no positions were found
                    runOnUiThread(() -> {
                        try {
                            Toast.makeText(MainActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return positions;
        }

        @Override
        protected void onPostExecute(List<Position> positions) {
            if (positions != null && !positions.isEmpty()) {
                positionList.clear();
                positionList.addAll(positions);
                positionAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MainActivity.this, "No positions found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
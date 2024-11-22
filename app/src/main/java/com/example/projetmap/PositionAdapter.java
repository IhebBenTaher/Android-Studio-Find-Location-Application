package com.example.projetmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder>{
    private List<Position> positionList;
    private Context context;
    private int curr;
    private static final int SMS_PERMISSION_CODE = 1002;
    public PositionAdapter(List<Position> positionList,Context context) {
        this.positionList = positionList;
        this.context=context;
    }

    @Override
    public PositionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_position, parent, false);
        return new PositionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PositionViewHolder holder, int position) {
        Position currentPosition = positionList.get(position);
        holder.idPosition.setText(String.valueOf(currentPosition.getIdPosition()));
        holder.longitude.setText(String.valueOf(currentPosition.getLongitude()));
        holder.latitude.setText(String.valueOf(currentPosition.getLatitude()));
        holder.numero.setText(currentPosition.getNumero());
        holder.pseudo.setText(currentPosition.getPseudo());
        holder.posread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShowActivity.class);
                intent.putExtra("idPosition", currentPosition.getIdPosition());
                intent.putExtra("pseudo",currentPosition.getPseudo());
                intent.putExtra("numero",currentPosition.getNumero());
                intent.putExtra("latitude",currentPosition.getLatitude());
                intent.putExtra("longitude",currentPosition.getLongitude());
                context.startActivity(intent);
            }
        });
        holder.posedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditActivity.class);
                intent.putExtra("idPosition", currentPosition.getIdPosition());
                intent.putExtra("pseudo",currentPosition.getPseudo());
                intent.putExtra("numero",currentPosition.getNumero());
                intent.putExtra("latitude",currentPosition.getLatitude());
                intent.putExtra("longitude",currentPosition.getLongitude());
                context.startActivity(intent);
            }
        });
        holder.posdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curr=currentPosition.getIdPosition();
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete this position?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            new PositionAdapter.PostRequestTask().execute();
                            positionList.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                            notifyItemRangeChanged(holder.getAdapterPosition(), positionList.size());
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });
        holder.possend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

                // Request the current location
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            1001);
                    return;
                }
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{
                                    Manifest.permission.SEND_SMS,
                                    Manifest.permission.RECEIVE_SMS
                            },
                            1002);
                    return;
                }
                fusedLocationClient.getLastLocation().addOnSuccessListener((Activity) context, location -> {
                    if (location != null) {
                        // Get the GPS coordinates
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Prepare the message
                        String message = "Current Position:\n" +
                                "Latitude: " + latitude + "\n" +
                                "Longitude: " + longitude;

                        // Get the recipient number
                        String recipientNumber = "(+1555)"+currentPosition.getNumero();

                        // Send SMS directly
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(recipientNumber, null, message, null, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Show error message (optional)
                            new androidx.appcompat.app.AlertDialog.Builder(context)
                                    .setTitle("Error")
                                    .setMessage("Failed to send SMS: " + e.getMessage())
                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                    } else {
                        // Handle case where location is null
                        new androidx.appcompat.app.AlertDialog.Builder(context)
                                .setTitle("Error")
                                .setMessage("Unable to fetch your current location. Please check your GPS settings.")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return positionList.size();
    }

    public static class PositionViewHolder extends RecyclerView.ViewHolder {
        public TextView idPosition, longitude, latitude, numero, pseudo;
        public ImageView posread,posedit,posdelete,possend;
        public PositionViewHolder(View itemView) {
            super(itemView);
            idPosition = itemView.findViewById(R.id.idPosition);
            longitude = itemView.findViewById(R.id.longitude);
            latitude = itemView.findViewById(R.id.latitude);
            numero = itemView.findViewById(R.id.numero);
            pseudo = itemView.findViewById(R.id.pseudo);
            posread=itemView.findViewById(R.id.posread);
            posedit=itemView.findViewById(R.id.posedit);
            posdelete=itemView.findViewById(R.id.posdelete);
            possend=itemView.findViewById(R.id.possend);
        }
    }
    private class PostRequestTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://10.0.2.2/servicephp/delete_position.php");
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
                        params.put("idPosition",""+curr);
                        return params;
                    }
                };
                RequestQueue requestQueue= Volley.newRequestQueue(context);
                requestQueue.add(stringRequest);
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }
    }
}

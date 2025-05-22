package com.example.smarthouseapp2;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Imports Volley
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.AuthFailureError;

// Imports JSON
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = "MainActivity2";

    private Handler handler = new Handler();
    private Runnable runnableCode;
    private LinearLayout linearLayout;
    private static final String URL = "http://happyresto.enseeiht.fr/smartHouse/api/v1/devices/32";
    private static final String POST_URL = "http://happyresto.enseeiht.fr/smartHouse/api/v1/devices/";
    private static final int houseId = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        linearLayout = findViewById(R.id.linearLayout);
        
        // Initialisation du runnable pour l'envoi périodique du message Bluetooth
        runnableCode = new Runnable() {
            @Override
            public void run() {
                // Envoyer le message "hello" via Bluetooth
                BluetoothConnectionManager.getInstance().sendMessage("hello");
                Log.d(TAG, "Message 'hello' envoyé via Bluetooth");
                
                // Programmer le prochain envoi dans 5 secondes
                handler.postDelayed(this, 5000);
            }
        };

        // Démarrer l'envoi périodique
        handler.post(runnableCode);

        // Initialisation de la file de requêtes Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        // Création de la requête JSON
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Vider le layout avant d'ajouter les nouveaux appareils
                            linearLayout.removeAllViews();

                            // Parcours de tous les appareils
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject device = response.getJSONObject(i);

                                View deviceView = createDeviceView(device.getInt("ID"),
                                        " [ " + device.getString("BRAND") + " ] " + device.getString("NAME"),
                                        device.getString("MODEL")
                                                + ( device.getString("DATA").isEmpty() ? "" : " | DATA: " + device.getString("DATA"))
                                                + ( device.getInt("AUTONOMY") == -1 ? "" : " | AUTONOMY: " + device.getString("AUTONOMY" ) + "%"),
                                        device.getInt("STATE") == 1
                                );
                                if (deviceView.getParent() != null) {
                                    ((ViewGroup) deviceView.getParent()).removeView(deviceView);
                                }
                                linearLayout.addView(deviceView);

                                // Ajout d'un espace vertical entre les appareils
                                View spacer = new View(MainActivity2.this);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        30); // 20dp de hauteur pour l'espace
                                spacer.setLayoutParams(params);
                                linearLayout.addView(spacer);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Erreur lors du parsing JSON: " + e.getMessage());
                            Toast.makeText(MainActivity2.this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erreur Volley: " + error.getMessage());
                        Toast.makeText(MainActivity2.this, "Erreur lors de la connexion au serveur", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Ajout de la requête à la file
        queue.add(jsonArrayRequest);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public View createDeviceView(int id, String nomAppareil, String informations, Boolean isOn) {
        RelativeLayout layout = new RelativeLayout(this);

        // Création d'un fond gris arrondi pour le layout
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16); // coins arrondis
        shape.setColor(Color.parseColor("#EEEEEE")); // couleur gris clair
        layout.setBackground(shape);

        // Ajouter une marge interne (padding) pour l'apparence
        layout.setPadding(30, 30, 30, 30);

        //paramètres de position relative
        RelativeLayout.LayoutParams paramsTopLeft =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsTopLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                RelativeLayout.TRUE);
        paramsTopLeft.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                RelativeLayout.TRUE);

        RelativeLayout.LayoutParams paramsBottomLeft =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsBottomLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                RelativeLayout.TRUE);
        paramsBottomLeft.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
                RelativeLayout.TRUE);

        RelativeLayout.LayoutParams paramsBottomRight =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsBottomRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                RelativeLayout.TRUE);
        paramsBottomRight.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
                RelativeLayout.TRUE);

        //ajout du texte
        TextView textNomAppareil = new TextView(this);
        textNomAppareil.setText(nomAppareil);
        textNomAppareil.setTextSize(18); // Taille de texte plus grande
        textNomAppareil.setTextColor(Color.BLACK); // Texte en noir
        layout.addView(textNomAppareil, paramsTopLeft);

        TextView textInformationAppareil = new TextView(this);
        textInformationAppareil.setText(informations);
        textInformationAppareil.setTextColor(Color.DKGRAY); // Texte en gris foncé
        layout.addView(textInformationAppareil, paramsBottomLeft);

        // Création du bouton ON/OFF
        ToggleButton toggleButton = new ToggleButton(this);
        toggleButton.setTextOn("ON");
        toggleButton.setTextOff("OFF");
        toggleButton.setChecked(isOn); //verifier l'état du boutton à partir du paramètre isOn qui sera recup depuis l'endpoint GET

        // Configuration des couleurs du bouton
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Vert quand ON
                buttonView.setBackgroundColor(Color.parseColor("#4CAF50")); // Vert
                buttonView.setTextColor(Color.WHITE);
            } else {
                // Rouge quand OFF
                buttonView.setBackgroundColor(Color.parseColor("#F44336")); // Rouge
                buttonView.setTextColor(Color.WHITE);
            }
        });

        // Initialiser la couleur du bouton selon son état
        if (isOn) {
            toggleButton.setBackgroundColor(Color.parseColor("#4CAF50")); // Vert
            toggleButton.setTextColor(Color.WHITE);
        } else {
            toggleButton.setBackgroundColor(Color.parseColor("#F44336")); // Rouge
            toggleButton.setTextColor(Color.WHITE);
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTurnOnOffRequest(id);
            }
        });

        // Ajout du bouton au layout
        layout.addView(toggleButton, paramsBottomRight);

        return layout;
    }

    //methode pour l'envoi de la requete off/on lors du click sur le boutton
    private void sendTurnOnOffRequest(final int deviceId) {
        // Initialisation de la file de requêtes Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        // Création de la requête pour changer l'état de l'appareil
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                POST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "État changé avec succès: " + response);
                        Toast.makeText(MainActivity2.this, "État modifié avec succès", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erreur lors de la modification de l'état: " + error.getMessage());
                        Toast.makeText(MainActivity2.this, "Échec de la modification de l'état", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("deviceId", String.valueOf(deviceId));
                params.put("houseId", String.valueOf(houseId));
                params.put("action", "turnOnOff");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        // Ajout de la requête à la file
        queue.add(stringRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Arrêter l'envoi périodique quand l'activité est en pause
        handler.removeCallbacks(runnableCode);
    }
}
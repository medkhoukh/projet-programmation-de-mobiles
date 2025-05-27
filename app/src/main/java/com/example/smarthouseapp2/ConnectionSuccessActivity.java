package com.example.smarthouseapp2;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConnectionSuccessActivity extends AppCompatActivity {
    private static final String TAG = "ConnectionSuccess";
    private LinearLayout linearLayout;

    private Runnable runnableCode;
    private Handler mainHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connection_success);

        linearLayout = findViewById(R.id.LinearLayout2);
        mainHandler = new Handler(Looper.getMainLooper());

        runnableCode = new Runnable() {
            @Override
            public void run() {
                // Configurer le BluetoothConnectionManager pour recevoir les messages
                BluetoothConnectionManager.getInstance().setMessageListener(message -> {
                    try {
                        // Parser le message JSON reçu
                        JSONArray response = new JSONArray(message);
                        
                        // Exécuter les modifications UI sur le thread principal
                        runOnUiThread(() -> {
                            try {
                                // Vider le layout avant d'ajouter les nouveaux appareils
                                linearLayout.removeAllViews();

                                // Parcours de tous les appareils et ajout des vues dans le layout
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject device = response.getJSONObject(i);

                                    View deviceView = createDeviceView(
                                        device.getInt("ID"),
                                        " [ " + device.getString("BRAND") + " ] " + device.getString("NAME"),
                                        device.getString("MODEL")
                                            + (device.getString("DATA").isEmpty() ? "" : " | DATA: " + device.getString("DATA"))
                                            + (device.getInt("AUTONOMY") == -1 ? "" : " | AUTONOMY: " + device.getString("AUTONOMY") + "%"),
                                        device.getInt("STATE") == 1
                                    );

                                    if (deviceView.getParent() != null) {
                                        ((ViewGroup) deviceView.getParent()).removeView(deviceView);
                                    }
                                    linearLayout.addView(deviceView);

                                    // Ajout d'un espace vertical entre les appareils
                                    View spacer = new View(ConnectionSuccessActivity.this);
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        30); // 30dp de hauteur pour l'espace
                                    spacer.setLayoutParams(params);
                                    linearLayout.addView(spacer);
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Erreur lors du parsing JSON: " + e.getMessage());
                                Toast.makeText(ConnectionSuccessActivity.this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "Erreur lors du parsing JSON initial: " + e.getMessage());
                    }
                });
                
                // Planifier la prochaine exécution
                mainHandler.postDelayed(this, 1000);
            }
        };
        
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
        JSONObject obj = new JSONObject();
        try {
            obj.put("deviceId", deviceId);
            // Ajoute un champ 'padding' pour allonger le message
            StringBuilder padding = new StringBuilder();
            for (int i = 0; i < 1900; i++) { // 500 caractères pour dépasser le buffer
                padding.append("X");
            }
            obj.put("padding", padding.toString());
            BluetoothConnectionManager.getInstance().sendMessage(obj.toString());
            Log.d(TAG, "id device à allumer/eteindre envoyé (JSON long): " + obj.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors de la création du JSON pour l'envoi de l'ID: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Démarre l'exécution périodique
        mainHandler.post(runnableCode);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stoppe les appels programmés quand l'activité est en pause
        mainHandler.removeCallbacks(runnableCode);
    }
} 
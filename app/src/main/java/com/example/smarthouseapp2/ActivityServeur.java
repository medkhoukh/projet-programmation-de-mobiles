package com.example.smarthouseapp2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.UUID;

public class ActivityServeur extends AppCompatActivity {

    private static final String TAG = "ActivityServeur";
    private static final String NAME = "SmartHouseApp";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerSocket serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_serveur);

        // Initialisation du Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (bluetoothAdapter.isEnabled()) {
                Log.e(TAG, "Bluetooth enabled");
                // Démarrer le serveur Bluetooth
                AcceptThread serverClass = new AcceptThread();
                serverClass.start();
            } else {
                bluetoothAdapter.disable();
                Log.e(TAG, "Bluetooth disabled");
                Toast.makeText(this, "Veuillez activer le Bluetooth", Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e.getMessage());
            Toast.makeText(this, "Permission Bluetooth manquante", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
                Log.e(TAG, "Socket serveur créé avec succès");
            } catch (IOException e) {
                Log.e(TAG, "Échec de la création du socket serveur", e);
            } catch (SecurityException e) {
                Log.e(TAG, "Exception de sécurité lors de la création du socket", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    Log.e(TAG, "Connexion acceptée");
                } catch (IOException e) {
                    Log.e(TAG, "Échec de l'acceptation de la connexion", e);
                    break;
                } catch (SecurityException e) {
                    Log.e(TAG, "Exception de sécurité lors de l'acceptation", e);
                    break;
                }

                if (socket != null) {
                    try {
                        mmServerSocket.close();
                        Log.e(TAG, "Socket serveur fermé après connexion réussie");
                        // Lancer l'activité de succès
                        Intent intent = new Intent(ActivityServeur.this, ConnectionSuccessActivity.class);
                        startActivity(intent);
                    } catch (IOException e) {
                        Log.e(TAG, "Impossible de fermer le socket serveur", e);
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
                Log.e(TAG, "Socket serveur fermé");
            } catch (IOException e) {
                Log.e(TAG, "Impossible de fermer le socket serveur", e);
            }
        }
    }
}
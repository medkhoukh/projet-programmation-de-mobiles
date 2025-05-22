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
import java.io.InputStream;
import java.io.OutputStream;
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
                        
                        // Initialiser le BluetoothConnectionManager avec le socket
                        BluetoothConnectionManager.getInstance().setConnectedThread(socket);
                        Log.e(TAG, "BluetoothConnectionManager initialisé avec le socket");

                        // Lancer l'activité de succès
                        Intent intent = new Intent(ActivityServeur.this, MainActivity2.class);
                        startActivity(intent);

                    } catch (IOException e) {
                        Log.e(TAG, "Impossible de fermer le socket serveur", e);
                    }

                    // Créer et démarrer le thread de connexion
                    ConnectedThread connectedThread = new ConnectedThread(socket);
                    connectedThread.start();

                    // Envoyer un message de test
                    //String testMessage = "Hello from Server!";
                    //connectedThread.write(testMessage.getBytes());
                    //Log.e(TAG, "Message de test envoyé: " + testMessage);

                    // Lancer l'activité de succès
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

    public static class SocketHandler {

        private static BluetoothSocket socket;

        //public static synchronized BluetoothSocket getSocket() {
        //return socket;
        // }

        public static synchronized void setSocket(BluetoothSocket socket) {
            SocketHandler.socket = socket;
        }
    }

    /**
     * Le Thread pour envoyer les messages (par Bytes).
     */
    private static class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream temponIn = null;
            OutputStream temponOut = null;

            try {
                temponIn = bluetoothSocket.getInputStream();
                temponOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = temponIn;
            outputStream = temponOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    // conversion en String
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
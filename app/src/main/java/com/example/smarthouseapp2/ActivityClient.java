package com.example.smarthouseapp2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class ActivityClient extends AppCompatActivity {

    private static final String TAG = "ActivityClient";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;

    BluetoothDevice[] btArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_client);
        Log.e(TAG, "=== BLUETOOTH CONNECTION ATTEMPT 1 ===");

        initializeBluetooth();

    }

    private void initializeBluetooth() {
        Log.e(TAG, "=== BLUETOOTH CONNECTION ATTEMPT 2 ===");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (bluetoothAdapter.isEnabled()) {
                Log.e(TAG, "blueTooth enabled");
            } else {
                bluetoothAdapter.disable();
            }


            Log.e(TAG, "Toutes les permissions vérifiées, accès aux appareils appairés");
            Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
            btArray = new BluetoothDevice[bt.size()];

            int i = 0;
            if (bt.size() > 0) {
                for (BluetoothDevice device : bt) {
                    btArray[i++] = device;
                    Log.e(TAG, "Appareil appairé trouvé: " + device.getName());
                }
            }

            if (btArray.length > 0) {
                Log.e(TAG, "Démarrage de la connexion avec " + btArray[0].getName());
                ClientClass clientClass = new ClientClass(btArray[0]);
                clientClass.start();

            } else {
                Log.e(TAG, "Aucun appareil appairé trouvé");
                Toast.makeText(this, "Aucun appareil appairé trouvé", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SECURITY EXCEPTION");
        }
    }

    private class ClientClass extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ClientClass(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.e(TAG, "=== Création du socket réussie ===");
            } catch (IOException e) {
                Log.e(TAG, "Échec de la création du socket", e);
            } catch (SecurityException e) {
                Log.e(TAG, "Exception de sécurité : permission manquante ?", e);
            }

            mmSocket = tmp;
        }

        public void run() {
            try {


                bluetoothAdapter.cancelDiscovery();
                mmSocket.connect();

                // Ici, la connexion est réussie
                Log.e(TAG, "Connexion Bluetooth réussie !");
                runOnUiThread(() -> {
                    Toast.makeText(ActivityClient.this, "Connexion établie", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ActivityClient.this, ConnectionSuccessActivity.class);
                    startActivity(intent);

                });

            } catch (SecurityException e) {
                Log.e(TAG, "Exception de sécurité lors de la connexion", e);

            } catch (IOException connectException) {
                Log.e(TAG, "Impossible de se connecter", connectException);
                try {
                    mmSocket.close();

                } catch (IOException closeException) {
                    Log.e(TAG, "Impossible de fermer le socket après échec", closeException);
                }
            }
            ConnectedThread connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Impossible de fermer le socket", e);
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

package com.example.smarthouseapp2;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothConnectionManager {
    private static final String TAG = "BluetoothConnectionManager";
    private static BluetoothConnectionManager instance;
    private ConnectedThread connectedThread;

    private BluetoothConnectionManager() {}

    public static synchronized BluetoothConnectionManager getInstance() {
        if (instance == null) {
            instance = new BluetoothConnectionManager();
        }
        return instance;
    }

    public void setConnectedThread(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public void sendMessage(String message) {
        if (connectedThread != null) {
            connectedThread.write(message.getBytes());
        } else {
            Log.e(TAG, "ConnectedThread n'est pas initialisé");
        }
    }

    private class ConnectedThread extends Thread {
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
                    if (bytes > 0) {
                        String receivedMessage = new String(buffer, 0, bytes);
                        Log.e(TAG, "Message reçu: " + receivedMessage);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Erreur de lecture: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                Log.e(TAG, "Message envoyé");
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de l'envoi du message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 
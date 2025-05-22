package com.example.smarthouseapp2;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BluetoothConnectionManager {
    private static final String TAG = "BluetoothConnectionManager";
    private static BluetoothConnectionManager instance;
    private ConnectedThread connectedThread;
    private MessageListener messageListener;
    private static final int BUFFER_SIZE = 1024;
    private static final int QUEUE_SIZE = 10;
    private BlockingQueue<byte[]> messageQueue;
    private StringBuilder messageBuilder;
    private static final String MESSAGE_END = "\n"; // Délimiteur de fin de message

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    private BluetoothConnectionManager() {
        messageQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        messageBuilder = new StringBuilder();
    }

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

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void sendMessage(String message) {
        if (connectedThread != null) {
            // Ajouter le délimiteur de fin de message
            String messageWithDelimiter = message + MESSAGE_END;
            connectedThread.write(messageWithDelimiter.getBytes());
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
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        // Ajouter les données reçues au message en cours de construction
                        String chunk = new String(buffer, 0, bytes);
                        messageBuilder.append(chunk);

                        // Vérifier si le message est complet
                        String currentMessage = messageBuilder.toString();
                        if (currentMessage.endsWith(MESSAGE_END)) {
                            // Retirer le délimiteur de fin
                            String completeMessage = currentMessage.substring(0, currentMessage.length() - MESSAGE_END.length());
                            Log.d(TAG, "Message complet reçu: " + completeMessage);
                            
                            // Notifier le listener si présent
                            if (messageListener != null) {
                                messageListener.onMessageReceived(completeMessage);
                            }
                            
                            // Réinitialiser le builder pour le prochain message
                            messageBuilder.setLength(0);
                        }
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
                outputStream.flush(); // S'assurer que les données sont envoyées immédiatement
                Log.d(TAG, "Message envoyé");
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de l'envoi du message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 
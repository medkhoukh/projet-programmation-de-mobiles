package com.example.smarthouseapp2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ConnectionSuccessActivity extends AppCompatActivity {
    private static final String TAG = "ConnectionSuccess";
    private LinearLayout linearLayout;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connection_success);

        linearLayout = findViewById(R.id.LinearLayout2);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Configurer le BluetoothConnectionManager pour recevoir les messages
        BluetoothConnectionManager.getInstance().setMessageListener(message -> {
            // Ajouter le message au LinearLayout sur le thread principal
            mainHandler.post(() -> {
                TextView textView = new TextView(this);
                textView.setText("Message reçu: " + message);
                textView.setTextSize(16);
                textView.setPadding(20, 10, 20, 10);
                linearLayout.addView(textView);
                
                // Faire défiler jusqu'au dernier message
                linearLayout.post(() -> {
                    linearLayout.fullScroll(LinearLayout.FOCUS_DOWN);
                });
            });
        });
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
} 
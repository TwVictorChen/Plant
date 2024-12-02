package com.example.plant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.plant.databinding.ActivityControlBinding;

public class ControlActivity extends AppCompatActivity {
    private ActivityControlBinding binding;
    private TcpClient tcpClient;

    private static final String TAG = "ControlActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tcpClient = TcpClientManager.getTcpClient();

        if (tcpClient == null || !tcpClient.isConnected()) {
            Log.e(TAG, "TCP connection failed");
            Toast.makeText(this, "Failed to establish TCP connection", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.buttonBackToFirstPage.setOnClickListener(v -> startActivity(new Intent(ControlActivity.this, MainActivity.class)));
        binding.buttonForward.setOnClickListener(v -> sendCommand("A"));
        binding.buttonBackward.setOnClickListener(v -> sendCommand("B"));
        binding.buttonLeft.setOnClickListener(v -> sendCommand("C"));
        binding.buttonRight.setOnClickListener(v -> sendCommand("D"));
        binding.buttonStop.setOnClickListener(v -> sendCommand("Z"));
        binding.buttonMotorOn.setOnClickListener(v -> sendCommand("L"));
        binding.buttonMotorOff.setOnClickListener(v -> sendCommand("H"));

        initializeWebView("http://192.168.57.103");
    }

    private void initializeWebView(String url) {
        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        binding.webView.loadUrl(url);
    }

    private void sendCommand(String command) {
        if (tcpClient != null && tcpClient.isConnected()) {
            tcpClient.sendCommand(command);
            Log.d(TAG, "Command sent: " + command);
        } else {
            String errorMessage = "Failed to send command: Not connected";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            Log.e(TAG, errorMessage);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (binding.webView != null) {
            binding.webView.destroy();
        }
    }
}














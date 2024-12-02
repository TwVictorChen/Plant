package com.example.plant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.plant.databinding.ActivityMainBinding;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private TcpClient tcpClient;
    private boolean isConnected = false;
    private List<String> messages = new LinkedList<>();

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_IP = "ipAddress";
    private static final String PREF_PORT = "portNumber";
    private static final String PREF_CONNECTED = "isConnected";
    private static final int MAX_MESSAGES = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedIP = preferences.getString(PREF_IP, "");
        String savedPort = preferences.getString(PREF_PORT, "");
        isConnected = preferences.getBoolean(PREF_CONNECTED, false);

        if (!savedIP.isEmpty()) {
            binding.editTextIP.setText(savedIP);
        }
        if (!savedPort.isEmpty()) {
            binding.editTextPort.setText(savedPort);
        }
        if (isConnected) {
            binding.textViewStatus.setText("Connected to server");
            binding.buttonConnect.setText("Disconnect");
            binding.imageViewStatus.setImageResource(R.drawable.green_light);
            tcpClient = TcpClientManager.getTcpClient();
        }

        binding.buttonConnect.setOnClickListener(v -> handleConnection(preferences));
        binding.buttonGoToSecondPage.setOnClickListener(v -> navigateToSecondPage());
    }

    private void handleConnection(SharedPreferences preferences) {
        if (!isConnected) {
            String ip = binding.editTextIP.getText().toString();
            int port = Integer.parseInt(binding.editTextPort.getText().toString());
            tcpClient = TcpClientManager.getTcpClient(ip, port, new TcpClient.ConnectionListener() {
                @Override
                public void onConnected() {
                    runOnUiThread(() -> {
                        binding.textViewStatus.setText("Connected to server");
                        binding.buttonConnect.setText("Disconnect");
                        binding.imageViewStatus.setImageResource(R.drawable.green_light);
                        isConnected = true;
                        Log.d(TAG, "Connected to server");

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(PREF_IP, ip);
                        editor.putString(PREF_PORT, String.valueOf(port));
                        editor.putBoolean(PREF_CONNECTED, true);
                        editor.apply();
                    });
                }

                @Override
                public void onConnectionFailed(String errorMessage) {
                    runOnUiThread(() -> {
                        binding.textViewStatus.setText("Connection failed: " + errorMessage);
                        binding.imageViewStatus.setImageResource(R.drawable.red_light);
                        Log.e(TAG, "Connection failed: " + errorMessage);
                    });
                }

                @Override
                public void onMessageReceived(String message) {
                    runOnUiThread(() -> {
                        addMessage(message);
                        updateTextView();
                    });
                }
            });
        } else {
            TcpClientManager.resetClient();
            runOnUiThread(() -> {
                binding.textViewStatus.setText("Disconnected from server");
                binding.buttonConnect.setText("Connect");
                binding.imageViewStatus.setImageResource(R.drawable.red_light);
                isConnected = false;
                Log.d(TAG, "Disconnected from server");

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREF_CONNECTED, false);
                editor.apply();
            });
        }
    }

    private void navigateToSecondPage() {
        if (isConnected) {
            Intent intent = new Intent(MainActivity.this, ControlActivity.class);
            startActivity(intent);
        } else {
            binding.textViewStatus.setText("Please connect to the server first.");
        }
    }

    private void addMessage(String message) {
        if (messages.size() >= MAX_MESSAGES) {
            messages.remove(0);
        }
        messages.add(message);
    }

    private void updateTextView() {
        StringBuilder sb = new StringBuilder();
        for (String msg : messages) {
            sb.append(msg).append("\n");
        }
        binding.textViewMessage.setText(sb.toString());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("savedIP", binding.editTextIP.getText().toString());
        outState.putString("savedPort", binding.editTextPort.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String savedIP = savedInstanceState.getString("savedIP");
        String savedPort = savedInstanceState.getString("savedPort");
        if (savedIP != null) {
            binding.editTextIP.setText(savedIP);
        }
        if (savedPort != null) {
            binding.editTextPort.setText(savedPort);
        }
    }
}









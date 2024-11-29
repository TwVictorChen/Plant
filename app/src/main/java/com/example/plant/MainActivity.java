package com.example.plant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText editTextIP;
    private EditText editTextPort;
    private Button buttonConnect;
    private TextView textViewStatus;
    private TextView textViewMessage;
    private Button buttonGoToSecondPage;
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
        setContentView(R.layout.activity_main);

        editTextIP = findViewById(R.id.editTextIP);
        editTextPort = findViewById(R.id.editTextPort);
        buttonConnect = findViewById(R.id.buttonConnect);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewMessage = findViewById(R.id.textViewMessage);
        buttonGoToSecondPage = findViewById(R.id.buttonGoToSecondPage);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedIP = preferences.getString(PREF_IP, "");
        String savedPort = preferences.getString(PREF_PORT, "");
        isConnected = preferences.getBoolean(PREF_CONNECTED, false);
        if (!savedIP.isEmpty()) {
            editTextIP.setText(savedIP);
        }
        if (!savedPort.isEmpty()) {
            editTextPort.setText(savedPort);
        }
        if (isConnected) {
            textViewStatus.setText("Connected to server");
            buttonConnect.setText("Disconnect");
            tcpClient = TcpClientManager.getTcpClient();
        }

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected) {
                    String ip = editTextIP.getText().toString();
                    int port = Integer.parseInt(editTextPort.getText().toString());
                    tcpClient = TcpClientManager.getTcpClient(ip, port, new TcpClient.ConnectionListener() {
                        @Override
                        public void onConnected() {
                            runOnUiThread(() -> {
                                textViewStatus.setText("Connected to server");
                                buttonConnect.setText("Disconnect");
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
                                textViewStatus.setText("Connection failed: " + errorMessage);
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
                        textViewStatus.setText("Disconnected from server");
                        buttonConnect.setText("Connect");
                        isConnected = false;
                        Log.d(TAG, "Disconnected from server");

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(PREF_CONNECTED, false);
                        editor.apply();
                    });
                }
            }
        });

        buttonGoToSecondPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                    startActivity(intent);
                } else {
                    textViewStatus.setText("Please connect to the server first.");
                }
            }
        });
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
        textViewMessage.setText(sb.toString());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("savedIP", editTextIP.getText().toString());
        outState.putString("savedPort", editTextPort.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String savedIP = savedInstanceState.getString("savedIP");
        String savedPort = savedInstanceState.getString("savedPort");
        if (savedIP != null) {
            editTextIP.setText(savedIP);
        }
        if (savedPort != null) {
            editTextPort.setText(savedPort);
        }
    }
}







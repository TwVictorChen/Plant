package com.example.plant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ControlActivity extends AppCompatActivity {
    private Button buttonBackToFirstPage;
    private WebView webView;
    private Button buttonForward;
    private Button buttonBackward;
    private Button buttonLeft;
    private Button buttonRight;
    private Button buttonMotorOn;
    private Button buttonMotorOff;
    private TcpClient tcpClient;

    private static final String TAG = "ControlActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        buttonBackToFirstPage = findViewById(R.id.buttonBackToFirstPage);
        webView = findViewById(R.id.webView);
        buttonForward = findViewById(R.id.buttonForward);
        buttonBackward = findViewById(R.id.buttonBackward);
        buttonLeft = findViewById(R.id.buttonLeft);
        buttonRight = findViewById(R.id.buttonRight);
        buttonMotorOn = findViewById(R.id.buttonMotorOn);
        buttonMotorOff = findViewById(R.id.buttonMotorOff);

        // 獲取 TcpClient 實例
        tcpClient = TcpClientManager.getTcpClient();

        if (tcpClient == null) {
            Log.e(TAG, "tcpClient is null");
            Toast.makeText(this, "Failed to establish TCP connection", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!tcpClient.isConnected()) {
            Log.e(TAG, "tcpClient is not connected");
            Toast.makeText(this, "Failed to establish TCP connection", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonBackToFirstPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ControlActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("FORWARD");
            }
        });

        buttonBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("BACKWARD");
            }
        });

        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("LEFT");
            }
        });

        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("RIGHT");
            }
        });

        buttonMotorOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("MOTOR_ON");
            }
        });

        buttonMotorOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("MOTOR_OFF");
            }
        });

        // 初始化 WebView 並加載指定網址
        initializeWebView("http://192.168.57.103");
    }

    private void initializeWebView(String url) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    private void sendCommand(String command) {
        try {
            if (tcpClient != null && tcpClient.isConnected()) {
                tcpClient.sendCommand(command);
                Log.d(TAG, "Command sent: " + command);
            } else {
                Toast.makeText(this, "Failed to send command: Not connected", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to send command: Not connected");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error sending command", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error sending command: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (webView != null) {
            webView.destroy();
        }
    }
}














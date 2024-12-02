package com.example.plant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import android.os.AsyncTask;
import android.util.Log;
// Imports ...

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import android.util.Log;

public class TcpClient extends Thread implements Serializable {
    private String ip;
    private int port;
    private transient ConnectionListener listener;
    private transient Socket socket;
    private transient PrintWriter out;
    private transient BufferedReader in;

    private static final String TAG = "TcpClient";

    public interface ConnectionListener {
        void onConnected();
        void onConnectionFailed(String errorMessage);
        void onMessageReceived(String message);
    }

    public TcpClient(String ip, int port, ConnectionListener listener) {
        this.ip = ip;
        this.port = port;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            connectToServer();
        } catch (IOException e) {
            handleConnectionError(e);
        }
    }

    private void connectToServer() throws IOException {
        socket = new Socket(ip, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        if (listener != null) {
            listener.onConnected();
            Log.d(TAG, "Connected to server");
        }

        String message;
        while ((message = in.readLine()) != null) {
            if (listener != null) {
                listener.onMessageReceived(message);
                Log.d(TAG, "Message received: " + message);
            }
        }
    }

    private void handleConnectionError(IOException e) {
        if (listener != null) {
            listener.onConnectionFailed(e.getMessage());
        }
        Log.e(TAG, "Connection failed: " + e.getMessage(), e);
        closeResources();
    }

    public void sendCommand(String command) {
        CompletableFuture.runAsync(() -> {
            try {
                if (out != null) {
                    out.println(command);
                    Log.d(TAG, "Command sent: " + command);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending command: " + e.getMessage(), e);
            }
        });
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void disconnect() {
        closeResources();
    }

    private void closeResources() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                Log.d(TAG, "Socket disconnected");
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing resources: " + e.getMessage(), e);
        }
    }
}

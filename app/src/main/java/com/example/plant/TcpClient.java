package com.example.plant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import android.os.AsyncTask;
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
        } catch (IOException e) {
            if (listener != null) {
                listener.onConnectionFailed(e.getMessage());
            }
            Log.e(TAG, "Connection failed: " + e.getMessage(), e);
        }
    }

    public void sendCommand(String command) {
        new SendCommandTask().execute(command);
    }

    private class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... commands) {
            try {
                if (out != null) {
                    out.println(commands[0]);
                    Log.d(TAG, "Command sent: " + commands[0]);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending command: " + e.getMessage(), e);
            }
            return null;
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                Log.d(TAG, "Socket disconnected");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting socket: " + e.getMessage(), e);
        }
    }
}















package com.example.plant;

import java.io.Serializable;

public class TcpClientManager implements Serializable {
    private static TcpClient tcpClient;

    public static TcpClient getTcpClient(String ip, int port, TcpClient.ConnectionListener listener) {
        if (tcpClient == null) {
            tcpClient = new TcpClient(ip, port, listener);
            tcpClient.start();
        }
        return tcpClient;
    }

    public static TcpClient getTcpClient() {
        return tcpClient;
    }

    public static void resetClient() {
        if (tcpClient != null) {
            tcpClient.disconnect();
            tcpClient = null;
        }
    }
}

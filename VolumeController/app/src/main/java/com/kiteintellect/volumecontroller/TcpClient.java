package com.kiteintellect.volumecontroller;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {

    private String serverIp;
    private int serverPort;

    private OnConnected onConnected = null;

    // while this is true, the server will continue running
    private boolean mRun = false;

    // used to send messages
    private PrintWriter mBufferOut;


    public TcpClient(String SERVER_IP, int SERVER_PORT) {
        this.serverIp = SERVER_IP;
        this.serverPort = SERVER_PORT;
    }

    public void setOnConnected(OnConnected onConnected) {
        this.onConnected = onConnected;
    }

    public synchronized void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mBufferOut = null;
        onConnected = null;
    }

    public void run() throws IOException {

        mRun = true;
        InetAddress serverAddr = InetAddress.getByName(serverIp);
        Socket socket = new Socket(serverAddr, serverPort);
        if (onConnected != null)
            onConnected.connected();

        try {
            while (mRun)
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } finally {
            socket.close();
        }

    }

    public interface OnConnected {
        void connected();
    }

}

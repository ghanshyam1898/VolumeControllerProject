package com.kiteintellect.volumecontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jem.rubberpicker.RubberSeekBar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;


public class MainActivity extends AppCompatActivity {

    private static final String KEY_IP_ADDRESS = "KEY_IP_ADDRESS";
    private static final String KEY_PORT_NUMBER = "KEY_PORT_NUMBER";
    private static final String SHARED_PREFERENCE_TABLE_NAME = "TABLE_1";
    String ipAddress;
    int portNumber;
    TcpClient mTcpClient;

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mTcpClient != null){
            mTcpClient.stopClient();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCE_TABLE_NAME, Context.MODE_PRIVATE);

        ipAddress = sharedPreferences.getString(KEY_IP_ADDRESS, "");
        portNumber = sharedPreferences.getInt(KEY_PORT_NUMBER, 5000);

        final TextView connectionStatusTextView = findViewById(R.id.connection_status);
        final EditText ipAddressEditText = findViewById(R.id.ip_address_edit_text);
        final EditText portNumberEditText = findViewById(R.id.port_number_edit_text);
        Button connectButton = findViewById(R.id.connect_button);
        RubberSeekBar rubberSeekBar = findViewById(R.id.rubber_seeker_bar);

        ipAddressEditText.setText(ipAddress);
        portNumberEditText.setText(String.valueOf(portNumber));

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ipAddress = ipAddressEditText.getText().toString();
                portNumber = Integer.parseInt(portNumberEditText.getText().toString());

                sharedPreferences.edit().putString(KEY_IP_ADDRESS, ipAddress).apply();
                sharedPreferences.edit().putInt(KEY_PORT_NUMBER, portNumber).apply();

                if (mTcpClient != null)
                    mTcpClient.stopClient();

                new Thread() {
                    public void run() {
                        mTcpClient = new TcpClient(ipAddress, portNumber);

                        try {

                            mTcpClient.setOnConnected(new TcpClient.OnConnected() {

                                @Override
                                public void connected() {
                                    connectionStatusTextView.setText(getString(R.string.connection_status_connected));
                                    connectionStatusTextView.setTextColor(Color.MAGENTA);
                                }
                            });

                            mTcpClient.run();
                            // show not connected after closing the connection
                            connectionStatusTextView.setText(getString(R.string.connection_status_not_connected));
                            connectionStatusTextView.setTextColor(Color.RED);
                            mTcpClient = null;

                        } catch (IOException e) {
                            mTcpClient = null;
                            connectionStatusTextView.setText(getString(R.string.connection_status_not_connected));
                            connectionStatusTextView.setTextColor(Color.RED);
                        }

                    }
                }.start();

            }
        });

        rubberSeekBar.setOnRubberSeekBarChangeListener(new RubberSeekBar.OnRubberSeekBarChangeListener() {
            @Override
            public void onProgressChanged(RubberSeekBar rubberSeekBar, int i, boolean b) {
                if (mTcpClient != null)
                    mTcpClient.sendMessage(String.valueOf(rubberSeekBar.getCurrentValue()));
            }

            @Override
            public void onStartTrackingTouch(RubberSeekBar rubberSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(RubberSeekBar rubberSeekBar) {

            }
        });

    }
}

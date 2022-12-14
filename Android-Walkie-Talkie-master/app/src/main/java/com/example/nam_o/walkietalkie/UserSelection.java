package com.example.nam_o.walkietalkie;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import at.markushi.ui.CircleButton;

public class UserSelection extends AppCompatActivity {

    // Requesting permission to RECORD_AUDIO
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    // Define UI elements
    private CircleButton audio;
    private Button listen;
    private Button connect;
    private Button disconnect;


    //Bluetooth parameters
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothDevice device;


    private MainConversation audioClient;



    private boolean listenAttempt = false;
    private boolean connectAttempt = false;
    String ipedit;
    Toast toast;
    ServerSocket serverSocket;
    Socket s;
    private Thread thread;
    Button record1,listen11;
    // Initialization of layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_selection);

        final EditText editText1;
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        //Bluetooth
        Log.d("BLUETOOTH", "On create");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Identify UI elements

        connect = (Button) findViewById(R.id.connect);
        listen = (Button) findViewById(R.id.listen);
        disconnect = (Button) findViewById(R.id.disconnect);
        audio = (CircleButton) findViewById(R.id.audioBtn);
        editText1 = (EditText) findViewById(R.id.edittext12);
        toast = Toast.makeText(this, "Connected", Toast.LENGTH_SHORT);

        audioClient = new MainConversation();
       // audio.setVisibility(View.VISIBLE);

        try {
            serverSocket = new ServerSocket(4499);

        } catch (IOException e) {
            e.printStackTrace();
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    s=serverSocket.accept();
                    audioClient.audioCreate();
                    audioClient.setSocket(s);
                    audioClient.setupStreams();
                    audioClient.startPlaying();
                    toast.show();

                    //listenAttempt = true;

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


        // Disable microphone button
        audio.setVisibility(audio.VISIBLE);
        listenAttempt = true;
        // Microphone button pressed/released
        audio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN ) {

                    audioClient.stopPlaying();
                    audioClient.startRecording();

                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL ) {

                    audioClient.stopRecording();
                    audioClient.startPlaying();
                }
                return false;
            }
        });

        connect.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                ipedit = editText1.getText().toString();
                InitiateSocket s = new InitiateSocket();
                s.execute(ipedit,ipedit);
            }
        });

        // Listen for connection requests
        listen.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                thread.start();

            }
        });

        // Disconnect
        disconnect.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {

                boolean disconnectListen = false;
                boolean disconnectConnect = false;
                // Enable buttons and disable listView
                listen.setEnabled(true);
                connect.setEnabled(true);

                // Close the bluetooth socket


                audioClient.destroyProcesses();

                Log.d("BLUETOOTH", "Disconnect");

                if (disconnectListen || disconnectConnect) {
                    // Disconnect successful - Handle UI element change
                    audio.setVisibility(audio.GONE);
                    listen.setEnabled(true);
                    connect.setEnabled(true);
                } else {
                    // Unsuccessful disconnect - Do nothing
                }
            }
        });


    }
    class InitiateSocket extends AsyncTask<String,Void,String>
    {


        String ip;
        @Override
        protected String doInBackground(String... strings) {
            ip = strings[0];
            Socket socket;
            try {

                socket  = new Socket(ip,4499);
                audioClient.audioCreate();
                audioClient.setSocket(socket);
                audioClient.setupStreams();
                audioClient.startPlaying();

            }catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                // Permission granted
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }
}

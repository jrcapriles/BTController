package com.example.joser.btcontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Button;


public class BTController extends Activity {

    private static final String TAG = "BTController";

    ImageButton FW, BW, CCW, CW, STOP, LEFT, RIGHT;//, JOY;

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private boolean BTEnable = false;

    // Well known SPP UUID
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Insert your bluetooth devices MAC address
    private static String address = "10:14:07:02:08:73";


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "In onCreate()");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        setContentView(R.layout.activity_btcontroller);

        FW = (ImageButton) findViewById(R.id.forward);
        BW = (ImageButton) findViewById(R.id.backward);
        CW = (ImageButton) findViewById(R.id.ck);
        CCW = (ImageButton) findViewById(R.id.cck);
        STOP = (ImageButton) findViewById(R.id.stop);
        LEFT = (ImageButton) findViewById(R.id.left);
        RIGHT = (ImageButton) findViewById(R.id.right);

//        JOY = (Button) findViewById(R.id.buttonJoy);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        FW.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("10");
                Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked Forward", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        BW.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("20");
                Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked Backward", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        CW.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("30");
                Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked Clockwise", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        CCW.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("40");
                Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked Counter-Clockwise", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        STOP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("50");
                Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked Stop", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        RIGHT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("60");
                Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked Right", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        LEFT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("70");
                Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked Left", Toast.LENGTH_SHORT);
                msg.show();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...In onResume - Attempting client connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting to Remote...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Creating Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }


    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
                BTEnable = true;
            }else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                BTEnable = true;
            }
        }
    }

    private void errorExit(String title, String message){
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }

    private void sendData(String message) {

        if(!BTEnable){
            Toast msg = Toast.makeText(getBaseContext(),
                    "Bluetooth is not active.", Toast.LENGTH_LONG);
            msg.show();
            return;
        }


        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Sending data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.btcontroller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()){
            case R.id.action_settings:
                Toast msg_setting = Toast.makeText(getBaseContext(),
                        "You have clicked Settings", Toast.LENGTH_SHORT);
                msg_setting.show();
                return true;
            case R.id.action_connect:
                Toast msg_connect = Toast.makeText(getBaseContext(),
                        "You have clicked Connect", Toast.LENGTH_SHORT);
                msg_connect.show();
                checkBTState();
                return true;
            case R.id.action_discover:
                Toast msg_discover = Toast.makeText(getBaseContext(),
                        "You have clicked Discover", Toast.LENGTH_SHORT);
                msg_discover.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}










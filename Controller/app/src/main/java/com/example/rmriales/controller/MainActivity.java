package com.example.rmriales.controller;

//import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
//AppCompatActivity
public class MainActivity extends AppCompatActivity {

    private static final String timeoutState = "timeoutState";

    private String time;

    private BluetoothAdapter BTAdapter = null;
    private BluetoothSocket BTSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private ConnectedThread cThread;
    private static final String address = "20:15:08:10:80:91";


    RadioButton enabled;
    RadioButton disabled;
    EditText countdown;
    RadioGroup radioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState == null) {
            time = "5";
        } else {
            time = savedInstanceState.getString(timeoutState);
        }


        enabled = (RadioButton) findViewById(R.id.enableRadioButton);
        disabled = (RadioButton) findViewById(R.id.disableRadioButton);
        countdown = (EditText) findViewById(R.id.timeOutEditText);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        addListenerToRadios();


        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }





    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    public void onClick (View v){
        switch(v.getId()){
            case R.id.button:
                time = countdown.getText().toString();
                time = "t"+time;
                cThread.write(time);
                break;
            case R.id.lightOffButton:
                cThread.write("l0");
                break;
            case R.id.lightOnButton:
                cThread.write("l1");
            case R.id.remote:
                break;
        }
    }


    public void onResume() {
        super.onResume();

        BluetoothDevice device = BTAdapter.getRemoteDevice(address);


           try {
                BTSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Socket creation failed.", Toast.LENGTH_LONG).show();
            }
            try {
                BTSocket.connect();
            } catch (IOException e2) {
                try {
                    Log.e("","trying fallback...");

                    BTSocket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                    BTSocket.connect();

                    Log.e("","Connected");
                }
                catch (Exception e3) {
                    Log.e("", "Couldn't establish Bluetooth connection!");
                }

            }

            cThread = new ConnectedThread(BTSocket);
            cThread.start();
            cThread.write("1");
        }


    public void onPause(){
        super.onPause();
            try {
                BTSocket.close();
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "Connection could not close.", Toast.LENGTH_LONG).show();
            }
        }


    private void checkBTState(){
        if(BTAdapter==null){
            Toast.makeText(getBaseContext(), "Device not supported.", Toast.LENGTH_LONG).show();
        }else{
            if(!BTAdapter.isEnabled()){
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, 1);
            }
        }
    }

    private void addListenerToRadios(){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (enabled.isChecked()) {
                    cThread.write("e1");
                } else if (disabled.isChecked()) {
                    cThread.write("e0");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmOutStream = tmpOut;
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}
